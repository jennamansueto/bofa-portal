<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ page import="java.util.*, com.bankofamerica.olb.model.*" %>
<%
  Transfer t = (Transfer) request.getAttribute("transfer");
  List accounts = (List) request.getAttribute("accounts");
  java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("EEEE, MMMM d, yyyy");
  java.text.SimpleDateFormat ts = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm a z");
  ts.setTimeZone(com.bankofamerica.olb.service.BusinessCalendar.EASTERN);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Transfer Confirmation - Bank of America Online Banking</title>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/olb.css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/olb.js"></script>
</head>
<body data-ctx="<%=request.getContextPath()%>">
<%@ include file="/WEB-INF/jsp/inc/olbHeader.jspf" %>
<div class="inner" id="crumbs"><a href="<%=request.getContextPath()%>/secure/transfer.do">Home</a><span class="sep">/</span><a href="<%=request.getContextPath()%>/secure/transfer.do">Accounts</a><span class="sep">/</span><a href="<%=request.getContextPath()%>/secure/transfer.do">Transfer Money</a><span class="sep">/</span><b>Confirmation</b></div>
<div class="inner" id="xfr"><table width="100%" cellpadding="0" cellspacing="0"><tr>
  <td class="lcol">
    <div class="confirm">
      <span class="check">&#10003;</span><h2><bean:message key="xfr.confirm.heading"/></h2>
      <div class="confnbr">Confirmation number: <b><%= t.getConfirmationNumber() %></b></div>
      <table class="summary" cellpadding="0" cellspacing="0">
        <tr><th colspan="2">TRANSFER DETAILS</th></tr>
        <tr><td>From</td><td class="v"><%= t.getFromDisplay() %></td></tr>
        <tr><td>To</td><td class="v"><%= t.getToDisplay() %></td></tr>
        <tr><td>Amount</td><td class="v"><%= t.getAmountDisplay() %></td></tr>
        <tr><td>Fee</td><td class="v"><%= t.getFeeCents() == 0 ? "No fee" : t.getFeeDisplay() %></td></tr>
        <tr><td>Transfer type</td><td class="v"><bean:message key='<%= "xfrtype." + t.getTypeCode() %>'/></td></tr>
        <tr><td>Relationship tier</td><td class="v"><bean:message key='<%= "tier." + t.getTierCode() %>'/></td></tr>
        <tr><td>Frequency</td><td class="v"><bean:message key='<%= "freq." + t.getFrequencyCode() %>'/></td></tr>
        <tr><td>Status</td><td class="v status-<%= t.getStatusCode() %>"><bean:message key='<%= "stat." + t.getStatusCode() %>'/></td></tr>
        <tr><td><%= Transfer.STATUS_POSTED.equals(t.getStatusCode()) ? "Posted" : "Delivery date" %></td><td class="v"><%= df.format(t.getPostDate()) %></td></tr>
        <% if (t.getMemo() != null) { %><tr><td>Memo</td><td class="v"><%= org.apache.commons.lang.StringEscapeUtils.escapeHtml(t.getMemo()) %></td></tr><% } %>
        <tr class="total"><td>Total debit</td><td class="v"><%= t.getTotalDebitDisplay() %></td></tr>
      </table>
      <p style="color:#555;font-size:14px;line-height:22px">
        <% if (t.isExternal()) { %><bean:message key="xfr.confirm.external"/><% } else { %><bean:message key="xfr.confirm.sameday"/><% } %>
        Submitted <%= ts.format(t.getCreatedTs()) %> via Online Banking.
      </p>
      <p style="margin-top:26px"><a class="btn-secondary" href="<%=request.getContextPath()%>/secure/transfer.do">Make another transfer</a> <a class="btn-secondary" href="#" onclick="window.print();return false;">Print</a></p>
    </div>
  </td>
  <td class="rcol">
    <h2 style="font-size:22px">Updated balances</h2>
    <% for (Iterator it = accounts.iterator(); it.hasNext();) { Account a = (Account) it.next(); if (a.isExternal()) continue; %>
    <div class="acct">
      <div class="bal"><%= a.getCurrentBalanceDisplay() %></div>
      <div class="nm"><%= a.getDisplayName() %></div>
      <div class="id"><%= a.getAcctId() %></div>
      <% if (a.getAvailableBalanceCents() != a.getCurrentBalanceCents()) { %><div class="avl">Available <%= a.getAvailableBalanceDisplay() %></div><% } %>
    </div>
    <% } %>
  </td>
</tr></table></div>
<%@ include file="/WEB-INF/jsp/inc/footer.jspf" %>
</body>
</html>
