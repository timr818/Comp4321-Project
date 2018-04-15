<%
        out.println("The words you entered are: <br>");
        String arr = request.getParameter("words");
        String[] a = arr.split(" ");

	Vector<Integer> pageIDList = new Vector<Integer>();
	
        DataManager dm = new DataManager();
	pageIDList = dm.querySimilarity(a);
	
	for(int i = 0; i < pageIDList.length; i++){
		out.println(pageIDList[i] + "<br>");
	}
%>
