/* 
 * The Spider program that coollects webpages and indexes them.
 */


import java.util.Vector;
import java.net.URL;
import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;

public class Spider {

	Spider() {
		
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
	}

	private void extractMetaData() throws ParserException {
		//Here we will gather data such as modified date, page size
	}

	private void extractKeywords() throws ParserException {
		//Here we will get all the keywords of the page and their freq
		//and save them into the jdbm system
	}
	
	private void extractLinks() throws ParserException {
		//Here we shall extract the links to other pages and record
		//them into the database :)
	}
}
