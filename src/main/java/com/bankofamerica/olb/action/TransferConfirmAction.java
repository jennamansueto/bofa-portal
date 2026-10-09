package com.bankofamerica.olb.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.bankofamerica.olb.dao.AccountDAO;
import com.bankofamerica.olb.dao.TransferDAO;
import com.bankofamerica.olb.model.Customer;
import com.bankofamerica.olb.model.Transfer;

public class TransferConfirmAction extends BaseAction {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        Customer cust = currentCustomer(request);
        String conf = request.getParameter("conf");
        if (conf == null) conf = (String) request.getSession().getAttribute("olb.lastConfirmation");
        Transfer t = conf == null ? null : new TransferDAO().findByConfirmation(cust.getCustId(), conf);
        if (t == null) return mapping.findForward("home");
        request.setAttribute("transfer", t);
        request.setAttribute("accounts", new AccountDAO().findByCustomer(cust.getCustId()));
        return mapping.findForward("success");
    }
}
