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
        String arr = request.getParameter("words");
	out.println("<br>Search results for: " + arr + "<br><br>");

        String[] a = arr.split(" ");

	Porter porter = new Porter();
		
	for(int i = 0; i < a.length; i++){
		a[i] = porter.stripAffixes(a[i]);
		//out.print(a[i]+ "<br>");//TO TEST
	}

	Vector<String> pageIDList = new Vector<String>();
        DataManager dm = new DataManager();
	pageIDList = dm.querySimilarity(a);

	//TEST	
	//out.print(pageIDList.size()+"<br>");

	String mostFreqWord = "";
	String pageRankIDList = "";
	for(int j = 0; j < pageIDList.size(); j++){
		pageRankIDList = pageIDList.get(j);
		String[] pageRankIDListArr = pageRankIDList.split(";");
		int pageID = Integer.parseInt(pageRankIDListArr[0]);
		
		//prints out page rank (vector space model)
		out.print(String.format("%.3f", Double.parseDouble(pageRankIDListArr[1])));
		//prints out linked title
		out.print("<a href = \"" + dm.getURL(pageID) + "\">");
		out.print(dm.getPageTitle(pageID) + "</a><br>");
		//prints out last mod date and size of page
		out.print("Last modified date: " + dm.getModifiedDate(pageID) + ";Page Size: " + dm.getPageSize(pageID) + "<br>");
		//print 5 most frequent words
		mostFreqWord = dm.retrieveMostFreqKeywords(pageID);
		out.print(mostFreqWord + "<br>");
		//print child links
		Vector<Integer> pageLinks = new Vector<Integer>();
		pageLinks = dm.getLinks(pageID);
		out.print("Child Links:<br>");
		for(int k = 0; k < pageLinks.size(); k++){
			out.print(dm.getURL(pageLinks.get(k)) + "<br>");
		}
		out.print("<br>");
	}	
%>
