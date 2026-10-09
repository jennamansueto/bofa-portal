package com.bankofamerica.olb.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.bankofamerica.olb.service.FeeSchedule;

public class FeeScheduleDAO {

    public FeeSchedule.Entry lookup(Connection cn, String typeCode, String tierCode) throws SQLException {
        PreparedStatement ps = null; ResultSet rs = null;
        try {
            ps = cn.prepareStatement("SELECT FEE_CENTS, DAILY_LIM_CENTS, PER_TXN_LIM_CENTS FROM OLB_FEE_SCHED "
                + "WHERE XFR_TYP_CD = ? AND REL_TIER_CD = ? AND EFF_DT <= CURRENT_DATE");
            ps.setString(1, typeCode);
            ps.setString(2, tierCode);
            rs = ps.executeQuery();
            if (!rs.next()) return null;
            return new FeeSchedule.Entry(rs.getLong(1), rs.getLong(2), rs.getLong(3));
        } finally {
            ConnectionFactory.close(rs, ps, null);
        }
    }

    public Set loadHolidays(Connection cn) throws SQLException {
        PreparedStatement ps = null; ResultSet rs = null;
        Set out = new HashSet();
        try {
            ps = cn.prepareStatement("SELECT HOL_DT FROM OLB_BANK_HOL");
            rs = ps.executeQuery();
            while (rs.next()) {
                Date d = rs.getDate(1);
                out.add(d.toString());
            }
            return out;
        } finally {
            ConnectionFactory.close(rs, ps, null);
        }
    }
}
