package com.bankofamerica.olb.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    private int custId;
    private String userId;
    private String firstName;
    private String lastName;
    private String tierCode;
    private Timestamp lastLogin;
    private int failCount;
    private String statusCode;

    public int getCustId() { return custId; }
    public void setCustId(int custId) { this.custId = custId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getTierCode() { return tierCode; }
    public void setTierCode(String tierCode) { this.tierCode = tierCode; }
    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }
    public int getFailCount() { return failCount; }
    public void setFailCount(int failCount) { this.failCount = failCount; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public String getDisplayName() { return firstName + " " + lastName; }
}
