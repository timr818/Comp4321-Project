/*
 * This file uses jdbm to manage a database of indexed webpages.
 */


/*
 * NOTES FROM PROJECT REQUIREMENTS:
 * 1. The indexer first removes all stop words from the file (a dictionary of stop words will be provided)
 * 2. then transfroms words into stems using the Porter's algorithm
 * 3. then inserts stems into the two inverted files:
 * 	a. all stems extracted from the page body, together with the statistical info needed to support
 * 	the vector space model.
 * 	b. all stems extracted from the page title are inserted into another inverted file
 * 4. The indexes must be able to support phrase search such as "HONG KONG" in page titles and page bodies
 */

//TODO:
//	Make the addentry not add duplicate entries
//
//	Make the add links work
//
//	Make it convert to wordIDs and docIDs :)

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.util.Vector;
import java.util.HashMap;
import java.io.IOException;

public class DataManager {

	public static final String MANAGER_ID = "recman";
	public static final String BODY_ID = "bodyid";
	public static final String TITLE_ID = "titleid";
	public static final String LINKS_ID = "links";
	
	private static final String WORDS_ID = "wordIDs";
	private static final String PAGES_ID = "pageIDs";

	private RecordManager recordManager;
	
	//Inverted index for the keywords in the pagebody of the pages
	private HTree pagebodyHash;

	//Inverted index for the keywords in the pagetitle of the pages
	private HTree pagetitleHash;

	//hash table for links that have the pageID and the pageIDs that that page links to
	private HTree linksHash;

	//These are the conversions for word -> wordID and URL -> pageIDs
	private HTree wordIDs;
	private HTree pageIDs;
	
	//The next ID number to give to the next unknown word or url
	private int currWordID = 0;
	private int currPageID = 0;

	DataManager() throws IOException {
		recordManager = RecordManagerFactory.createRecordManager(MANAGER_ID);
		createIndexTable(BODY_ID);
		createIndexTable(TITLE_ID);
		createIndexTable(LINKS_ID);
		createIndexTable(WORDS_ID);
		createIndexTable(PAGES_ID);
	}

	//initializes the hash tables
	private void createIndexTable(String id) throws IOException {
		long hashid = recordManager.getNamedObject(id);

		if (hashid != 0) {
			if (id.equals(BODY_ID)) {
				pagebodyHash = HTree.load(recordManager, hashid);
			} else if (id.equals(TITLE_ID)) {
				pagetitleHash = HTree.load(recordManager, hashid);
			} else if (id.equals(LINKS_ID)) {
				linksHash = HTree.load(recordManager, hashid);
			} else if (id.equals(WORDS_ID)) {
				wordIDs = HTree.load(recordManager, hashid);
			} else {
				pageIDs = HTree.load(recordManager, hashid);
			}
		} else {
			if (id.equals(BODY_ID)) {
				pagebodyHash = HTree.createInstance(recordManager);
				recordManager.setNamedObject(id, pagebodyHash.getRecid());
			} else if (id.equals(TITLE_ID))  {
				pagetitleHash = HTree.createInstance(recordManager);
				recordManager.setNamedObject(id, pagetitleHash.getRecid());
			} else if (id.equals(LINKS_ID)) {
				linksHash = HTree.createInstance(recordManager);
				recordManager.setNamedObject(id, linksHash.getRecid());
			} else if (id.equals(WORDS_ID)) {
				wordIDs = HTree.createInstance(recordManager);
				recordManager.setNamedObject(id, wordIDs.getRecid());
			} else {
				pageIDs = HTree.createInstance(recordManager);
				recordManager.setNamedObject(id, pageIDs.getRecid());
			}
		}
	}

	//finalizes the changes to the hashtables
	public void finalize() throws IOException {
		recordManager.commit();
		recordManager.close();
	}

	//add an entry into the hashtable
	//id = which hash table to add to (body, title or links)
	//keyword = the keyword from the page
	//url = the url of the page
	public void addEntry(String id, String key, String val) throws IOException {
		HTree hash;
		if (id.equals(BODY_ID)) {
			hash = pagebodyHash;
		} else if (id.equals(TITLE_ID)) {
			hash = pagetitleHash;
		} else {
			hash = linksHash;
		}

		int wordID = getWordID(key);
		if (wordID == -1) {
			wordIDs.put(currWordID, key);
			wordID = currWordID;
			currWordID++;
		}

		int pageID = getPageID(val);
		if (pageID == -1) {
			pageIDs.put(currPageID, val);
			pageID = currPageID;
			currPageID++;
		}
		
		String content = (String) hash.get(wordID);
		if (content == null) {
			content = Integer.toString(pageID);
		} else {
			content += ";" + Integer.toString(pageID);
		}

		hash.put(wordID, content);
		
	}

	//returns -1 if the word has not been used, the wordID otherwise
	private int getWordID(String word) throws IOException {
		FastIterator iter = wordIDs.values();
		FastIterator keys = wordIDs.keys();

		String curr;
		int wordID;
		while((curr = (String) iter.next()) != null) {
			wordID = (int) keys.next();
			if (curr.equals(word)) {
				return wordID;
			}
		}

		return -1;
	}
	
	//returns -1 if the url has not been used, pageID otherwise
	private int getPageID(String url) throws IOException {
		FastIterator vals = pageIDs.values();
		FastIterator keys = pageIDs.keys();

		String curr;
		int pageID;
		while((curr = (String) vals.next()) != null) {
			pageID = (int) keys.next();
			if (curr.equals(url)) {
				return pageID;
			}
		}

		return -1;
	}

	//deletes an entry from the hashtable (id specifies which has table to delete from)
	public void deleteEntry(String id, String keyword) throws IOException {
		HTree hash;
		if (id.equals(BODY_ID)) {
			hash = pagebodyHash;
		} else if (id.equals(TITLE_ID)) {
			hash = pagetitleHash;
		} else {
			hash = linksHash;
		}

		hash.remove(keyword);
	} 

	public void printAll() throws IOException {
		FastIterator iter1 = pagebodyHash.keys();
		FastIterator iter2 = pagetitleHash.keys();
		FastIterator iter3 = linksHash.keys();
		
		System.out.println("PAGE BODY INDEX");
		Integer key;
		while((key=(Integer)iter1.next()) != null) {
			System.out.println(key + " = " + pagebodyHash.get(key));
		}
		
		System.out.println("");
		System.out.println("PAGE TITLE INDEX");
		Integer a;
		while((a = (Integer)iter2.next()) != null) {
			System.out.println(a + " = " + pagetitleHash.get(a));
		}

		System.out.println("");
		System.out.println("PAGE LINKS INDEX");
		Integer b;
		while ((b = (Integer)iter3.next()) != null) {
			System.out.println(b + " = " + pagetitleHash.get(b));
		}
	}

	public static void main (String[] args) {
		try{
		DataManager d = new DataManager();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
}
