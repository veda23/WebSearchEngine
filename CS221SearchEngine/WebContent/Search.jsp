<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*" %>
<%@ page import="business.SearchHandler" %>
<%@ page import="business.Result" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>CS221 ICS Search</title>
</head>
<body style="margin: 0 50px;">
<h1>Seeker</h1>
<form method="get" action="SearchHandler">
<%
if (request.getParameter("k") != null) {
	%>
	<input type="text" id="searchBox" name="k" placeholder="Enter Search Query" value="<%= request.getParameter("k") %>" style="width:300px"/>
	<%
} else {
%>
<input type="text" id="searchBox" name="k" placeholder="Enter Search Query" style="width:100px"/>
<%
}
%>
<input type="submit" value="Search" style="margin-left:30px" />
<br />
</form>
<%
   if (request.getAttribute("results") != null) {
	   List<Result> al = (List<Result>) request.getAttribute("results");
       Iterator itr = al.iterator();
       if (!itr.hasNext()) {
	   %>
	   Sorry! No results found. Try again.
	   <%
    	} else {
%>
<div id="resultsArea" style="margin-top:30px; margin-bottom:30px;">
<%
			while (itr.hasNext()) {
		    	Result oneResult = (Result) itr.next();
%>
		<a href="<%= oneResult.getURL() %>"><%= oneResult.getTitle() %></a><br />
		<div style="color:green;font-size:13px"><%= oneResult.getURL() %></div>
		<%= oneResult.getRelevantText() %><br />
		<div style="min-height:13px;"></div>
<%
			}
    	}
%>
</div>
<%
   }
%>
</body>
</html>