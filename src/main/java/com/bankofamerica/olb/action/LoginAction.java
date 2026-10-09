package com.bankofamerica.olb.action;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.bankofamerica.olb.form.LoginForm;
import com.bankofamerica.olb.service.AuthService;
import com.bankofamerica.olb.web.AuthFilter;

public class LoginAction extends BaseAction {

    private static final Logger LOG = Logger.getLogger(LoginAction.class);
    public static final String COOKIE_SAVED_USER = "olb_uid";

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        LoginForm f = (LoginForm) form;
        ActionMessages errors = new ActionMessages();

        if (isBlank(f.getUserId()) || isBlank(f.getPassword())) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.login.required"));
            saveErrors(request, errors);
            return mapping.findForward("failure");
        }

        AuthService.Outcome o = new AuthService().authenticate(f.getUserId().trim(), f.getPassword());
        if (o.result == AuthService.RESULT_LOCKED) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.login.locked"));
        } else if (o.result != AuthService.RESULT_OK) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.login.invalid"));
        }
        if (!errors.isEmpty()) {
            LOG.warn("Sign-in failed for user " + f.getUserId());
            saveErrors(request, errors);
            return mapping.findForward("failure");
        }

        // session fixation defence: new session on sign-in
        HttpSession old = request.getSession(false);
        if (old != null) old.invalidate();
        HttpSession s = request.getSession(true);
        s.setAttribute(AuthFilter.SESSION_CUSTOMER, o.customer);

        Cookie c = new Cookie(COOKIE_SAVED_USER, f.isSaveUserId() ? o.customer.getUserId() : "");
        c.setMaxAge(f.isSaveUserId() ? 60 * 60 * 24 * 365 : 0);
        c.setPath(request.getContextPath().length() == 0 ? "/" : request.getContextPath());
        response.addCookie(c);

        LOG.info("Sign-in OK cust=" + o.customer.getCustId());
        return mapping.findForward("success");
    }

    private static boolean isBlank(String s) { return s == null || s.trim().length() == 0; }
}
