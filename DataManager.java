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


import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.util.Vector;
import java.util.HashMap;
import java.io.IOException;

public class DataManager {

	public static final String MANAGER_ID = "recman";
	public static final String INDEX_ID = "index";
	public static final String BODY_ID = "bodyid";
	public static final String TITLE_ID = "titleid";
	public static final String LINKS_ID = "links";
	
	private static final String WORDS_ID = "wordIDs";
	private static final String PAGES_ID = "pageIDs";

	private RecordManager recordManager;
	
	//Non-Inverted hash for pageID -> wordID
	private HTree indexHash;

	//Inverted index for the keywords in the pagebody of the pages
	private HTree pagebodyHash;

	//Inverted index for the keywords in the pagetitle of the pages
	private HTree pagetitleHash;

	//hash table for links that have the pageID and the pageIDs that that page links to
	private HTree linksHash;

	//These are the conversions for word -> wordID
	private HTree wordIDs;

	//Conversion for pageIDs -> urls (also stores metadata)
	//format: pageID -> url;pagetitle;last modified date;page size
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
		createIndexTable(INDEX_ID);

		Initialize();
	}

	public static void main (String[] args) {
		try {
			DataManager dm = new DataManager();
			dm.addEntry(DataManager.BODY_ID, "word1", "url1");
			dm.addEntry(DataManager.BODY_ID, "word1", "url2");
			dm.addEntry(DataManager.BODY_ID, "word1", "url1");
			dm.addEntry(DataManager.BODY_ID, "word2", "url1");
			dm.addEntry(DataManager.BODY_ID, "word3", "url1");
			dm.addEntry(DataManager.BODY_ID, "word2", "url2");
			dm.printAll();
			dm.finalize();
		} catch (IOException e) {
			
		}
	}

	private void Initialize() throws IOException {
		FastIterator wordIter = wordIDs.keys();
		FastIterator pageIter = pageIDs.keys();
		
		Integer a;
		Integer max = 0;
		while((a = (Integer) wordIter.next()) != null) {
			if (a > max) {
				max = a;
			}
		}
		currWordID = max;

		Integer b;
		max = 0;
		while((b = (Integer) pageIter.next()) != null) {
			if (b > max) {
				max = b;
			}
		}
		currPageID = max;
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
			} else if (id.equals(PAGES_ID)) {
				pageIDs = HTree.load(recordManager, hashid);
			} else {
				indexHash = HTree.load(recordManager, hashid);
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
			} else  if (id.equals(PAGES_ID)) {
				pageIDs = HTree.createInstance(recordManager);
				recordManager.setNamedObject(id, pageIDs.getRecid());
			} else {
				indexHash = HTree.createInstance(recordManager);
				recordManager.setNamedObject(id, indexHash.getRecid());
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
		HTree hash = getHash(id);

		int firstID, secondID;
		
		//get the corresponding ids to the data
		if (!id.equals(LINKS_ID)) {
			firstID = getWordID(key);
			secondID = getPageID(val);
		} else {
			firstID = getPageID(key);
			secondID = getPageID(val);
		}

		//get the exisiting values to avoid duplicates
		Vector<Integer> existingValues;
		if (id.equals(BODY_ID)) {
			existingValues = getPages(firstID);
		} else if (id.equals(TITLE_ID)) {
			existingValues = getTitles(firstID);
		} else {
			existingValues = getLinks(firstID);
		}
		
		//get the current content and append the new entry to it
		String content = (String) hash.get(firstID);

		if (existingValues.contains(secondID)) {
			if (id.equals(BODY_ID) || id.equals(TITLE_ID)) {
				if (content.startsWith(Integer.toString(secondID) + ":")) {
					int startFreq = content.indexOf(':');
					int endFreq = content.indexOf(';');
					int freq = 1;
					String after = "";

					if (startFreq < endFreq) {
						freq = Integer.parseInt(content.substring(startFreq + 1, endFreq));
						after = content.substring(content.indexOf(';'), content.length());
					} else {
						freq = Integer.parseInt(content.substring(startFreq + 1, content.length()));
					}
					
					content = Integer.toString(secondID) + ":" + Integer.toString(freq + 1) + after;
				} else {
					int start = content.indexOf(";" + Integer.toString(secondID) + ":");

					int startFreq = content.indexOf(':', start);
					int endFreq = content.indexOf(';', start + 1);
					int freq = 0;
					String before = content.substring(0, start + 1);
					String after = "";

					if (startFreq < endFreq) {
						freq = Integer.parseInt(content.substring(startFreq + 1, endFreq));
						after = content.substring(endFreq, content.length());
					} else {
						freq = Integer.parseInt(content.substring(startFreq + 1, content.length()));
					}
					
					content = before + Integer.toString(secondID) + ":" + Integer.toString(freq + 1) + after;
				}

				//update indexed hash
				String a = (String) indexHash.get(secondID);
				if (a.startsWith(Integer.toString(firstID) + ":")) {
					int startFreq = a.indexOf(':');
					int endFreq = a.indexOf(';');
					int freq = 1;
					String after = "";

					if (startFreq < endFreq) {
						freq = Integer.parseInt(a.substring(startFreq + 1, endFreq));
						after = a.substring(a.indexOf(';'), a.length());
					} else {
						freq = Integer.parseInt(a.substring(startFreq + 1, a.length()));
					}
					
					a = Integer.toString(firstID) + ":" + Integer.toString(freq + 1) + after;
				} else {
					int start = a.indexOf(";" + Integer.toString(firstID) + ":");
					int startFreq = a.indexOf(':', start);
					int endFreq = a.indexOf(';', start + 1);
					int freq = 0;
					String before = a.substring(0, start + 1);
					String after = "";

					if (startFreq < endFreq) {
						freq = Integer.parseInt(a.substring(startFreq + 1, endFreq));
						after = a.substring(endFreq, a.length());
					} else {
						freq = Integer.parseInt(a.substring(startFreq + 1, a.length()));
					}
					
					a = before + Integer.toString(firstID) + ":" + Integer.toString(freq + 1) + after;
				}

				indexHash.put(secondID, a);
			}
		} else {
			if (content == null) {
				if (id.equals(BODY_ID) || id.equals(TITLE_ID))  {
						content = Integer.toString(secondID) + ":1";
				} else {
						content = Integer.toString(secondID);
				}
			} else {
				if (id.equals(BODY_ID) || id.equals(TITLE_ID))  {
						content += ";" + Integer.toString(secondID) + ":1";
				} else {
						content += ";" + Integer.toString(secondID);
				}
			}

			//add to the non-inverted index
			if (id.equals(BODY_ID) || id.equals(TITLE_ID)) {
				String a = (String) indexHash.get(secondID);
				
				if (a == null) {
					a = Integer.toString(firstID) + ":1";
				} else {
					a += ";" + Integer.toString(firstID) + ":1";
				}

				indexHash.put(secondID, a);
			}
		}

		//finally place into the actual database
		hash.put(firstID, content);
	}

	public void addMetaData(String url, String title, String modDate, int size) throws IOException {
		int pageID = getPageID(url);		
		HTree hash = getHash(PAGES_ID);
		String content = (String) hash.get(PAGES_ID);
		
		content = url + ";" + title + ";" + modDate + ";" + size;
		
		hash.put(pageID, content);
	}

	private HTree getHash(String id) {
		if (id.equals(BODY_ID)) {
			return pagebodyHash;
		} else if (id.equals(TITLE_ID)) {
			return pagetitleHash;
		} else if (id.equals(LINKS_ID)) {
			return linksHash;
		} else if (id.equals(WORDS_ID)) {
			return wordIDs;
		} else if (id.equals(PAGES_ID)) {
			return pageIDs;
		} else {
			return indexHash;
		}
	}
	
	//get the pages (pageIDs) that the given word appears on
	public Vector<Integer> getPages(int wordID) throws IOException {
		Vector<Integer> result = new Vector<Integer>();
		HTree hash = getHash(BODY_ID);
		String content = (String) hash.get(wordID);

		if (content != null) {
			String[] split = content.split(";");
		
			for (String id : split) {
				String[] a = id.split(":");
				result.add(Integer.parseInt(a[0]));
			}
		}

		return result;
	}
	
	//get the pages (pageIDs) that this word appears as the title on
	private Vector<Integer> getTitles(int wordID) throws IOException {
		Vector<Integer> result = new Vector<Integer>();
		HTree hash = getHash(TITLE_ID);
		String content = (String) hash.get(wordID);
		
		if (content != null) {
			String[] split = content.split(";");
		
			for (String id : split) {
				String[] a = id.split(":");
				result.add(Integer.parseInt(a[0]));
			}
		}

		return result;
	}

	//get the pages that the given page links to
	public Vector<Integer> getLinks(int pageID) throws IOException {
		Vector<Integer> result = new Vector<Integer>();
		HTree hash = getHash(LINKS_ID);
		String content = (String) hash.get(pageID);
		
		if (content != null) {
			String[] split = content.split(";");
		
			for (String id : split) {
				result.add(Integer.parseInt(id));
			}
		}

		return result;
	}

	//if the value already exist, it returns the id, otherwise creates the entry and returns the id it assigns to it.
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

		wordIDs.put(currWordID, word);
		int t = currWordID;
		currWordID++;

		return t;
	}
	
	private int getPageID(String url) throws IOException {
		FastIterator vals = pageIDs.values();
		FastIterator keys = pageIDs.keys();

		String curr;
		int pageID;
		while((curr = (String) vals.next()) != null) {
			String[] split = curr.split(";");
			String currURL = split[0];

			pageID = (int) keys.next();
			if (currURL.equals(url)) {
				return pageID;
			}
		}

		pageIDs.put(currPageID, url);
		int t = currPageID;
		currPageID++;

		return t;
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

	public String getPageTitle(int pageID) throws IOException {
		HTree lookup = getHash(PAGES_ID);
		String content = (String) lookup.get(pageID);
		if (content != null) {
			String[] split = content.split(";");
			if (split.length > 1) {
				return split[1];
			}
		}
	
		return "[ title is missing ]";
	}
	
	public String getModifiedDate(int pageID) throws IOException {
		HTree lookup = getHash(PAGES_ID);
		String content = (String) lookup.get(pageID);
		if (content != null) {
			String[] split = content.split(";");
			if (split.length > 2) {
				return split[2];
			}
		}
	
		return "[ modified date is missing ]";
	}

	public String getURL(int pageID) throws IOException {
		HTree lookup = getHash(PAGES_ID);
		String content = (String) lookup.get(pageID);
		if (content != null) {
			String[] split = content.split(";");
			if (split.length > 0) {
				return split[0];
			}
		}

		return "[ url misssing ]";
	}
	
	public String getPageSize(int pageID) throws IOException {
		HTree lookup = getHash(PAGES_ID);
		String content = (String) lookup.get(pageID);
		if (content != null) {
			String[] split = content.split(";");
			if (split.length > 3) {
				return split[3];
			}
		}
	
		return "[ page size is missing ]";
	}

	public Vector<Integer> getKeywords(int pageID) throws IOException {
		HTree hash = getHash(INDEX_ID);
		String content = (String) hash.get(pageID);
		Vector<Integer> result = new Vector<Integer>();

		if (content != null) {
			String[] split = content.split(";");
			for (int i = 0; i < split.length; i++) {
				String[] a = split[i].split(":");
				result.add(Integer.parseInt(a[0]));
			}
		}

		return result;
	}

	public Vector<String> getKeywordsAndFreq(int pageID) throws IOException {
		HTree hash = getHash(INDEX_ID);
		String content = (String) hash.get(pageID);
		Vector<String> result = new Vector<String>();

		if (content != null) {
			String[] split = content.split(";");
			for (int i = 0; i < split.length; i++) {
				String[] a = split[i].split(":");
				content = getWordFromID(Integer.parseInt(a[0])) + " " + a[1];
				result.add(content);
			}
		}

		return result;
	}

	public String getWordFromID(int wordID) throws IOException {
		HTree hash = getHash(WORDS_ID);
		String content = (String) hash.get(wordID);
		if (content == null) {
			return "[ word not found ]";
		} else {
			return content;
		}
	}

	public void printAll() throws IOException {
		FastIterator iter1 = pagebodyHash.keys();
		FastIterator iter2 = pagetitleHash.keys();
		FastIterator iter3 = linksHash.keys();
		FastIterator iter4 = wordIDs.keys();
		FastIterator iter5 = pageIDs.keys();		
		FastIterator iter6 = indexHash.keys();

		int maxPrint = 10;

		System.out.println("PAGE BODY INDEX");
		Integer key;
		int size = 0;
		while((key=(Integer)iter1.next()) != null) {
			size++;
			System.out.println(key + " = " + pagebodyHash.get(key));
			if (size >= maxPrint) {
				break;
			}
		}
		
		System.out.println("");
		System.out.println("PAGE TITLE INDEX");
		Integer a;
		size = 0;
		while((a = (Integer)iter2.next()) != null) {
			size++;
			System.out.println(a + " = " + pagetitleHash.get(a));
			if (size >= maxPrint) {
				break;
			}
		}

		size = 0;
		System.out.println("");
		System.out.println("PAGE LINKS INDEX");
		Integer b;
		while ((b = (Integer)iter3.next()) != null) {
			size++;
			System.out.println(b + " = " + linksHash.get(b));
			if (size >= maxPrint) {
				break;
			}
		}

		System.out.println("");
		System.out.println("WORDID LOOKUP");
		Integer wordID;
		size = 0;
		while((wordID = (Integer)iter4.next()) != null) {
			System.out.println(wordID + " = " + wordIDs.get(wordID));
			size++;
			if (size >= maxPrint) {
				break;
			}
		}
		System.out.println("");
		System.out.println("PAGEID LOOKUP");
		Integer pageID;
		size = 0;
		while((pageID = (Integer)iter5.next()) != null) {
			System.out.println(pageID + " = " + pageIDs.get(pageID));
			size++;
			if (size >= maxPrint) {
				break;
			}
		}

		System.out.println("");
		System.out.println("NON-INVERTED INDEX");
		Integer p;
		size = 0;
		while ((p = (Integer)iter6.next()) != null) {
			System.out.println(p + " = " + indexHash.get(p));
			size++;
			if (size >= maxPrint) {
				break;
			}
		}
	}

	public void clean() {
		recordManager.clean();
	}
}
