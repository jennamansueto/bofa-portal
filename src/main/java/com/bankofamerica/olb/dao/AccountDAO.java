package com.bankofamerica.olb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bankofamerica.olb.model.Account;

public class AccountDAO {

    private static final String COLS =
        "ACCT_ID, CUST_ID, ACCT_TYP_CD, PROD_NM, ACCT_NBR_LAST4, CUR_BAL_CENTS, AVL_BAL_CENTS, EXT_BANK_NM, SEQ_NO";

    public List findByCustomer(int custId) {
        Connection cn = null; PreparedStatement ps = null; ResultSet rs = null;
        List out = new ArrayList();
        try {
            cn = ConnectionFactory.getConnection();
            ps = cn.prepareStatement("SELECT " + COLS + " FROM OLB_ACCT WHERE CUST_ID = ? AND STAT_CD = 'A' ORDER BY SEQ_NO");
            ps.setInt(1, custId);
            rs = ps.executeQuery();
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (SQLException e) {
            throw new DataAccessException("findByCustomer failed", e);
        } finally {
            ConnectionFactory.close(rs, ps, cn);
        }
    }

    public Account findById(Connection cn, String acctId) throws SQLException {
        PreparedStatement ps = null; ResultSet rs = null;
        try {
            ps = cn.prepareStatement("SELECT " + COLS + " FROM OLB_ACCT WHERE ACCT_ID = ? AND STAT_CD = 'A'");
            ps.setString(1, acctId);
            rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } finally {
            ConnectionFactory.close(rs, ps, null);
        }
    }

    public void adjustBalances(Connection cn, String acctId, long currentDeltaCents, long availableDeltaCents) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = cn.prepareStatement("UPDATE OLB_ACCT SET CUR_BAL_CENTS = CUR_BAL_CENTS + ?, AVL_BAL_CENTS = AVL_BAL_CENTS + ? WHERE ACCT_ID = ?");
            ps.setLong(1, currentDeltaCents);
            ps.setLong(2, availableDeltaCents);
            ps.setString(3, acctId);
            if (ps.executeUpdate() != 1) throw new SQLException("Balance update affected 0 rows for " + acctId);
        } finally {
            ConnectionFactory.close(null, ps, null);
        }
    }

    private Account map(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setAcctId(rs.getString("ACCT_ID"));
        a.setCustId(rs.getInt("CUST_ID"));
        a.setTypeCode(rs.getString("ACCT_TYP_CD"));
        a.setProductName(rs.getString("PROD_NM"));
        a.setLast4(rs.getString("ACCT_NBR_LAST4"));
        a.setCurrentBalanceCents(rs.getLong("CUR_BAL_CENTS"));
        a.setAvailableBalanceCents(rs.getLong("AVL_BAL_CENTS"));
        a.setExternalBankName(rs.getString("EXT_BANK_NM"));
        a.setSeqNo(rs.getInt("SEQ_NO"));
        return a;
    }
}
