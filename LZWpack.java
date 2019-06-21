/*
	Alex Geary
	1188083
	Samuel Vink
	1289304
	
	LZW bit packer:
	Program receives input as a stream of numbers, one per line created by the LZW encoder.
	Each number is a phrase number which is output in log2p bits where p is the number of 
	phrases that the encoder has at that time of its output.
*/

import java.util.Scanner;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;


public class LZWpack{
	private static Scanner sc;
	private static BufferedOutputStream outputStream;
	private static BufferedReader fileReader;
	private static int numPhrases; // to keep track of how many bits the phrase number needs to be packed with
	private static int numFreeOutputBits; // keeps track of how many bits are left to fill in output int
	private static int outputInt; // int to be filled with packed bits for output
	
	private static int counter; // test counter
	
	
	public static void main(String[] args){
	    counter = 0;
		numFreeOutputBits = 32;
		outputInt = 0;
		
		sc = new Scanner(System.in);
		outputStream = new BufferedOutputStream(System.out);
		
		try{
			
			// number of initial phrases in trie
			numPhrases = getNumPhrases()+1;
			
			sc.nextLine(); // first phrasenum is always 1 so it's redundant
			
			// read line of input until there's none left
			while(sc.hasNextLine()){
				String line = sc.nextLine();
				int phraseNumber = Integer.parseInt(line);
				prepareOutput(phraseNumber);
			}
			
			finishOutput(); // write any remaining bits to stdout
			
			outputStream.close();
			
		}catch(Exception e){
			e.printStackTrace(); // get error infomation
		}
	}
	
	
	// returns the number of phrases the trie is initialized with
	private static int getNumPhrases() throws Exception{
		File f = new File("trieSetup.txt");
		fileReader = new BufferedReader(new FileReader(f));
		
		int numPhrases = 0;
		String line;
		while((line = fileReader.readLine()) != null) numPhrases++;
		
		fileReader.close();
		return numPhrases;
	}
	
	/* returns the number of bits required for the phrase number, 
	log2(p) bits, where p is the number of phrases currently read in */
	private static int getPhraseNumBitCount(int y){
		int x = 0;
		while((Math.pow(2, x)) < y) x++;
		return x;
	}
	
	// prepares output int to be filled with phrase number and mismatch bits
	private static void prepareOutput(int phraseNum) throws Exception{
		// number of bits for the phrase number
		int numPhraseBits = getPhraseNumBitCount(numPhrases);
		
		// pack the output int with the phrase number
		packPhrase(numPhraseBits, --phraseNum);
		
		numPhrases++; // increment num phrases in the LZW trie
	}
	
	// packs output int with compressed phrase number bits
	private static void packPhrase(int numPhraseBits, int phraseNum) throws Exception{
		if(numFreeOutputBits >= numPhraseBits){
			// copy phraseNum bits into output int
			outputInt = outputInt | phraseNum << (numFreeOutputBits-numPhraseBits);
			//System.out.print(" "+Integer.toBinaryString(outputInt));
			numFreeOutputBits = numFreeOutputBits-numPhraseBits; // update number of unused bits
		}else{
			int shiftRightAmount = numPhraseBits-numFreeOutputBits;
			
			// fill rest of output int with limited num of phraseNum bits
			outputInt = outputInt | phraseNum >>> shiftRightAmount;
			//System.out.print(" "+Integer.toBinaryString(outputInt));
			writeOutput(4);
			
			// copy remaining phraseNum bits into output int
			outputInt = phraseNum << 32-shiftRightAmount;
			//System.out.print(" "+Integer.toBinaryString(outputInt));
			numFreeOutputBits -= shiftRightAmount; // update the num of unused bits
		}
		//System.out.println();
	}
	
	// output whatever is remaining in the outputInt
	private static void finishOutput() throws Exception{
		if(numFreeOutputBits < 32)
			writeOutput((32-numFreeOutputBits)/8+1);
	}
	
	// writes 4 bytes to output stream, possibly less if end of input has been reached
	private static void writeOutput(int numBytes) throws Exception{
		int shiftAmount = 24;
		byte[] output = new byte[numBytes];
		
		// copy bits from output int into 4 separate bytes for writing to standard out
		for(int i = 0; i < output.length; i++){
		     output[i] = (byte) (outputInt >>> shiftAmount);
		     shiftAmount -= 8;
		}
		
		counter++;
		outputStream.write(output);
		outputStream.flush();
		
        // clear all outputint bits and update number of unused bits
		outputInt = 0;
		numFreeOutputBits = 32;
	}
}
