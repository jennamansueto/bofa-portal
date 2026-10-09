package com.bankofamerica.olb.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

/**
 * Poor man's connection pool holder.  Initialised once by StartupListener.
 * In WAS this is replaced by the jdbc/OLBP01 JNDI data source (see
 * deployment notes in docs/RUNBOOK.txt).
 */
public final class ConnectionFactory {

    private static final Logger LOG = Logger.getLogger(ConnectionFactory.class);
    private static BasicDataSource ds;

    private ConnectionFactory() {}

    public static synchronized void init(String driver, String url, String user, String password) {
        if (ds != null) return;
        BasicDataSource b = new BasicDataSource();
        b.setDriverClassName(driver);
        b.setUrl(url);
        b.setUsername(user);
        b.setPassword(password);
        b.setMaxActive(8);
        b.setMaxIdle(4);
        b.setDefaultAutoCommit(true);
        ds = b;
        LOG.info("Data source initialised: " + url);
    }

    public static Connection getConnection() throws SQLException {
        if (ds == null) throw new SQLException("ConnectionFactory not initialised");
        return ds.getConnection();
    }

    public static void close(ResultSet rs, Statement st, Connection cn) {
        if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
        if (st != null) try { st.close(); } catch (SQLException e) { /* ignore */ }
        if (cn != null) try { cn.close(); } catch (SQLException e) { /* ignore */ }
    }

    public static synchronized void shutdown() {
        if (ds != null) {
            try { ds.close(); } catch (SQLException e) { LOG.warn("Error closing pool", e); }
            ds = null;
        }
    }
}
