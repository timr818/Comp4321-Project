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
import java.io.Serializable;

public class DataManager {

	public final String MANAGER_ID = "recman";
	public final String BODY_ID = "bodyid";
	public final String TITLE_ID = "titleid";

	DataManager() {
		createIndexTable(BODY_ID);
		createIndexTable(TITLE_ID);
	}

	private RecordManager recordManager;
	
	//Inverted index for the keywords in the pagebody of the pages
	private HTree pagebodyHash;

	//Inverted index for the keywords in the pagetitle of the pages
	private HTree pagetitleHash;

	//initializes the hash tables
	private void CreateIndexTable(String id) {
		
	}

	//finalizes the changes to the hashtables
	public void finalize() throws IOException {
		recordManager.commit();
		recordManager.close();
	}

	//add an entry into the hashtable (id specifies which hash table to add into)
	public void addEntry(String id, String data) throws IOException {

	}

	//deletes an entry from the hashtable (id specifies which has table to delete from)
	public void deleteEntry(String id, String data) throws IOException {

	} 
}
