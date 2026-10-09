package com.bankofamerica.olb.service;

import java.sql.Date;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TimeZone;

/**
 * Business-day arithmetic.  The bank operates on Eastern time; the posting
 * cutoff (default 8:00 PM ET) is applied before computing delivery dates for
 * ACH transfers.  Weekends and OLB_BANK_HOL dates are not business days.
 */
public class BusinessCalendar {

    public static final TimeZone EASTERN = TimeZone.getTimeZone("America/New_York");

    private final Set holidayIsoDates;   // Set<String> of yyyy-MM-dd
    private final int cutoffHour;

    public BusinessCalendar(Set holidayIsoDates, int cutoffHour) {
        this.holidayIsoDates = holidayIsoDates == null ? Collections.EMPTY_SET : holidayIsoDates;
        this.cutoffHour = cutoffHour;
    }

    /** Today's calendar date in Eastern time. */
    public Date today() {
        Calendar c = new GregorianCalendar(EASTERN);
        return toSqlDate(c);
    }

    /** True when the current Eastern time is at or past the cutoff hour. */
    public boolean isAfterCutoff() {
        Calendar c = new GregorianCalendar(EASTERN);
        return c.get(Calendar.HOUR_OF_DAY) >= cutoffHour;
    }

    public boolean isBusinessDay(Date d) {
        Calendar c = new GregorianCalendar(EASTERN);
        c.setTime(d);
        int dow = c.get(Calendar.DAY_OF_WEEK);
        if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) return false;
        return !holidayIsoDates.contains(d.toString());
    }

    /** Next business day strictly after d. */
    public Date nextBusinessDay(Date d) {
        return addBusinessDays(d, 1);
    }

    /** Roll forward to d itself if it is a business day, otherwise the next one. */
    public Date rollForward(Date d) {
        Date x = d;
        while (!isBusinessDay(x)) x = addDays(x, 1);
        return x;
    }

    public Date addBusinessDays(Date d, int n) {
        Date x = d;
        int added = 0;
        while (added < n) {
            x = addDays(x, 1);
            if (isBusinessDay(x)) added++;
        }
        return x;
    }

    /**
     * Effective start date for an external transfer requested on requestDate:
     * if requested after cutoff, or on a non-business day, the clock starts on
     * the next business day.
     */
    public Date effectiveStartDate(Date requestDate, boolean afterCutoff) {
        Date start = requestDate;
        if (afterCutoff && isSameDay(start, today())) start = addDays(start, 1);
        return rollForward(start);
    }

    public static Date addDays(Date d, int n) {
        Calendar c = new GregorianCalendar(EASTERN);
        c.setTime(d);
        c.add(Calendar.DAY_OF_MONTH, n);
        return toSqlDate(c);
    }

    public static Date firstOfMonth(Date d) {
        Calendar c = new GregorianCalendar(EASTERN);
        c.setTime(d);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return toSqlDate(c);
    }

    public static Date firstOfNextMonth(Date d) {
        Calendar c = new GregorianCalendar(EASTERN);
        c.setTime(d);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.MONTH, 1);
        return toSqlDate(c);
    }

    public static boolean isSameDay(Date a, Date b) {
        return a.toString().equals(b.toString());
    }

    private static Date toSqlDate(Calendar c) {
        return Date.valueOf(c.get(Calendar.YEAR) + "-" + pad(c.get(Calendar.MONTH) + 1) + "-" + pad(c.get(Calendar.DAY_OF_MONTH)));
    }

    private static String pad(int n) { return n < 10 ? "0" + n : String.valueOf(n); }
}
