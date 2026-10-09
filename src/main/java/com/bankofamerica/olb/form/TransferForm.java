package com.bankofamerica.olb.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class TransferForm extends ActionForm {
    private static final long serialVersionUID = 1L;

    private String fromAcctId;
    private String toAcctId;
    private String amount;
    private String tierCode;
    private String delivery = "EXS";
    private String frequency = "O";
    private String scheduledDate;
    private String memo;

    public String getFromAcctId() { return fromAcctId; }
    public void setFromAcctId(String v) { this.fromAcctId = v; }
    public String getToAcctId() { return toAcctId; }
    public void setToAcctId(String v) { this.toAcctId = v; }
    public String getAmount() { return amount; }
    public void setAmount(String v) { this.amount = v; }
    public String getTierCode() { return tierCode; }
    public void setTierCode(String v) { this.tierCode = v; }
    public String getDelivery() { return delivery; }
    public void setDelivery(String v) { this.delivery = v; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String v) { this.frequency = v; }
    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String v) { this.scheduledDate = v; }
    public String getMemo() { return memo; }
    public void setMemo(String v) { this.memo = v; }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        // session-scoped: keep selections between round trips; nothing to reset
    }
}
