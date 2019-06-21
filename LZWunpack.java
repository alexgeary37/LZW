/*
	Alex Geary
	1188083
	
	Program receives a stream of bytes as input. The stream of bytes 
	is a list of integers representing phrase numbers for an LZW trie. 
	The number of bits used to represent each phrase number is basically 
	log2(number of phrases) in the trie. Each phrase number is essentially 
	represented in binary format but -1. So 4 is represented as 11 instead 
	of 100.
*/

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;


public class LZWunpack{
	private static BufferedInputStream inputStream; // reads stdin
	private static BufferedReader fileReader; // reads from triesetup file
	private static int numPhrases; // number of possible phrases in trie
	private static int outputInt;
	private static int outputIntBitsUsed;
	private static int counter;
	
	
	public static void main(String[] args){
		inputStream = new BufferedInputStream(System.in);
		outputInt = 0;
		outputIntBitsUsed = 0;
		counter = 1;
		
		try{
			
			/* number of initial phrases in trie + the 
			one gained from skipping first phrase number */
			numPhrases = getNumPhrases();
			
			outputPhraseNumber(1); // output first phrase number
			
			// read all of input stream
			int input = -1; // will contain the next byte of input
			int preparationInt = 0; // to hold 4 bytes of input
			int offset = 24; // shift input left offset amount
			
			int test = 0;
			while((input = inputStream.read()) != -1){
				preparationInt = preparationInt | input << offset;
				offset -= 8;
				if(counter == 259) return; // testing for BMPIMAGE unpacking
				if(offset == -8){
					prepareOutput(preparationInt, offset);
					preparationInt = 0;
					offset = 24;
					test++;
				}
			}
			
			prepareOutput(preparationInt, offset);
			
		}catch(Exception e){
			e.printStackTrace(); // print exception information
		}
	}
	
	
	/* returns the number of bits required for the phrase number, 
	log2(y) bits, where y is the number of phrases 
	currently read in */
	private static int getPhraseNumBitCount(int y){
		int x = 0;
		while((Math.pow(2, x)) < y) x++;
		return x;
	}
	
	// finds the phrase number to output
	private static void prepareOutput(int preparationInt, int offset){
		int prepInt = preparationInt;
		int numPrepBits;
		if(offset == -8) numPrepBits = 32;
		else if(offset == 0) numPrepBits = 24;
		else if(offset == 8) numPrepBits = 16;
		else if(offset == 16) numPrepBits = 8;
		else numPrepBits = 0;
		
		// testing for BMPIMAGE unpacking
		if(counter >= 254)System.out.println(Integer.toBinaryString(prepInt)+" initial "+offset);

		// while the number of bits in prepInt > numBits
		while(numPrepBits >= getPhraseNumBitCount(numPhrases)-outputIntBitsUsed){
			int numBits = getPhraseNumBitCount(numPhrases)-outputIntBitsUsed;
			
			int outputNumber = outputInt >>> 32-(outputIntBitsUsed+numBits);
			outputNumber = outputNumber | prepInt >>> 32-numBits;
			
			if(outputNumber == 0) break;
			
			outputIntBitsUsed = outputInt = 0;
			
			outputPhraseNumber(outputNumber);
			
			prepInt = prepInt << numBits;
			
			// testing for BMPIMAGE unpacking
			if(counter >= 254)System.out.println(Integer.toBinaryString(prepInt));
			
			numPrepBits -= numBits;
		}
		
		if(numPrepBits != 0){
			outputInt = prepInt;
			outputIntBitsUsed = numPrepBits;
		}else{
			
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
	
	// prints the phrase number to standard out
	private static void outputPhraseNumber(int phraseNum){
		System.out.println(phraseNum);
		numPhrases++;
		counter++;
	}
}
