package com.bankofamerica.olb.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.bankofamerica.olb.dao.AccountDAO;
import com.bankofamerica.olb.dao.TransferDAO;
import com.bankofamerica.olb.form.TransferForm;
import com.bankofamerica.olb.model.Customer;
import com.bankofamerica.olb.model.Transfer;
import com.bankofamerica.olb.service.TransferValidationException;

public class TransferSubmitAction extends BaseAction {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        Customer cust = currentCustomer(request);
        TransferForm f = (TransferForm) form;
        try {
            Transfer t = transferService().submit(cust, f.getFromAcctId(), f.getToAcctId(), f.getAmount(), f.getTierCode(),
                f.getDelivery(), f.getFrequency(), f.getScheduledDate(), f.getMemo());
            request.getSession().setAttribute("olb.lastConfirmation", t.getConfirmationNumber());
            // clear the draft for the next transfer but keep the tier choice
            f.setAmount(null);
            f.setMemo(null);
            f.setScheduledDate(null);
            return mapping.findForward("success");
        } catch (TransferValidationException e) {
            ActionMessages errors = new ActionMessages();
            errors.add(ActionMessages.GLOBAL_MESSAGE, e.getArgs() == null
                ? new ActionMessage(e.getKey())
                : new ActionMessage(e.getKey(), e.getArgs()));
            saveErrors(request, errors);
            request.setAttribute("accounts", new AccountDAO().findByCustomer(cust.getCustId()));
            request.setAttribute("recentTransfers", new TransferDAO().findRecent(cust.getCustId(), 10));
            return mapping.findForward("failure");
        }
    }
}
