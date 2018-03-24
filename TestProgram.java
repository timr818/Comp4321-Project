import java.io.IOException;

public class TestProgram {

	TestProgram() {

	}
	
	public static void main (String[] args) {
		
		try {
			Spider spider = new Spider();

			DataManager dm = spider.search("http://www.cse.ust.hk/");

			dm.printAll();
			dm.finalize();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
}
