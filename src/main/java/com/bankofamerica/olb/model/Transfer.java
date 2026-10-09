package com.bankofamerica.olb.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import com.bankofamerica.olb.util.Money;

public class Transfer implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TYPE_INTERNAL      = "INT";
    public static final String TYPE_EXT_STANDARD  = "EXS";
    public static final String TYPE_EXT_NEXT_DAY  = "EXN";

    public static final String STATUS_POSTED    = "P";
    public static final String STATUS_SCHEDULED = "S";
    public static final String STATUS_REJECTED  = "R";

    private int xfrId;
    private String confirmationNumber;
    private int custId;
    private String fromAcctId;
    private String toAcctId;
    private long amountCents;
    private long feeCents;
    private String typeCode;
    private String tierCode;
    private String frequencyCode;
    private Date scheduledDate;
    private Date postDate;
    private String statusCode;
    private String memo;
    private Timestamp createdTs;

    // display-only (joined in DAO)
    private String fromDisplay;
    private String toDisplay;

    public int getXfrId() { return xfrId; }
    public void setXfrId(int xfrId) { this.xfrId = xfrId; }
    public String getConfirmationNumber() { return confirmationNumber; }
    public void setConfirmationNumber(String confirmationNumber) { this.confirmationNumber = confirmationNumber; }
    public int getCustId() { return custId; }
    public void setCustId(int custId) { this.custId = custId; }
    public String getFromAcctId() { return fromAcctId; }
    public void setFromAcctId(String fromAcctId) { this.fromAcctId = fromAcctId; }
    public String getToAcctId() { return toAcctId; }
    public void setToAcctId(String toAcctId) { this.toAcctId = toAcctId; }
    public long getAmountCents() { return amountCents; }
    public void setAmountCents(long amountCents) { this.amountCents = amountCents; }
    public long getFeeCents() { return feeCents; }
    public void setFeeCents(long feeCents) { this.feeCents = feeCents; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getTierCode() { return tierCode; }
    public void setTierCode(String tierCode) { this.tierCode = tierCode; }
    public String getFrequencyCode() { return frequencyCode; }
    public void setFrequencyCode(String frequencyCode) { this.frequencyCode = frequencyCode; }
    public Date getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Date scheduledDate) { this.scheduledDate = scheduledDate; }
    public Date getPostDate() { return postDate; }
    public void setPostDate(Date postDate) { this.postDate = postDate; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public Timestamp getCreatedTs() { return createdTs; }
    public void setCreatedTs(Timestamp createdTs) { this.createdTs = createdTs; }
    public String getFromDisplay() { return fromDisplay; }
    public void setFromDisplay(String fromDisplay) { this.fromDisplay = fromDisplay; }
    public String getToDisplay() { return toDisplay; }
    public void setToDisplay(String toDisplay) { this.toDisplay = toDisplay; }

    public long getTotalDebitCents() { return amountCents + feeCents; }
    public String getAmountDisplay() { return Money.format(amountCents); }
    public String getFeeDisplay() { return Money.format(feeCents); }
    public String getTotalDebitDisplay() { return Money.format(getTotalDebitCents()); }
    public boolean isExternal() { return !TYPE_INTERNAL.equals(typeCode); }
}
