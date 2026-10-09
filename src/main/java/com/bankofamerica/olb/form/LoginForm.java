package com.bankofamerica.olb.form;

import org.apache.struts.action.ActionForm;

public class LoginForm extends ActionForm {
    private static final long serialVersionUID = 1L;
    private String userId;
    private String password;
    private boolean saveUserId;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isSaveUserId() { return saveUserId; }
    public void setSaveUserId(boolean saveUserId) { this.saveUserId = saveUserId; }
}
