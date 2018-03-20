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
import java.io.IOException;

public class DataManager {

	public static final String MANAGER_ID = "recman";
	public static final String BODY_ID = "bodyid";
	public static final String TITLE_ID = "titleid";
	
	private RecordManager recordManager;
	
	//Inverted index for the keywords in the pagebody of the pages
	private HTree pagebodyHash;

	//Inverted index for the keywords in the pagetitle of the pages
	private HTree pagetitleHash;

	DataManager() throws IOException {
		recordManager = RecordManagerFactory.createRecordManager(MANAGER_ID);
		createIndexTable(BODY_ID);
		createIndexTable(TITLE_ID);
	}

	//initializes the hash tables
	private void createIndexTable(String id) throws IOException {
		long hashid = recordManager.getNamedObject(id);

		if (hashid != 0) {
			if (id.equals(BODY_ID)) {
				pagebodyHash = HTree.load(recordManager, hashid);
			} else {
				pagetitleHash = HTree.load(recordManager, hashid);
			}
		} else {
			if (id.equals(BODY_ID)) {
				pagebodyHash = HTree.createInstance(recordManager);
				recordManager.setNamedObject(id, pagebodyHash.getRecid());
			} else {
				pagetitleHash = HTree.createInstance(recordManager);
				recordManager.setNamedObject(id, pagetitleHash.getRecid());
			}
		}
	}

	//finalizes the changes to the hashtables
	public void finalize() throws IOException {
		recordManager.commit();
		recordManager.close();
	}

	//add an entry into the hashtable (id specifies which hash table to add into)
	public void addEntry(String id, String keyword, String data) throws IOException {
		HTree hash;
		if (id.equals(BODY_ID)) {
			hash = pagebodyHash;
		} else {
			hash = pagetitleHash;
		}

		hash.put(keyword, data);
	}

	//deletes an entry from the hashtable (id specifies which has table to delete from)
	public void deleteEntry(String id, String keyword) throws IOException {
		HTree hash;
		if (id.equals(BODY_ID)) {
			hash = pagebodyHash;
		} else {
			hash = pagetitleHash;
		}

		hash.remove(keyword);
	} 

	public void printAll() throws IOException {
		FastIterator iter1 = pagebodyHash.keys();
		FastIterator iter2 = pagetitleHash.keys();
		String key;
		while((key=(String)iter1.next()) != null) {
			System.out.println(key + " = " + pagebodyHash.get(key));

		}

		String a;
		while((a = (String)iter2.next()) != null) {
			System.out.println(a + " = " + pagetitleHash.get(a));
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
