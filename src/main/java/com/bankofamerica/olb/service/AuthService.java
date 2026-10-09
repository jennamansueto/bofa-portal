package com.bankofamerica.olb.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.bankofamerica.olb.dao.CustomerDAO;
import com.bankofamerica.olb.model.Customer;

public class AuthService {

    public static final int RESULT_OK = 0;
    public static final int RESULT_INVALID = 1;
    public static final int RESULT_LOCKED = 2;

    private final CustomerDAO customerDAO = new CustomerDAO();

    public static class Outcome {
        public final int result;
        public final Customer customer;
        Outcome(int result, Customer customer) { this.result = result; this.customer = customer; }
    }

    public Outcome authenticate(String userId, String password) {
        StringBuffer hash = new StringBuffer();
        Customer c = customerDAO.findByUserId(userId, hash);
        if (c == null) return new Outcome(RESULT_INVALID, null);
        if ("L".equals(c.getStatusCode())) return new Outcome(RESULT_LOCKED, null);
        if (!md5Hex(password).equalsIgnoreCase(hash.toString())) {
            customerDAO.recordLogin(c.getCustId(), false);
            return new Outcome(c.getFailCount() + 1 >= 3 ? RESULT_LOCKED : RESULT_INVALID, null);
        }
        customerDAO.recordLogin(c.getCustId(), true);
        return new Outcome(RESULT_OK, c);
    }

    /** Legacy unsalted MD5; replacement tracked under OLB-2211. */
    static String md5Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] d = md.digest(s.getBytes("UTF-8"));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < d.length; i++) {
                String h = Integer.toHexString(d[i] & 0xff);
                if (h.length() == 1) sb.append('0');
                sb.append(h);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
