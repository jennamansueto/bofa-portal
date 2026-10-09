package com.bankofamerica.olb.model;

import java.io.Serializable;

import com.bankofamerica.olb.util.Money;

public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TYPE_CHECKING = "DDA";
    public static final String TYPE_SAVINGS  = "SAV";
    public static final String TYPE_EXTERNAL = "EXT";

    private String acctId;
    private int custId;
    private String typeCode;
    private String productName;
    private String last4;
    private long currentBalanceCents;
    private long availableBalanceCents;
    private String externalBankName;
    private int seqNo;

    public String getAcctId() { return acctId; }
    public void setAcctId(String acctId) { this.acctId = acctId; }
    public int getCustId() { return custId; }
    public void setCustId(int custId) { this.custId = custId; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getLast4() { return last4; }
    public void setLast4(String last4) { this.last4 = last4; }
    public long getCurrentBalanceCents() { return currentBalanceCents; }
    public void setCurrentBalanceCents(long v) { this.currentBalanceCents = v; }
    public long getAvailableBalanceCents() { return availableBalanceCents; }
    public void setAvailableBalanceCents(long v) { this.availableBalanceCents = v; }
    public String getExternalBankName() { return externalBankName; }
    public void setExternalBankName(String externalBankName) { this.externalBankName = externalBankName; }
    public int getSeqNo() { return seqNo; }
    public void setSeqNo(int seqNo) { this.seqNo = seqNo; }

    public boolean isExternal() { return TYPE_EXTERNAL.equals(typeCode); }
    public boolean isSavings() { return TYPE_SAVINGS.equals(typeCode); }

    /** Display label, e.g. "Advantage Plus Banking - Checking ...1001" */
    public String getDisplayName() {
        StringBuffer sb = new StringBuffer();
        if (isExternal()) {
            sb.append(externalBankName).append(" - ");
        }
        sb.append(productName).append(" ...").append(last4);
        return sb.toString();
    }
    public String getCurrentBalanceDisplay() { return Money.format(currentBalanceCents); }
    public String getAvailableBalanceDisplay() { return Money.format(availableBalanceCents); }
}
