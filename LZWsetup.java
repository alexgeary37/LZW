/*
	Alex Geary
	1188083
	
	Program receives input as a stream of bytes and creates a list of all 
	unique byte values. These unique byte values are written to a file 
	which will be used to initialize the LZW trie structure.
*/

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;


public class LZWsetup{
	private static BufferedInputStream inputStream;
	private static BufferedWriter writer;
	private static ArrayList<Integer> uniqueBytes;
	
	
	public static void main(String[] args){
		uniqueBytes = new ArrayList<Integer>();
		
		try{
			
		    getUniqueBytes(); // get all unique bytes in to a list
			writeTrieSetupFile(); // create trie setup file
			
		}catch(Exception e){
			e.printStackTrace(); // get error information
		}
	}
	
	
	// gets all unique bytes from file for trie initialization
	private static void getUniqueBytes() throws Exception{
		inputStream = new BufferedInputStream(System.in);
		
		int input = 0;
		while((input = inputStream.read()) != -1)
			if(!uniqueBytes.contains(input)) uniqueBytes.add(input);
		
		inputStream.close();
	}
	
	// writes all unique bytes found to a trie setup file
	private static void writeTrieSetupFile() throws Exception{
		File tempFile = new File("trieSetup.txt");
		tempFile.createNewFile();
		
		writer = new BufferedWriter(new FileWriter(tempFile));
		
		for(Integer integer : uniqueBytes){
			int i = integer;
			writer.write((byte) i+"\n");
			writer.flush();
		}
		
		writer.close();
	}
}
