<%@ page import="java.util.Vector,searchEngine.*,jdbc.*" %>
<html>
	<head>USA Fresh Milk</head>
		<body>
			<p>Got Milk?</p>
				<form method = "post" action = USAFreshMilkSearch.jsp>
					Please key in a list of words:
					<input type = "text" name = "words">
					<input type = "submit">
				</form>
		</body>
</html>

<%
        out.println("The words you entered are: <br>");
        String arr = request.getParameter("words");
        String[] a = arr.split(" ");
	
	//loop that stems the user input a into stemA
	Porter porter = new Porter();
	 
	for(int i = 0; i < a.length; i++){
		a[i] = porter.stripAffixes(a[i]);
		out.print(a[i]+ "<br>");
	}

	Vector<Integer> pageIDList = new Vector<Integer>();
        DataManager dm = new DataManager();
	pageIDList = dm.querySimilarity(a);

	//TO TEST
	out.println("<br>Ranked Pages:<br>");
	
	for(int j = 0; j < pageIDList.size(); j++){
		out.print("<a href = \"" + dm.getURL(pageIDList.get(j)) + "\">");
		out.print(dm.getPageTitle(pageIDList.get(j)) + "</a><br>");
		//out.println(pageIDList.get(j) + "<br>");
	}
	
%>
