/*
	Alex Geary
	1188083
	Samuel Vink
	1289304
	
	LZ78 bit packer:
	Program receives input as a stream of bytes in the form of tuples created by the LZ78 encoder.
	Each tuple consists of a phrase number followed by a mismatch character, each separated by a 
	new line character. Program takes the bits from each byte of the tuples and outputs them as a 
	stream of bytes and it outputs the phrase number byte in log2p bits where p is the number of 
	phrases that the encoder has at that time of its output.
*/


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;


public class LZpack{
	private static BufferedInputStream inputStream;
	private static BufferedOutputStream outputStream;
	private static int numPhrases; // to keep track of how many bits the phrase number needs to be packed with
	private static int numFreeOutputBits; // keeps track of how many bits are left to fill in output int
	private static byte[] inputLine; // array to obtain the phrase number and mismatch pairs
	private static int outputInt; // int to be filled with packed bits for output
	
	private static int counter; // test counter
	
	
	public static void main(String[] args){
	    counter = 0;
		int input = 0; // contains byte read in from standard input
		boolean foundNewLine = false; // indicates whether the inputLine contains a newline character
		
		numPhrases = 1;
		numFreeOutputBits = 32;
		outputInt = 0;
		inputStream = new BufferedInputStream(System.in);
		outputStream = new BufferedOutputStream(System.out);
		
		
		
		try{
		     
			// while there is still input
			while((input = inputStream.read()) != -1){
				addToArray((byte) input);
				
				// if input is a newline character
				if(input == 10){
					if(foundNewLine != true) {
						foundNewLine = true; // indicate that the first newline has been found
					}else{
						if((input = inputStream.read()) == -1) break; // finish below loop
						
						// compress phrase number
						addToArray((byte) input);
						compress(false);
						foundNewLine = false;
					}
				}else{
				     	// if newline was found, compress phrase number
					if(foundNewLine == true){
						compress(false);
						foundNewLine = false;
					}
				}
				
			}
			
			// finish by compressing and outputting last bytes
			if(inputLine != null) compress(true);
			
			// close input and output streams
			inputStream.close();
			outputStream.close();
			
		}catch(Exception e){
			e.printStackTrace(); // get error infomation
		}
	}
	
	
	
	/* returns the number of bits required for the phrase number, 
	log2(p) bits, where p is the number of phrases currently read in */
	private static int getPhraseNumBitCount(int y){
		int x = 0;
		while((Math.pow(2, x)) < y) x++;
		return x;
	}
	
	// adds byte b to the array which contains current line of input
	private static void addToArray(byte b){
		if(inputLine == null){
			inputLine = new byte[1]; // create array for first byte of input line
			inputLine[0] = b;
		}else{
		
		     	// increase inputline then add input b
			byte[] temp = new byte[inputLine.length+1];
			for(int i = 0; i < inputLine.length; i++)
			     temp[i] = inputLine[i];
			
			inputLine = temp;
			inputLine[inputLine.length-1] = b;
		}
	}
	
	// compresses the current line of input
	private static void compress(boolean finishedInput) throws Exception{
		int maxIndex = (finishedInput)? inputLine.length-3 : inputLine.length-4;
		
		// get phrase number digits into a single 32bit int and get mismatch
		String phraseNumString = "";
		for(int i = 0; i <= maxIndex; i++)
		     phraseNumString += (char) inputLine[i];
		int phraseNumber = Integer.parseInt(phraseNumString);
		
		byte mismatch = inputLine[maxIndex+1];
		
		// pack phrase num and reset inputline for next line from encoder
		prepareOutput(phraseNumber, mismatch, finishedInput);
	}
	
	// prepares output int to be filled with phrase number and mismatch bits
	private static void prepareOutput(int phraseNum, byte mismatch, boolean finishedInput) throws Exception{
		int numPhraseBits = getPhraseNumBitCount(numPhrases); // number of bits for the phrase number
		
		// pack the output int with phrase num followed by mismatch
		packPhrase(numPhraseBits, phraseNum);
		packMismatch(mismatch, finishedInput);
		
		// if there is input remaining, set last byte read in as first byte of new inputline
		byte lastByte = inputLine[inputLine.length-1];
		inputLine = null;
		if(!finishedInput) addToArray(lastByte);
		
		numPhrases++; // increment num phrases in the LZ78 dictionary
	}
	
	// packs output int with compressed phrase number bits
	private static void packPhrase(int numPhraseBits, int phraseNum) throws Exception{
		if(numFreeOutputBits >= numPhraseBits){
			outputInt = outputInt | phraseNum << (numFreeOutputBits-numPhraseBits); // copy phraseNum bits into output int
			numFreeOutputBits = numFreeOutputBits-numPhraseBits; // update number of unused bits in output int
		}else{
			int shiftRightAmount = numPhraseBits-numFreeOutputBits;
			outputInt = outputInt | phraseNum >>> shiftRightAmount; // fill rest of output int with limited num of phraseNum bits
			writeOutput(4);
			outputInt = phraseNum << 32-shiftRightAmount; // copy remaining phraseNum bits into output int
			numFreeOutputBits -= shiftRightAmount; // update the num of unused bits in output int
		}
	}
	
	// packs output int with mismatch
	private static void packMismatch(byte mismatch, boolean finishedInput) throws Exception{
		int intMismatch = 255;
		intMismatch = intMismatch & mismatch;
		
		if(numFreeOutputBits >= 8){
			outputInt = outputInt | intMismatch << (numFreeOutputBits-8); // copy mismatch bits into output int
			numFreeOutputBits = numFreeOutputBits-8; // update number of unused bits in output int
		}else{
			int shiftRightAmount = 8-numFreeOutputBits;
			outputInt = outputInt | intMismatch >>> shiftRightAmount; // fill rest of output int with limited num of mismatch bits
			writeOutput(4);
			outputInt = intMismatch << 32-shiftRightAmount; // copy remaining mismatch bits into output int
			numFreeOutputBits -= shiftRightAmount; // update the num of unused bits in output int
		}
		
		// if this is last mismatch then output final bytes
		if(finishedInput)
			if(numFreeOutputBits < 32) // this was added recently !!!!!!!!!!!!!!!!!!
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
		
		counter++;
		outputStream.write(output);
		outputStream.flush();
		
        // clear all outputint bits and update number of unused bits
		outputInt = 0;
		numFreeOutputBits = 32;
	}
}
