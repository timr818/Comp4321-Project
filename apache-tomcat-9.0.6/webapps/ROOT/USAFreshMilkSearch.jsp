<%
        out.println("The words you entered are: <br>");
        String arr = request.getParameter("words");
        String[] a = arr.split(" ");
        for(int i = 0; i < a.length; i++)
                out.println(a[i] + "<br>");
%>
