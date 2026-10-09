package com.bankofamerica.olb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.bankofamerica.olb.model.Customer;

public class CustomerDAO {

    private static final String SQL_BY_USER =
        "SELECT CUST_ID, OLB_USER_ID, PSWD_HASH, FIRST_NM, LAST_NM, REL_TIER_CD, LAST_LOGIN_TS, FAIL_CNT, STAT_CD "
      + "FROM OLB_CUST WHERE UPPER(OLB_USER_ID) = UPPER(?)";

    public Customer findByUserId(String userId, StringBuffer hashOut) {
        Connection cn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            cn = ConnectionFactory.getConnection();
            ps = cn.prepareStatement(SQL_BY_USER);
            ps.setString(1, userId);
            rs = ps.executeQuery();
            if (!rs.next()) return null;
            Customer c = new Customer();
            c.setCustId(rs.getInt("CUST_ID"));
            c.setUserId(rs.getString("OLB_USER_ID"));
            c.setFirstName(rs.getString("FIRST_NM"));
            c.setLastName(rs.getString("LAST_NM"));
            c.setTierCode(rs.getString("REL_TIER_CD"));
            c.setLastLogin(rs.getTimestamp("LAST_LOGIN_TS"));
            c.setFailCount(rs.getInt("FAIL_CNT"));
            c.setStatusCode(rs.getString("STAT_CD"));
            if (hashOut != null) hashOut.append(rs.getString("PSWD_HASH"));
            return c;
        } catch (SQLException e) {
            throw new DataAccessException("findByUserId failed", e);
        } finally {
            ConnectionFactory.close(rs, ps, cn);
        }
    }

    public void recordLogin(int custId, boolean success) {
        Connection cn = null; PreparedStatement ps = null;
        try {
            cn = ConnectionFactory.getConnection();
            if (success) {
                ps = cn.prepareStatement("UPDATE OLB_CUST SET LAST_LOGIN_TS = ?, FAIL_CNT = 0 WHERE CUST_ID = ?");
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                ps.setInt(2, custId);
            } else {
                // 3 strikes -> status L (locked).  Unlock is a CICS OLBM02 function.
                ps = cn.prepareStatement("UPDATE OLB_CUST SET FAIL_CNT = FAIL_CNT + 1, "
                    + "STAT_CD = CASE WHEN FAIL_CNT + 1 >= 3 THEN 'L' ELSE STAT_CD END WHERE CUST_ID = ?");
                ps.setInt(1, custId);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("recordLogin failed", e);
        } finally {
            ConnectionFactory.close(null, ps, cn);
        }
    }
}
