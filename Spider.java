/* 
 * The Spider program that coollects webpages and indexes them.
 */

//to compile:	javac -cp lib/htmlparser.jar Spider.java
//to run:		java -cp lib/htmlparser.jar:. Spider

import org.htmlparser.beans.StringBean;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;

import org.htmlparser.filters.TagNameFilter;	//testing
import org.htmlparser.tags.TitleTag;	//testing
import org.htmlparser.Node;	//testing

import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.Parser;

import java.util.StringTokenizer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import java.net.URL;

import java.io.IOException;

public class Spider {
	
	private String url;

	//results from crawl
	private List<String> linkResults = new LinkedList<String>();
	private Vector<String> wordResults = new Vector<String>();

	//information to be used by the recursive function
	private Set<String> pagesVisited = new HashSet<String>();
	private List<String> pagesToVisit = new LinkedList<String>();
	private static final int MAX_PAGES_TO_SEARCH = 30;

	//Constructor
	Spider() {

	}
	

	//testing with manual input
	public static void main(String [] args){
		Spider testCrawl = new Spider();
		try {
			testCrawl.search("http://www.cse.ust.hk/");
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return;
	}




	/**
	* This performs all the work. It makes an HTTP request, checks the response, and then gathers
	* up all the links on the page. Perform a searchForWord after the successful crawl
	* 
	* @param url
	*            - The URL to visit
	* @return whether or not the crawl was successful
	*/
	//Sifts through URL pages and sends it to the database
	public void crawl(String url, DataManager db) {
		//given the url, it will crawl it and recursively crawl on the pages that it links to
		//
		//NOTES FROM PROJECT REQUIREMENTS:
		//1. Given a starting URL, and the number of pages to be indexed, recursively fetch
		//the required pages using a breadth-first startegy.
		//
		//2. BEFORE A PAGE IS FETCHED:
		//	a. if url does not exist in index, go ahead and retrieve it
		//	b. if url already exist but last modification date of the url is later than
		//	that recorded in the index, go ahead and retrieve the url; otherwise ignore
		//	c. handle cyclic links gracefully 
		

		try {
			this.extractLinks(url, db);
			this.extractKeywords(url, db);
			this.extractMetaData(url);
		}
		catch(Exception e) {
			System.out.println(e);
		}


		return;
	}


	//Iteratively searches through a certain number of pages decided on my MAX_PAGES_TO_SEARCH variable
	public DataManager search(String url) throws IOException {
		DataManager db = new DataManager();
		
		while(this.pagesVisited.size() < MAX_PAGES_TO_SEARCH) {
			String currentUrl;
			Spider leg = new Spider();
			
			if(this.pagesToVisit.isEmpty()) {
				currentUrl = url;
				this.pagesVisited.add(url);
			}
			else {
				currentUrl = this.nextUrl();
			}

			System.out.println("\n\n");
			System.out.println("PAGE VISITED SIZE: " + this.pagesVisited.size());
			System.out.println("PAGE URL:          " + currentUrl);
			System.out.println("\n\n");

			/*
			try{
				//this.printWords();
				//this.printLinks();
			}
			catch(Exception e){
				System.out.println(e);
			}
			*/

			leg.crawl(currentUrl, db); // Lots of stuff happening here. Look at the crawl method in Spider
			this.pagesToVisit.addAll(leg.getLinks());
		}

		return db;
	}


	private String nextUrl()
	{
		String nextUrl;
		do {
			nextUrl = this.pagesToVisit.remove(0);
		} while(this.pagesVisited.contains(nextUrl));

		this.pagesVisited.add(nextUrl);
		return nextUrl;
	}

	public List<String> getLinks() {
    		return this.linkResults;
	}


	private void extractMetaData(String url) throws ParserException {
		//Here we will gather data such as modified date, page size
		Parser parser = new Parser(url);
		TagNameFilter filter = new TagNameFilter("title");
		
		try {
			NodeList list = parser.parse(filter);
			Node node = list.elementAt(0);
			
			if (node instanceof TitleTag) {
				TitleTag titleTag = (TitleTag) node;
				String title = titleTag.getTitle();
				System.out.println(title); 
			}
		}
		catch(Exception e) {
			System.out.println(e);
		}
		
		return;
	}


	//Extracts all the words in a given URL, may need to sift through words to get rid of useless characters i.e. brackets, periods, etc
	private void extractKeywords(String url, DataManager db) throws ParserException, IOException {
		//Here we will get all the keywords of the page and their freq
		//and save them into the jdbm system
		
		//Extracts words/titles
		Parser parser = new Parser(url);
		StringBean beanWord = new StringBean();
		parser.visitAllNodesWith(beanWord);
		String contents = beanWord.getStrings();
		StringTokenizer st = new StringTokenizer(contents);	

		//Extracts Titles


		while (st.hasMoreTokens()) {
			String tokenNext = st.nextToken();
			this.wordResults.add(tokenNext);
			db.addEntry(DataManager.BODY_ID, tokenNext, url);		

			//THIS IS WHERE YOU SEND DATA TO DATABASE!!
			//System.out.println(tokenNext);
		}

		return;
	}

	
	//Extracts all the links in a given URL
	private void extractLinks(String url, DataManager db) throws ParserException, IOException {
		//Here we shall extract the links to other pages and record
		//them into the database :)
		
		//Extracts links
		LinkBean beanLinks = new LinkBean();
		beanLinks.setURL(url);
		URL[] urls = beanLinks.getLinks();
		for (URL s : urls) {
			this.linkResults.add(s.toString());
			db.addEntry(DataManager.LINKS_ID, url, s.toString());

			//prints out links in the page
			//System.out.println(s.toString());
		}

		return;
	}
}
