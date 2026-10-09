<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" isErrorPage="true" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>We're sorry - Bank of America Online Banking</title>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/olb.css" />
</head>
<body>
<%@ include file="/WEB-INF/jsp/inc/olbHeader.jspf" %>
<div class="inner" id="xfr">
  <h2>We're sorry, Online Banking is temporarily unavailable.</h2>
  <p class="lead">Please try again in a few minutes. If you continue to see this message, call us at 800.432.1000.</p>
  <p style="font-family:'Courier New',monospace;color:#777;font-size:12px">
    Error reference: ERR-<%= Long.toHexString(System.currentTimeMillis()).toUpperCase() %>
    <% Object code = request.getAttribute("javax.servlet.error.status_code"); if (code != null) { %> &middot; HTTP <%= code %><% } %>
    <% if (exception != null) { %> &middot; <%= exception.getClass().getName() %><% } %>
  </p>
  <p><a class="btn-secondary" href="<%=request.getContextPath()%>/index.jsp">Return to sign in</a></p>
</div>
<%@ include file="/WEB-INF/jsp/inc/footer.jspf" %>
</body>
</html>
