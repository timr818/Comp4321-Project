/* 
 * The Spider program that coollects webpages and indexes them.
 */

//to compile:	javac -cp lib/htmlparser.jar Spider.java
//to run:		java -cp lib/htmlparser.jar:. Spider

import java.util.Vector;

import org.htmlparser.beans.StringBean;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import java.util.StringTokenizer;
import org.htmlparser.beans.LinkBean;

import java.net.URL;
import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;


public class Spider {
	
	private String url;
	private Vector<String> linkResults;
	private Vector<String> wordResults;

	Spider(String _url) {
		url = _url;
		linkResults = new Vector<String>();
		wordResults = new Vector<String>();
	}

	//testing with manual input
	public static void main(String [] args){
		Spider testCrawl = new Spider("http://www.cse.ust.hk/");
		
		testCrawl.crawlURL("http://www.cse.ust.hk/",1);
		
		try{
			testCrawl.printWords();
			//testCrawl.printLinks();
		}
		catch(Exception e){
			System.out.println(e);
		}
		return;
	}

	public void crawlURL(String url, int numPages) {
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
		try{
			extractKeywords(url);
			extractLinks(url);
		}
		catch (ParserException e){
			e.printStackTrace ();
		}


		return;
	}

	private void extractMetaData() throws ParserException {
		//Here we will gather data such as modified date, page size
	}

	private void extractKeywords(String url) throws ParserException {
		//Here we will get all the keywords of the page and their freq
		//and save them into the jdbm system
		
		//Extracts words/titles
		//Vector<String> wordResults = new Vector<String>();
		StringBean beanWord = new StringBean();
		beanWord.setURL(url);
		beanWord.setLinks(false);
		String contents = beanWord.getStrings();
		StringTokenizer st = new StringTokenizer(contents);
		while (st.hasMoreTokens()) {
			String tokenNext = st.nextToken();
			wordResults.add(tokenNext);
			//System.out.println(tokenNext);
		}

		return;
	}
	
	private void extractLinks(String url) throws ParserException {
		//Here we shall extract the links to other pages and record
		//them into the database :)
		
		//Extracts links
		//Vector<String> linkResults = new Vector<String>();
		LinkBean beanLinks = new LinkBean();
		beanLinks.setURL(url);
		URL[] urls = beanLinks.getLinks();
		for (URL s : urls) {
			linkResults.add(s.toString());
			//System.out.println(s.toString());
		}

		return;
	}


	public void printWords() throws ParserException{
		for (Object s : wordResults) {
			System.out.println(s);
		}

		return;
	}

	//Prints all links in a given page, primarily used for testing
	public void printLinks() throws ParserException{
		for (Object s : linkResults) {
			System.out.println(s);
		}

		return;
	}
}
