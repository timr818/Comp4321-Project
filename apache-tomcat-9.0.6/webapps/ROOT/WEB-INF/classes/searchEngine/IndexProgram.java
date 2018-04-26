package searchEngine;

import java.io.*;
import java.util.Vector;

public class IndexProgram {

	private static final String FILE_NAME = "spider_result.txt";

	public IndexProgram() {

	}
	
	public static void main (String[] args) {
		
		try {
			
			Spider spider = new Spider();
			DataManager dm = spider.search("http://www.cse.ust.hk/");
			dm.printAll();
			dm.finalize();
			
			
			/*
			//TEST IF DB IS PRESENT
			Vector<Integer> intVector = new Vector<Integer>();
			DataManager dm = new DataManager();
			Porter porter = new Porter();
			String word = "computer science";
			String[] arr = word.split(" ");
			
			for(int i = 0; i < arr.length; i++){
				arr[i] = porter.stripAffixes(arr[i]);
				System.out.println(arr[i]);
			}

			intVector = dm.querySimilarity(arr);
			System.out.println(intVector);
			*/
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
}
