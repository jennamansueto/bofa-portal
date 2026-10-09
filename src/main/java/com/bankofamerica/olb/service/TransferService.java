package com.bankofamerica.olb.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Set;

import org.apache.log4j.Logger;

import com.bankofamerica.olb.dao.AccountDAO;
import com.bankofamerica.olb.dao.ConnectionFactory;
import com.bankofamerica.olb.dao.DataAccessException;
import com.bankofamerica.olb.dao.FeeScheduleDAO;
import com.bankofamerica.olb.dao.TransferDAO;
import com.bankofamerica.olb.model.Account;
import com.bankofamerica.olb.model.Customer;
import com.bankofamerica.olb.model.Transfer;
import com.bankofamerica.olb.util.Money;

/**
 * Transfer orchestration.  Mirrors the edit rules in CICS program OLBX020 so
 * that the web channel rejects what the host would reject (OLB-1877).
 *
 * Rule summary (keep in sync with OLBX020 and the fee table OLB_FEE_SCHED):
 *   R1  amount > 0, max two decimals
 *   R2  from != to, neither may be closed, not EXT -> EXT
 *   R3  type derived from the TO account: INT when both accounts are ours,
 *       otherwise EXS/EXN per the customer's delivery choice
 *   R4  fee + limits from OLB_FEE_SCHED by (type, tier)
 *   R5  amount + fee <= available balance of FROM (internal accounts only)
 *   R6  per-transaction limit and daily external limit (sum of today's EXS+EXN)
 *   R7  Reg D: max 6 outbound transfers per calendar month from a SAV account
 *   R8  INT scheduled for today posts immediately (same day); future-dated INT
 *       is held as 'S' until the nightly TRNPOST batch
 *   R9  EXS delivers in 3 business days, EXN next business day, both from the
 *       effective start date (after 8 PM ET cutoff -> next business day);
 *       EXS/EXN always 'S' with an available-balance hold on FROM
 *   R10 confirmation number XFRyyMMdd-nnnnnn, sequence resets daily
 */
public class TransferService {

    private static final Logger LOG = Logger.getLogger(TransferService.class);
    public static final int REG_D_MONTHLY_LIMIT = 6;
    public static final long INTERNAL_PER_TXN_CAP_CENTS = 9999999L; // $99,999.99

    private final AccountDAO accountDAO = new AccountDAO();
    private final TransferDAO transferDAO = new TransferDAO();
    private final FeeScheduleDAO feeDAO = new FeeScheduleDAO();
    private final int cutoffHour;

    public TransferService(int cutoffHour) {
        this.cutoffHour = cutoffHour;
    }

    public BusinessCalendar calendar(Connection cn) throws SQLException {
        Set hol = feeDAO.loadHolidays(cn);
        return new BusinessCalendar(hol, cutoffHour);
    }

    /** Prices and dates a transfer without persisting.  Throws on edit failure. */
    public TransferQuote quote(Customer cust, String fromId, String toId, String amountRaw, String tierCode,
                               String delivery, String frequency, String scheduledRaw) throws TransferValidationException {
        Connection cn = null;
        try {
            cn = ConnectionFactory.getConnection();
            return buildQuote(cn, cust, fromId, toId, amountRaw, tierCode, delivery, frequency, scheduledRaw, false);
        } catch (SQLException e) {
            throw new DataAccessException("quote failed", e);
        } finally {
            ConnectionFactory.close(null, null, cn);
        }
    }

    /** Validates, prices and persists the transfer.  Returns the stored row. */
    public Transfer submit(Customer cust, String fromId, String toId, String amountRaw, String tierCode,
                           String delivery, String frequency, String scheduledRaw, String memo) throws TransferValidationException {
        Connection cn = null;
        try {
            cn = ConnectionFactory.getConnection();
            cn.setAutoCommit(false);
            TransferQuote q = buildQuote(cn, cust, fromId, toId, amountRaw, tierCode, delivery, frequency, scheduledRaw, true);
            BusinessCalendar cal = calendar(cn);
            Date today = cal.today();

            Transfer t = new Transfer();
            t.setCustId(cust.getCustId());
            t.setFromAcctId(fromId);
            t.setToAcctId(toId);
            t.setAmountCents(q.getAmountCents());
            t.setFeeCents(q.getFeeCents());
            t.setTypeCode(q.getTypeCode());
            t.setTierCode(q.getTierCode());
            t.setFrequencyCode(frequency);
            t.setScheduledDate(q.getScheduledDate());
            t.setPostDate(q.getDeliveryDate());
            t.setMemo(memo == null || memo.trim().length() == 0 ? null : memo.trim());
            t.setCreatedTs(new Timestamp(System.currentTimeMillis()));
            t.setConfirmationNumber(buildConfirmationNumber(cn, today));

            boolean postNow = Transfer.TYPE_INTERNAL.equals(q.getTypeCode())
                && BusinessCalendar.isSameDay(q.getScheduledDate(), today);
            if (postNow) {
                t.setStatusCode(Transfer.STATUS_POSTED);
                accountDAO.adjustBalances(cn, fromId, -t.getTotalDebitCents(), -t.getTotalDebitCents());
                accountDAO.adjustBalances(cn, toId, t.getAmountCents(), t.getAmountCents());
            } else {
                t.setStatusCode(Transfer.STATUS_SCHEDULED);
                if (t.isExternal()) {
                    Account from = accountDAO.findById(cn, fromId);
                    if (!from.isExternal()) {
                        // ACH debit: hold available funds now, ledger balance moves at settlement
                        accountDAO.adjustBalances(cn, fromId, 0, -t.getTotalDebitCents());
                    }
                }
            }
            transferDAO.insert(cn, t);
            cn.commit();
            LOG.info("Transfer " + t.getConfirmationNumber() + " " + t.getStatusCode() + " cust=" + cust.getCustId()
                + " " + fromId + "->" + toId + " amt=" + t.getAmountCents() + " fee=" + t.getFeeCents() + " typ=" + t.getTypeCode());
            return t;
        } catch (SQLException e) {
            rollback(cn);
            throw new DataAccessException("submit failed", e);
        } catch (TransferValidationException e) {
            rollback(cn);
            throw e;
        } finally {
            if (cn != null) try { cn.setAutoCommit(true); } catch (SQLException ignore) { }
            ConnectionFactory.close(null, null, cn);
        }
    }

    private TransferQuote buildQuote(Connection cn, Customer cust, String fromId, String toId, String amountRaw,
                                     String tierCode, String delivery, String frequency, String scheduledRaw,
                                     boolean forUpdate) throws SQLException, TransferValidationException {
        // R1
        if (amountRaw == null || amountRaw.trim().length() == 0) throw new TransferValidationException("error.xfr.amount.required");
        Long cents = Money.parseToCents(amountRaw);
        if (cents == null) throw new TransferValidationException("error.xfr.amount.invalid");
        if (cents.longValue() <= 0) throw new TransferValidationException("error.xfr.amount.min");

        // R2
        if (fromId == null || toId == null) throw new TransferValidationException("error.xfr.acct.invalid");
        if (fromId.equals(toId)) throw new TransferValidationException("error.xfr.sameacct");
        Account from = accountDAO.findById(cn, fromId);
        Account to = accountDAO.findById(cn, toId);
        if (from == null || to == null || from.getCustId() != cust.getCustId() || to.getCustId() != cust.getCustId()) {
            throw new TransferValidationException("error.xfr.acct.invalid");
        }
        if (from.isExternal() && to.isExternal()) throw new TransferValidationException("error.xfr.ext2ext");

        if (!FeeSchedule.isValidTier(tierCode)) throw new TransferValidationException("error.xfr.tier.invalid");
        if (frequency == null || !(frequency.equals("O") || frequency.equals("W") || frequency.equals("M"))) {
            throw new TransferValidationException("error.xfr.frequency.invalid");
        }

        BusinessCalendar cal = calendar(cn);
        Date today = cal.today();
        Date sched = parseScheduled(scheduledRaw, today);
        if (sched.before(today)) throw new TransferValidationException("error.xfr.date.past");

        // R3
        String type;
        if (!from.isExternal() && !to.isExternal()) {
            type = Transfer.TYPE_INTERNAL;
        } else {
            type = "EXN".equals(delivery) ? Transfer.TYPE_EXT_NEXT_DAY : Transfer.TYPE_EXT_STANDARD;
        }

        // R4
        FeeSchedule.Entry fee = feeDAO.lookup(cn, type, tierCode);
        if (fee == null) throw new TransferValidationException("error.xfr.tier.invalid");

        // R6 per-txn
        long perTxn = Transfer.TYPE_INTERNAL.equals(type) ? INTERNAL_PER_TXN_CAP_CENTS : fee.perTxnLimitCents;
        if (cents.longValue() > perTxn) {
            throw new TransferValidationException("error.xfr.pertxn", new Object[] { Money.format(perTxn) });
        }
        // R6 daily external
        if (!Transfer.TYPE_INTERNAL.equals(type)) {
            long usedToday = transferDAO.sumExternalForDay(cn, cust.getCustId(), today);
            if (BusinessCalendar.isSameDay(sched, today) && usedToday + cents.longValue() > fee.dailyLimitCents) {
                throw new TransferValidationException("error.xfr.daily",
                    new Object[] { Money.format(fee.dailyLimitCents), Money.format(usedToday) });
            }
        }
        // R5
        if (!from.isExternal() && cents.longValue() + fee.feeCents > from.getAvailableBalanceCents()) {
            throw new TransferValidationException("error.xfr.nsf");
        }
        // R7
        if (from.isSavings()) {
            int n = transferDAO.countOutboundInMonth(cn, fromId, BusinessCalendar.firstOfMonth(sched), BusinessCalendar.firstOfNextMonth(sched));
            if (n >= REG_D_MONTHLY_LIMIT) throw new TransferValidationException("error.xfr.regd");
        }

        // R8 / R9
        Date delivery_dt;
        if (Transfer.TYPE_INTERNAL.equals(type)) {
            delivery_dt = sched;
        } else {
            boolean afterCutoff = BusinessCalendar.isSameDay(sched, today) && cal.isAfterCutoff();
            Date start = cal.effectiveStartDate(sched, afterCutoff);
            int days = Transfer.TYPE_EXT_NEXT_DAY.equals(type) ? 1 : 3;
            delivery_dt = cal.addBusinessDays(start, days);
        }

        TransferQuote q = new TransferQuote();
        q.setTypeCode(type);
        q.setAmountCents(cents.longValue());
        q.setFeeCents(fee.feeCents);
        q.setScheduledDate(sched);
        q.setDeliveryDate(delivery_dt);
        q.setTierCode(tierCode);
        q.setFromDisplay(from.getDisplayName());
        q.setToDisplay(to.getDisplayName());
        return q;
    }

    private Date parseScheduled(String raw, Date today) throws TransferValidationException {
        if (raw == null || raw.trim().length() == 0) return today;
        SimpleDateFormat f = new SimpleDateFormat("MM/dd/yyyy");
        f.setLenient(false);
        f.setTimeZone(BusinessCalendar.EASTERN);
        try {
            java.util.Date d = f.parse(raw.trim());
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd");
            iso.setTimeZone(BusinessCalendar.EASTERN);
            return Date.valueOf(iso.format(d));
        } catch (java.text.ParseException e) {
            throw new TransferValidationException("error.xfr.date.invalid");
        }
    }

    private String buildConfirmationNumber(Connection cn, Date today) throws SQLException {
        int seq = transferDAO.nextConfirmationSeq(cn, today);
        SimpleDateFormat f = new SimpleDateFormat("yyMMdd");
        f.setTimeZone(BusinessCalendar.EASTERN);
        String n = String.valueOf(seq);
        while (n.length() < 6) n = "0" + n;
        return "XFR" + f.format(today) + "-" + n;
    }

    private void rollback(Connection cn) {
        if (cn != null) try { cn.rollback(); } catch (SQLException ignore) { }
    }
}
