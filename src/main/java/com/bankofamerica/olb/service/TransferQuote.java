package com.bankofamerica.olb.service;

import java.io.Serializable;
import java.sql.Date;

import com.bankofamerica.olb.util.Money;

/** Priced, dated preview of a transfer before (or after) submission. */
public class TransferQuote implements Serializable {
    private static final long serialVersionUID = 1L;

    private String typeCode;
    private long amountCents;
    private long feeCents;
    private Date scheduledDate;
    private Date deliveryDate;
    private String tierCode;
    private String fromDisplay;
    private String toDisplay;

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public long getAmountCents() { return amountCents; }
    public void setAmountCents(long amountCents) { this.amountCents = amountCents; }
    public long getFeeCents() { return feeCents; }
    public void setFeeCents(long feeCents) { this.feeCents = feeCents; }
    public Date getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Date scheduledDate) { this.scheduledDate = scheduledDate; }
    public Date getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(Date deliveryDate) { this.deliveryDate = deliveryDate; }
    public String getTierCode() { return tierCode; }
    public void setTierCode(String tierCode) { this.tierCode = tierCode; }
    public String getFromDisplay() { return fromDisplay; }
    public void setFromDisplay(String fromDisplay) { this.fromDisplay = fromDisplay; }
    public String getToDisplay() { return toDisplay; }
    public void setToDisplay(String toDisplay) { this.toDisplay = toDisplay; }

    public long getTotalDebitCents() { return amountCents + feeCents; }
    public String getAmountDisplay() { return Money.format(amountCents); }
    public String getFeeDisplay() { return feeCents == 0 ? "No fee" : Money.format(feeCents); }
    public String getTotalDebitDisplay() { return Money.format(getTotalDebitCents()); }
}
