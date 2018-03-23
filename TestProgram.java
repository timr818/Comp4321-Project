import java.io.IOException;

public class TestProgram {

	TestProgram() {

	}
	
	public static void main (String[] args) {
		
		try {
			DataManager dm = new DataManager();
		
			dm.addEntry(DataManager.BODY_ID, "sham", "wow");
			dm.addEntry(DataManager.BODY_ID, "sham", "dunk");
			dm.addEntry(DataManager.TITLE_ID, "smell", "me");
			dm.printAll();
			dm.finalize();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
}
