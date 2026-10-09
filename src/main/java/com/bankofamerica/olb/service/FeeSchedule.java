package com.bankofamerica.olb.service;

import java.io.Serializable;

/** Fee / limit row from OLB_FEE_SCHED. */
public final class FeeSchedule {

    private FeeSchedule() {}

    public static final String TIER_NONE            = "00";
    public static final String TIER_GOLD            = "10";
    public static final String TIER_PLATINUM        = "20";
    public static final String TIER_PLATINUM_HONORS = "30";

    public static final String[] TIER_CODES = { TIER_NONE, TIER_GOLD, TIER_PLATINUM, TIER_PLATINUM_HONORS };

    public static boolean isValidTier(String code) {
        for (int i = 0; i < TIER_CODES.length; i++) if (TIER_CODES[i].equals(code)) return true;
        return false;
    }

    public static class Entry implements Serializable {
        private static final long serialVersionUID = 1L;
        public final long feeCents;
        public final long dailyLimitCents;
        public final long perTxnLimitCents;
        public Entry(long feeCents, long dailyLimitCents, long perTxnLimitCents) {
            this.feeCents = feeCents;
            this.dailyLimitCents = dailyLimitCents;
            this.perTxnLimitCents = perTxnLimitCents;
        }
    }
}
