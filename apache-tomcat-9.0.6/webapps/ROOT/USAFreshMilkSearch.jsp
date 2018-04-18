<%@ page import="java.util.Vector,searchEngine.*,jdbc.*" %>
<%
        out.println("The words you entered are: <br>");
        String arr = request.getParameter("words");
        String[] a = arr.split(" ");

	Vector<Integer> pageIDList = new Vector<Integer>();
	
	//TO TEST
	out.println("String before split: " + arr + "<br>String after split: ");
	for(int j = 0; j < a.length; j++){
		out.println(a[j] + " ");
	}

        DataManager dm = new DataManager();
	pageIDList = dm.querySimilarity(a);
	
	//TO TEST
	out.println("<br><br>pageIDList type: " + pageIDList.getClass()  + "<br>" + pageIDList + "<br><br>");	

	/*
	for(int i = 0; i < pageIDList.size(); i++){
		out.println(pageIDList.get(i) + "<br>");
	}
	*/
%>
