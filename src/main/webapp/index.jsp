<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" session="true" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%
  // "Save user ID" cookie
  String savedUid = "";
  javax.servlet.http.Cookie[] cks = request.getCookies();
  if (cks != null) for (int i = 0; i < cks.length; i++) if ("olb_uid".equals(cks[i].getName())) savedUid = cks[i].getValue();
  boolean expired = "1".equals(request.getParameter("expired"));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
<title><bean:message key="app.title"/></title>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/olb.css" />
<!--[if lte IE 8]><style type="text/css">#hero{background:#1a3dcf;filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#0b1e7a',endColorstr='#2255e6',GradientType=1);}</style><![endif]-->
<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/olb.js"></script>
</head>
<body data-ctx="<%=request.getContextPath()%>">
<%@ include file="/WEB-INF/jsp/inc/publicHeader.jspf" %>

<div id="hero"><div class="inner">
  <div id="loginbox">
    <div class="redtab"></div>
    <div class="pad">
      <% if (expired) { %><div class="olb-errbox"><bean:message key="error.session.expired"/></div><% } %>
      <html:errors/>
      <html:form action="/login" method="post" styleId="loginForm">
        <label for="userId">User ID</label>
        <html:text property="userId" styleId="userId" styleClass="txt" value="<%= savedUid %>" />
        <label for="password">Password</label>
        <html:password property="password" styleId="password" styleClass="txt" redisplay="false" />
        <div class="chk"><html:checkbox property="saveUserId" styleId="saveUserId" value="true" /><label for="saveUserId" style="display:inline">Save user ID</label></div>
        <button type="submit" class="btn-primary">Log in</button>
      </html:form>
      <div class="links">
        <a href="#">Forgot user ID/password</a><br />
        <a href="#">Security &amp; Help</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#">Enroll</a>
      </div>
    </div>
  </div>
  <div id="openacct"><img src="<%=request.getContextPath()%>/images/dollar.png" alt="" width="24" height="24" />Open an account</div>

  <div id="cards">
    <h1>Choose the card that works for you</h1>
    <table cellpadding="0" cellspacing="0"><tr>
      <td>
        <div class="stat">6<sup>%</sup></div><div class="statlbl">cash back offer</div><div class="noannual">No annual fee.</div>
        <img class="card" src="<%=request.getContextPath()%>/images/card-red.png" alt="Customized Cash Rewards card" />
        <div class="cardname">Customized Cash Rewards</div>
        <a class="offer" href="#">$200<br />online bonus offer</a>
      </td>
      <td>
        <div class="stat">1.5<sup>%</sup></div><div class="statlbl">cash back</div><div class="noannual">No annual fee.</div>
        <img class="card" src="<%=request.getContextPath()%>/images/card-silver.png" alt="Unlimited Cash Rewards card" />
        <div class="cardname">Unlimited Cash Rewards</div>
        <a class="offer" href="#">$250<br />online bonus offer</a>
      </td>
      <td>
        <div class="stat">1.5</div><div class="statlbl">points for every <sup style="font-size:11px">$</sup>1</div><div class="noannual">No annual fee.</div>
        <img class="card" src="<%=request.getContextPath()%>/images/card-blue.png" alt="Travel Rewards card" />
        <div class="cardname">Travel Rewards</div>
        <a class="offer" href="#">25,000 online<br />bonus points offer</a>
      </td>
      <td>
        <div class="stat">0<sup>%</sup></div><div class="statlbl">intro APR offer</div><div class="noannual">No annual fee.</div>
        <img class="card" src="<%=request.getContextPath()%>/images/card-white.png" alt="BankAmericard" />
        <div class="cardname">BankAmericard&reg;</div>
        <a class="offer" href="#">Intro APR offer<br />for 21 billing cycles</a>
      </td>
    </tr></table>
  </div>
</div></div>

<div id="cookie">
  We use cookies and other tracking technologies to collect data for advertising, fraud prevention, analytics, and<br />
  other purposes. By using this website, you agree to the use of these tracking technologies and to the use and<br />
  disclosure of data in accordance with our <a href="#">Privacy Notices</a>
  <img class="close" src="<%=request.getContextPath()%>/images/close.png" alt="Close" width="16" height="16" />
</div>
</body>
</html>
