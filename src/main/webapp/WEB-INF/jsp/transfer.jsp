<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="java.util.*, com.bankofamerica.olb.model.*, com.bankofamerica.olb.form.TransferForm, com.bankofamerica.olb.service.TransferQuote" %>
<%
  Customer cust = (Customer) session.getAttribute("olb.customer");
  List accounts = (List) request.getAttribute("accounts");
  TransferForm tf = (TransferForm) session.getAttribute("transferForm");
  TransferQuote quote = (TransferQuote) request.getAttribute("quote");
  java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("EEE, MMM d, yyyy");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
<title><bean:message key="app.transfer.title"/></title>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/olb.css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/olb.js"></script>
</head>
<body data-ctx="<%=request.getContextPath()%>">
<%@ include file="/WEB-INF/jsp/inc/olbHeader.jspf" %>

<div id="olbhero"><div class="inner"><table width="100%"><tr>
  <td valign="middle">
    <div class="kicker">Online Banking &middot; Transfers</div>
    <h1>Transfer Money</h1>
    <p>Move money between your accounts or to someone else &mdash; schedule one-time or recurring transfers.</p>
  </td>
  <td align="right" valign="middle" width="660"><div class="illus"><img src="<%=request.getContextPath()%>/images/calendar.png" alt="" width="460" height="300" /></div></td>
</tr></table></div></div>

<div class="inner" id="crumbs"><a href="<%=request.getContextPath()%>/secure/transfer.do">Home</a><span class="sep">/</span><a href="<%=request.getContextPath()%>/secure/transfer.do">Accounts</a><span class="sep">/</span><b>Transfer Money</b></div>

<div class="inner" id="xfr"><table width="100%" cellpadding="0" cellspacing="0"><tr>
  <td class="lcol">
    <h2>It's easy to transfer funds</h2>
    <p class="lead">Move money between your Bank of America accounts or to an external account. You can schedule transfers, set up recurring transfers, or make Bank of America payments.</p>
    <ul class="dash">
      <li>Transfers between your accounts post the same day.</li>
      <li>Schedule transfers or set up recurring transfers.</li>
      <li>Your savings preferences stay with your account.</li>
    </ul>
    <% for (Iterator it = accounts.iterator(); it.hasNext();) { Account a = (Account) it.next(); if (a.isExternal()) continue; %>
    <div class="acct">
      <div class="bal"><%= a.getCurrentBalanceDisplay() %></div>
      <div class="nm"><%= a.getDisplayName() %></div>
      <div class="id"><%= a.getAcctId() %></div>
      <% if (a.getAvailableBalanceCents() != a.getCurrentBalanceCents()) { %><div class="avl">Available <%= a.getAvailableBalanceDisplay() %></div><% } %>
    </div>
    <% } %>

    <table class="activity" cellpadding="0" cellspacing="0">
      <tr><th>DATE</th><th>CONFIRMATION</th><th>FROM</th><th>TO</th><th>STATUS</th><th style="text-align:right">AMOUNT</th></tr>
      <c:forEach var="t" items="${recentTransfers}">
      <tr>
        <td><fmt:formatDate value="${t.scheduledDate}" pattern="MM/dd/yyyy"/></td>
        <td style="font-family:'Courier New',monospace">${t.confirmationNumber}</td>
        <td>${t.fromDisplay}</td>
        <td>${t.toDisplay}</td>
        <td class="status-${t.statusCode}"><bean:message key="stat.${t.statusCode}"/><c:if test="${t.statusCode == 'S'}"> &middot; <fmt:formatDate value="${t.postDate}" pattern="MM/dd"/></c:if></td>
        <td class="amt">${t.amountDisplay}<c:if test="${t.feeCents > 0}"><br /><span style="font-weight:normal;color:#777;font-size:12px">+ ${t.feeDisplay} fee</span></c:if></td>
      </tr>
      </c:forEach>
    </table>
  </td>

  <td class="rcol">
    <div class="panel">
      <h2>Make a transfer</h2>
      <p class="sub">Choose the accounts and amount, then select Transfer now.</p>
      <html:errors/>
      <html:form action="/secure/transferSubmit" method="post" styleId="xfrForm">
        <div class="fld"><label for="fromAcctId">From</label>
          <select name="fromAcctId" id="fromAcctId">
          <% for (Iterator it = accounts.iterator(); it.hasNext();) { Account a = (Account) it.next(); %>
            <option value="<%= a.getAcctId() %>" data-ext="<%= a.isExternal() ? "Y" : "N" %>"<%= a.getAcctId().equals(tf.getFromAcctId()) ? " selected=\"selected\"" : "" %>><%= a.getDisplayName() %></option>
          <% } %>
          </select></div>
        <div class="fld"><label for="toAcctId">To</label>
          <select name="toAcctId" id="toAcctId">
          <% for (Iterator it = accounts.iterator(); it.hasNext();) { Account a = (Account) it.next(); %>
            <option value="<%= a.getAcctId() %>" data-ext="<%= a.isExternal() ? "Y" : "N" %>"<%= a.getAcctId().equals(tf.getToAcctId()) ? " selected=\"selected\"" : "" %>><%= a.getDisplayName() %></option>
          <% } %>
          </select></div>
        <table class="two" cellpadding="0" cellspacing="0"><tr>
          <td class="l"><div class="fld"><label for="amount">Amount</label><html:text property="amount" styleId="amount" styleClass="txt" maxlength="16" /></div></td>
          <td class="r"><div class="fld"><label for="tierCode">Relationship tier</label>
            <select name="tierCode" id="tierCode">
              <option value="00"<%= "00".equals(tf.getTierCode()) ? " selected=\"selected\"" : "" %>><bean:message key="tier.00"/></option>
              <option value="10"<%= "10".equals(tf.getTierCode()) ? " selected=\"selected\"" : "" %>><bean:message key="tier.10"/></option>
              <option value="20"<%= "20".equals(tf.getTierCode()) ? " selected=\"selected\"" : "" %>><bean:message key="tier.20"/></option>
              <option value="30"<%= "30".equals(tf.getTierCode()) ? " selected=\"selected\"" : "" %>><bean:message key="tier.30"/></option>
            </select></div></td>
        </tr></table>
        <div class="fld" id="deliveryRow" style="display:none"><label for="delivery">Delivery</label>
          <select name="delivery" id="delivery">
            <option value="EXS"<%= "EXS".equals(tf.getDelivery()) ? " selected=\"selected\"" : "" %>><bean:message key="xfrtype.EXS"/></option>
            <option value="EXN"<%= "EXN".equals(tf.getDelivery()) ? " selected=\"selected\"" : "" %>><bean:message key="xfrtype.EXN"/></option>
          </select></div>
        <div class="schedlink"><a href="#" id="schedToggle">Schedule options</a></div>
        <div id="schedOpts" style="display:none">
          <table class="two" cellpadding="0" cellspacing="0"><tr>
            <td class="l"><div class="fld"><label for="scheduledDate">Send on (MM/DD/YYYY)</label><html:text property="scheduledDate" styleId="scheduledDate" styleClass="txt" maxlength="10" /></div></td>
            <td class="r"><div class="fld"><label for="frequency">Frequency</label>
              <select name="frequency" id="frequency">
                <option value="O"<%= "O".equals(tf.getFrequency()) ? " selected=\"selected\"" : "" %>><bean:message key="freq.O"/></option>
                <option value="W"<%= "W".equals(tf.getFrequency()) ? " selected=\"selected\"" : "" %>><bean:message key="freq.W"/></option>
                <option value="M"<%= "M".equals(tf.getFrequency()) ? " selected=\"selected\"" : "" %>><bean:message key="freq.M"/></option>
              </select></div></td>
          </tr></table>
          <div class="fld"><label for="memo">Memo (optional)</label><html:text property="memo" styleId="memo" styleClass="txt" maxlength="60" /></div>
        </div>
        <button type="submit" class="btn-primary">Transfer now</button>
        <div id="quoteErr"></div>
      </html:form>
      <div class="note">Your transfer is submitted as soon as you select Transfer now.</div>

      <table class="summary" cellpadding="0" cellspacing="0">
        <tr><th colspan="2">TRANSFER SUMMARY</th></tr>
        <tr><td>From</td><td class="v" id="sFrom"><%= quote != null ? quote.getFromDisplay() : "&mdash;" %></td></tr>
        <tr><td>To</td><td class="v" id="sTo"><%= quote != null ? quote.getToDisplay() : "&mdash;" %></td></tr>
        <tr><td>Amount</td><td class="v" id="sAmount"><%= quote != null ? quote.getAmountDisplay() : "&mdash;" %></td></tr>
        <tr><td>Fee</td><td class="v" id="sFee"><%= quote != null ? quote.getFeeDisplay() : "&mdash;" %></td></tr>
        <tr><td>Transfer type</td><td class="v" id="sType"><% if (quote != null) { %><bean:message key='<%= "xfrtype." + quote.getTypeCode() %>'/><% } else { %>&mdash;<% } %></td></tr>
        <tr><td>Relationship tier</td><td class="v" id="sTier"><% if (quote != null) { %><bean:message key='<%= "tier." + quote.getTierCode() %>'/><% } else { %>&mdash;<% } %></td></tr>
        <tr><td>Delivery date</td><td class="v" id="sDelivery"><%= quote != null ? df.format(quote.getDeliveryDate()) : "&mdash;" %></td></tr>
        <tr class="total"><td>Total debit</td><td class="v" id="sTotal"><%= quote != null ? quote.getTotalDebitDisplay() : "&mdash;" %></td></tr>
      </table>
    </div>
  </td>
</tr></table></div>

<%@ include file="/WEB-INF/jsp/inc/footer.jspf" %>
</body>
</html>
