import java.util.Scanner;
import java.util.Vector;
import java.io.IOException;

public class SearchProgram {

	private static final String FILE_NAME = "spider_result.txt";

	SearchProgram() {

	}

    public static void main (String[] args) {
		
		try {
			DataManager dm = new DataManager();
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter your query: ");
            String query = scanner.nextLine();

            String[] queryWords = query.split(" ");
            dm.querySimilarity(queryWords);

	    } catch (IOException e) {
            System.out.println(e);
        }
    }
}
