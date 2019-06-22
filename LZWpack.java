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
	private static Scanner sc; // reads phrase numbers from stdin
	private static BufferedOutputStream outputStream; // writes to stdout
	private static int numPhrases; // to keep track of how many bits the phrase number needs to be packed with
	private static int numFreeOutputBits; // keeps track of how many bits are left to fill in output int
	private static int outputInt; // int to be filled with packed bits for output
	
	
	
	public static void main(String[] args){
		numFreeOutputBits = 32;
		outputInt = 0;
		
		sc = new Scanner(System.in);
		outputStream = new BufferedOutputStream(System.out);
		
		try{
			
			/* number of initial phrases in trie + the 
			one gained from skipping first phrase number */
			if(!outputUniqueBytes()) return;
			
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
	private static boolean outputUniqueBytes() throws Exception{
		String line;
		
		if(sc.hasNextLine()) line = sc.nextLine();
		else return false;
		
		try{
			numPhrases = Integer.parseInt(line);
		}catch(Exception e){
			System.err.println("Error: First line of input was not numeric");
			return false;
		}
		
		System.out.println(numPhrases); // print number of unique bytes
		
		// write all unique bytes to output stream
		int i = 0;
		while(i++ < numPhrases){
			line = sc.nextLine();
			int b = Integer.parseInt(line);
			outputStream.write((byte) b); // write unique byte
			outputStream.flush();
		}
		
		/* first phrasenum is always 1 so it's redundant 
		and number of phrases can be increased */
		sc.nextLine();
		numPhrases++;
		return true;
	}
	
	/* returns the number of bits required for the phrase number, 
	log2(y) bits, where y is the number of phrases currently read in */
	private static int getPhraseNumBitCount(int y){
		int x = 0;
		while((Math.pow(2, x)) <= y) x++;
		return x;
	}
	
	// prepares output int to be filled with phrase number and mismatch bits
	private static void prepareOutput(int phraseNum) throws Exception{
		int numPhraseBits = getPhraseNumBitCount(numPhrases); // num bits for phrase number
		
		// pack the output int with the phrase number
		packPhrase(numPhraseBits, phraseNum);
		
		numPhrases++; // increment num phrases in the LZW trie
	}
	
	// packs output int with compressed phrase number bits
	private static void packPhrase(int numPhraseBits, int phraseNum) throws Exception{
		if(numFreeOutputBits >= numPhraseBits){
			// copy phraseNum bits into output int and update number of unused bits
			outputInt = outputInt | phraseNum << (numFreeOutputBits-numPhraseBits);
			numFreeOutputBits = numFreeOutputBits-numPhraseBits;
		}else{
			
			int shiftRightAmount = numPhraseBits-numFreeOutputBits;
			
			// fill rest of output int with limited num of phraseNum bits
			outputInt = outputInt | phraseNum >>> shiftRightAmount;
			writeOutput(4);
			
			/* copy remaining phraseNum bits into output int 
			and update the number of unused bits */
			outputInt = phraseNum << 32-shiftRightAmount;
			numFreeOutputBits -= shiftRightAmount;
		}
	}
	
	// output whatever is remaining in the outputInt
	private static void finishOutput() throws Exception{
		if(numFreeOutputBits < 32)
			if(((32-numFreeOutputBits)%8) == 0)
				writeOutput((32-numFreeOutputBits)/8);
			else
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
		
		outputStream.write(output);
		outputStream.flush();
		
        // clear all outputint bits and update number of unused bits
		outputInt = 0;
		numFreeOutputBits = 32;
	}
}
