import java.io.*;
import java.util.Vector;

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
				
				//print keywords
				Vector<String> words = dm.getKeywordsAndFreq(i);
				for (int j = 0; j < words.size(); j++) {
					if (j != 0) {
						bw.write("; " + words.get(j));
					} else {
						bw.write(words.get(j));
					}
				}
			
				//print links
				Vector<Integer> pageIDs = dm.getLinks(i);
				for (int pID : pageIDs) {
					bw.write(dm.getURL(pID) + "\n");
				}
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
