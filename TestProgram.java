import java.io.IOException;

public class TestProgram {

	TestProgram() {

	}
	
	public static void main (String[] args) {
		
		try {
			DataManager dm = new DataManager();

			dm.addEntry(DataManager.BODY_ID, "word", "url");
			dm.addEntry(DataManager.BODY_ID, "otherword", "url");
			dm.addEntry(DataManager.BODY_ID, "word", "url2");
			dm.addEntry(DataManager.TITLE_ID, "titleword", "url");
			dm.addEntry(DataManager.LINKS_ID, "url", "url2");
			dm.addMetaData("url", "title", "2018-4-12", 12);
			dm.printAll();
			dm.finalize();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
}
