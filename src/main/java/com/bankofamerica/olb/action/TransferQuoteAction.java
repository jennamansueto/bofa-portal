package com.bankofamerica.olb.action;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;

import com.bankofamerica.olb.form.TransferForm;
import com.bankofamerica.olb.model.Customer;
import com.bankofamerica.olb.service.TransferQuote;
import com.bankofamerica.olb.service.TransferValidationException;

/**
 * XMLHttpRequest endpoint used by olb.js to refresh the Transfer Summary as
 * the customer edits the form.  Hand-rolled JSON (no JSON library in the
 * approved component list as of 2011).
 */
public class TransferQuoteAction extends BaseAction {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        Customer cust = currentCustomer(request);
        TransferForm f = (TransferForm) form;
        MessageResources res = getResources(request);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        try {
            TransferQuote q = transferService().quote(cust, f.getFromAcctId(), f.getToAcctId(), f.getAmount(),
                f.getTierCode(), f.getDelivery(), f.getFrequency(), f.getScheduledDate());
            SimpleDateFormat df = new SimpleDateFormat("EEE, MMM d, yyyy");
            out.print("{\"ok\":true");
            out.print(",\"from\":\"" + js(q.getFromDisplay()) + "\"");
            out.print(",\"to\":\"" + js(q.getToDisplay()) + "\"");
            out.print(",\"amount\":\"" + js(q.getAmountDisplay()) + "\"");
            out.print(",\"fee\":\"" + js(q.getFeeDisplay()) + "\"");
            out.print(",\"total\":\"" + js(q.getTotalDebitDisplay()) + "\"");
            out.print(",\"type\":\"" + js(res.getMessage("xfrtype." + q.getTypeCode())) + "\"");
            out.print(",\"tier\":\"" + js(res.getMessage("tier." + q.getTierCode())) + "\"");
            out.print(",\"delivery\":\"" + js(df.format(q.getDeliveryDate())) + "\"");
            out.print("}");
        } catch (TransferValidationException e) {
            String msg = e.getArgs() == null ? res.getMessage(e.getKey()) : res.getMessage(e.getKey(), e.getArgs());
            out.print("{\"ok\":false,\"message\":\"" + js(msg) + "\"}");
        }
        out.flush();
        return null;
    }

    private static String js(String s) { return StringEscapeUtils.escapeJavaScript(s == null ? "" : s); }
}
