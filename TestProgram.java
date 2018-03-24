import java.io.*;

public class TestProgram {

	private static final String FILE_NAME = "spider_result.txt";

	TestProgram() {

	}
	
	public static void main (String[] args) {
		
		try {
			Spider spider = new Spider();

			DataManager dm = spider.search("http://www.cse.ust.hk/");

			FileWriter fw = new FileWriter(FILE_NAME);
			BufferedWriter bw = new BufferedWriter(fw);
		
			for (int i = 0; i < 30; i++) {
				bw.write(dm.getPageTitle(i) + "\n");
				bw.write(dm.getURL(i) + "\n");
				bw.write(dm.getModifiedDate(i) + ", " + dm.getPageSize(i) + "\n");
				Vector<int> wordIDs = dm.getKeywords(i);
				for (int wID : wordIDs) {
					bw.write("; " + dm.getWordFromID(wID));
				}
				bw.write("\n");
				bw.write("\n---------------\n\n");
			}

			dm.printAll();
			
			bw.close();
			fw.close();
			dm.finalize();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
}
