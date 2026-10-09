package com.bankofamerica.olb.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Guards /secure/*: no customer in session -> back to the sign-in page. */
public class AuthFilter implements Filter {

    public static final String SESSION_CUSTOMER = "olb.customer";

    public void init(FilterConfig cfg) throws ServletException { }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) res;
        HttpSession s = r.getSession(false);
        if (s == null || s.getAttribute(SESSION_CUSTOMER) == null) {
            w.setHeader("Cache-Control", "no-cache, no-store");
            w.sendRedirect(r.getContextPath() + "/index.jsp?expired=1");
            return;
        }
        w.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        w.setHeader("Pragma", "no-cache");
        chain.doFilter(req, res);
    }

    public void destroy() { }
}
