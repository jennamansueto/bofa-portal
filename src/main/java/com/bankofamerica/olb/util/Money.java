package com.bankofamerica.olb.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Money helpers.  All amounts are carried as long cents to match the
 * COMP-3 S9(13)V99 fields on the host (see copybook TRNREC).
 */
public final class Money {

    private Money() {}

    /** Formats cents as "$4,215.38".  Negative values render as "-$4,215.38". */
    public static String format(long cents) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        String s = nf.format(new BigDecimal(cents).movePointLeft(2));
        // JDK 6/7 currency format renders negatives as ($1.00); product wants -$1.00
        if (s.startsWith("(") && s.endsWith(")")) {
            s = "-" + s.substring(1, s.length() - 1);
        }
        return s;
    }

    /** Formats cents without the currency sign, e.g. 500.00 */
    public static String formatPlain(long cents) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(new BigDecimal(cents).movePointLeft(2));
    }

    /**
     * Parses user input such as "$1,250.5" to cents.  Returns null when the
     * input is not a valid amount or has more than two decimal places.
     */
    public static Long parseToCents(String raw) {
        if (raw == null) return null;
        String s = raw.trim().replace("$", "").replace(",", "");
        if (s.length() == 0) return null;
        if (!s.matches("-?\\d{1,13}(\\.\\d{0,2})?")) return null;
        try {
            BigDecimal bd = new BigDecimal(s).setScale(2, RoundingMode.UNNECESSARY);
            return Long.valueOf(bd.movePointRight(2).longValueExact());
        } catch (ArithmeticException e) {
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
