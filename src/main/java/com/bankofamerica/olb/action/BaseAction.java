package com.bankofamerica.olb.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.Action;

import com.bankofamerica.olb.model.Customer;
import com.bankofamerica.olb.service.TransferService;
import com.bankofamerica.olb.web.AuthFilter;
import com.bankofamerica.olb.web.StartupListener;

public abstract class BaseAction extends Action {

    protected Customer currentCustomer(HttpServletRequest request) {
        return (Customer) request.getSession().getAttribute(AuthFilter.SESSION_CUSTOMER);
    }

    protected TransferService transferService() {
        return (TransferService) getServlet().getServletContext().getAttribute(StartupListener.ATTR_TRANSFER_SERVICE);
    }
}
