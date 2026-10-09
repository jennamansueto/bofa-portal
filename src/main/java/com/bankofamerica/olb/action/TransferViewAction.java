package com.bankofamerica.olb.action;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.bankofamerica.olb.dao.AccountDAO;
import com.bankofamerica.olb.dao.TransferDAO;
import com.bankofamerica.olb.form.TransferForm;
import com.bankofamerica.olb.model.Account;
import com.bankofamerica.olb.model.Customer;
import com.bankofamerica.olb.service.TransferQuote;
import com.bankofamerica.olb.service.TransferValidationException;

/** Renders the Transfer Money page with the customer's accounts and a priced summary of the current form. */
public class TransferViewAction extends BaseAction {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        Customer cust = currentCustomer(request);
        TransferForm f = (TransferForm) form;
        List accounts = new AccountDAO().findByCustomer(cust.getCustId());

        if (f.getFromAcctId() == null || f.getToAcctId() == null) {
            // defaults: first non-external as From, second as To
            Account first = null, second = null;
            for (Iterator it = accounts.iterator(); it.hasNext();) {
                Account a = (Account) it.next();
                if (a.isExternal()) continue;
                if (first == null) first = a; else if (second == null) second = a;
            }
            if (first != null) f.setFromAcctId(first.getAcctId());
            if (second != null) f.setToAcctId(second.getAcctId());
            if (f.getAmount() == null) f.setAmount("500");
        }
        if (f.getTierCode() == null) f.setTierCode(cust.getTierCode());

        TransferQuote quote = null;
        try {
            quote = transferService().quote(cust, f.getFromAcctId(), f.getToAcctId(), f.getAmount(), f.getTierCode(),
                f.getDelivery(), f.getFrequency(), f.getScheduledDate());
        } catch (TransferValidationException e) {
            // summary simply shows the raw inputs when the draft does not price
        }

        request.setAttribute("accounts", accounts);
        request.setAttribute("quote", quote);
        request.setAttribute("recentTransfers", new TransferDAO().findRecent(cust.getCustId(), 10));
        return mapping.findForward("success");
    }
}
