package com.bankofamerica.olb.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bankofamerica.olb.model.Transfer;

public class TransferDAO {

    private static final String SELECT =
        "SELECT X.XFR_ID, X.CONF_NBR, X.CUST_ID, X.FROM_ACCT_ID, X.TO_ACCT_ID, X.AMT_CENTS, X.FEE_CENTS, X.XFR_TYP_CD, "
      + "X.REL_TIER_CD, X.FREQ_CD, X.SCHED_DT, X.POST_DT, X.STAT_CD, X.MEMO, X.CRT_TS, "
      + "F.PROD_NM AS F_NM, F.ACCT_NBR_LAST4 AS F_L4, F.EXT_BANK_NM AS F_BK, "
      + "T.PROD_NM AS T_NM, T.ACCT_NBR_LAST4 AS T_L4, T.EXT_BANK_NM AS T_BK "
      + "FROM OLB_XFR X "
      + "LEFT JOIN OLB_ACCT F ON F.ACCT_ID = X.FROM_ACCT_ID "
      + "LEFT JOIN OLB_ACCT T ON T.ACCT_ID = X.TO_ACCT_ID ";

    public void insert(Connection cn, Transfer t) throws SQLException {
        PreparedStatement ps = null; ResultSet rs = null;
        try {
            ps = cn.prepareStatement(
                "INSERT INTO OLB_XFR (CONF_NBR, CUST_ID, FROM_ACCT_ID, TO_ACCT_ID, AMT_CENTS, FEE_CENTS, XFR_TYP_CD, "
              + "REL_TIER_CD, FREQ_CD, SCHED_DT, POST_DT, STAT_CD, MEMO, CRT_TS) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            int i = 1;
            ps.setString(i++, t.getConfirmationNumber());
            ps.setInt(i++, t.getCustId());
            ps.setString(i++, t.getFromAcctId());
            ps.setString(i++, t.getToAcctId());
            ps.setLong(i++, t.getAmountCents());
            ps.setLong(i++, t.getFeeCents());
            ps.setString(i++, t.getTypeCode());
            ps.setString(i++, t.getTierCode());
            ps.setString(i++, t.getFrequencyCode());
            ps.setDate(i++, t.getScheduledDate());
            ps.setDate(i++, t.getPostDate());
            ps.setString(i++, t.getStatusCode());
            ps.setString(i++, t.getMemo());
            ps.setTimestamp(i++, t.getCreatedTs());
            ps.executeUpdate();
            ConnectionFactory.close(null, ps, null);
            ps = cn.prepareStatement("CALL IDENTITY()");
            rs = ps.executeQuery();
            if (rs.next()) t.setXfrId(rs.getInt(1));
        } finally {
            ConnectionFactory.close(rs, ps, null);
        }
    }

    public Transfer findByConfirmation(int custId, String confNbr) {
        Connection cn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            cn = ConnectionFactory.getConnection();
            ps = cn.prepareStatement(SELECT + "WHERE X.CUST_ID = ? AND X.CONF_NBR = ?");
            ps.setInt(1, custId);
            ps.setString(2, confNbr);
            rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } catch (SQLException e) {
            throw new DataAccessException("findByConfirmation failed", e);
        } finally {
            ConnectionFactory.close(rs, ps, cn);
        }
    }

    public List findRecent(int custId, int max) {
        Connection cn = null; PreparedStatement ps = null; ResultSet rs = null;
        List out = new ArrayList();
        try {
            cn = ConnectionFactory.getConnection();
            ps = cn.prepareStatement(SELECT + "WHERE X.CUST_ID = ? ORDER BY X.CRT_TS DESC, X.XFR_ID DESC");
            ps.setMaxRows(max);
            ps.setInt(1, custId);
            rs = ps.executeQuery();
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (SQLException e) {
            throw new DataAccessException("findRecent failed", e);
        } finally {
            ConnectionFactory.close(rs, ps, cn);
        }
    }

    /** Sum of external transfers (EXS/EXN) created by this customer on the given calendar day, excluding rejected. */
    public long sumExternalForDay(Connection cn, int custId, Date day) throws SQLException {
        PreparedStatement ps = null; ResultSet rs = null;
        try {
            ps = cn.prepareStatement("SELECT COALESCE(SUM(AMT_CENTS),0) FROM OLB_XFR WHERE CUST_ID = ? "
                + "AND XFR_TYP_CD IN ('EXS','EXN') AND STAT_CD <> 'R' AND SCHED_DT = ?");
            ps.setInt(1, custId);
            ps.setDate(2, day);
            rs = ps.executeQuery();
            rs.next();
            return rs.getLong(1);
        } finally {
            ConnectionFactory.close(rs, ps, null);
        }
    }

    /** Count of outbound transfers from an account within the calendar month of the given day. */
    public int countOutboundInMonth(Connection cn, String fromAcctId, Date monthStart, Date nextMonthStart) throws SQLException {
        PreparedStatement ps = null; ResultSet rs = null;
        try {
            ps = cn.prepareStatement("SELECT COUNT(*) FROM OLB_XFR WHERE FROM_ACCT_ID = ? AND STAT_CD <> 'R' "
                + "AND SCHED_DT >= ? AND SCHED_DT < ?");
            ps.setString(1, fromAcctId);
            ps.setDate(2, monthStart);
            ps.setDate(3, nextMonthStart);
            rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } finally {
            ConnectionFactory.close(rs, ps, null);
        }
    }

    /** Next confirmation sequence for the day: XFRyyMMdd-nnnnnn */
    public int nextConfirmationSeq(Connection cn, Date day) throws SQLException {
        PreparedStatement ps = null; ResultSet rs = null;
        try {
            ps = cn.prepareStatement("SELECT LAST_SEQ FROM OLB_CONF_SEQ WHERE SEQ_DT = ?");
            ps.setDate(1, day);
            rs = ps.executeQuery();
            int next;
            if (rs.next()) {
                next = rs.getInt(1) + 1;
                ConnectionFactory.close(rs, ps, null);
                ps = cn.prepareStatement("UPDATE OLB_CONF_SEQ SET LAST_SEQ = ? WHERE SEQ_DT = ?");
                ps.setInt(1, next);
                ps.setDate(2, day);
            } else {
                next = 1;
                ConnectionFactory.close(rs, ps, null);
                ps = cn.prepareStatement("INSERT INTO OLB_CONF_SEQ (SEQ_DT, LAST_SEQ) VALUES (?, ?)");
                ps.setDate(1, day);
                ps.setInt(2, next);
            }
            ps.executeUpdate();
            return next;
        } finally {
            ConnectionFactory.close(rs, ps, null);
        }
    }

    private Transfer map(ResultSet rs) throws SQLException {
        Transfer t = new Transfer();
        t.setXfrId(rs.getInt("XFR_ID"));
        t.setConfirmationNumber(rs.getString("CONF_NBR"));
        t.setCustId(rs.getInt("CUST_ID"));
        t.setFromAcctId(rs.getString("FROM_ACCT_ID"));
        t.setToAcctId(rs.getString("TO_ACCT_ID"));
        t.setAmountCents(rs.getLong("AMT_CENTS"));
        t.setFeeCents(rs.getLong("FEE_CENTS"));
        t.setTypeCode(rs.getString("XFR_TYP_CD"));
        t.setTierCode(rs.getString("REL_TIER_CD"));
        t.setFrequencyCode(rs.getString("FREQ_CD"));
        t.setScheduledDate(rs.getDate("SCHED_DT"));
        t.setPostDate(rs.getDate("POST_DT"));
        t.setStatusCode(rs.getString("STAT_CD"));
        t.setMemo(rs.getString("MEMO"));
        t.setCreatedTs(rs.getTimestamp("CRT_TS"));
        t.setFromDisplay(label(rs.getString("F_BK"), rs.getString("F_NM"), rs.getString("F_L4")));
        t.setToDisplay(label(rs.getString("T_BK"), rs.getString("T_NM"), rs.getString("T_L4")));
        return t;
    }

    private String label(String bank, String name, String l4) {
        if (name == null) return "(closed account)";
        return (bank != null ? bank + " - " : "") + name + " ..." + l4;
    }
}
