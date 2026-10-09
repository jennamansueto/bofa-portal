package com.bankofamerica.olb.web;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.bankofamerica.olb.dao.ConnectionFactory;
import com.bankofamerica.olb.service.TransferService;

/**
 * Bootstraps the connection pool and (dev only) loads the schema + seed
 * into the in-memory HSQLDB that stands in for DB2 OLBP01.
 */
public class StartupListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(StartupListener.class);
    public static final String ATTR_TRANSFER_SERVICE = "olb.transferService";

    public void contextInitialized(ServletContextEvent evt) {
        ServletContext ctx = evt.getServletContext();
        ConnectionFactory.init(
            ctx.getInitParameter("olb.jdbc.driver"),
            ctx.getInitParameter("olb.jdbc.url"),
            ctx.getInitParameter("olb.jdbc.user"),
            ctx.getInitParameter("olb.jdbc.password"));
        try {
            runScript("sql/schema.sql");
            runScript("sql/seed.sql");
        } catch (Exception e) {
            LOG.fatal("Schema load failed", e);
            throw new RuntimeException(e);
        }
        int cutoff = Integer.parseInt(ctx.getInitParameter("olb.cutoff.hour"));
        ctx.setAttribute(ATTR_TRANSFER_SERVICE, new TransferService(cutoff));
        LOG.info("OLB Transfers Portal started (cutoff hour " + cutoff + " ET)");
    }

    public void contextDestroyed(ServletContextEvent evt) {
        Connection cn = null; Statement st = null;
        try {
            cn = ConnectionFactory.getConnection();
            st = cn.createStatement();
            st.execute("SHUTDOWN");
        } catch (Exception e) {
            LOG.warn("HSQLDB shutdown: " + e.getMessage());
        } finally {
            ConnectionFactory.close(null, st, cn);
        }
        ConnectionFactory.shutdown();
    }

    private void runScript(String resource) throws Exception {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (in == null) throw new IllegalStateException("Missing " + resource);
        BufferedReader r = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
        StringBuffer sb = new StringBuffer();
        String line;
        Connection cn = ConnectionFactory.getConnection();
        Statement st = cn.createStatement();
        int n = 0;
        try {
            while ((line = r.readLine()) != null) {
                String t = line.trim();
                if (t.startsWith("--") || t.length() == 0) continue;
                sb.append(line).append('\n');
                if (t.endsWith(";")) {
                    String sql = sb.toString().trim();
                    st.execute(sql.substring(0, sql.length() - 1));
                    sb.setLength(0);
                    n++;
                }
            }
        } finally {
            ConnectionFactory.close(null, st, cn);
            r.close();
        }
        LOG.info("Executed " + n + " statements from " + resource);
    }
}
