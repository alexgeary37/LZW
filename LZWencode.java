/*
	Alex Geary
	1188083
	
	LZW encoder:
	Program initializes a trie structure with all the unique bytes read 
	in from a file which contains all the unique bytes from the file to 
	be compressed. Program receives standard input of a file and proceeds 
	to compress the file using the LZW algorithm. Output of program is 
	a list of phrase numbers which will be compressed by a bit packer.
*/

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;


public class LZWencode{
	private static LZWTrie trie;
	private static BufferedInputStream inputStream;
	private static BufferedReader reader;
	
	
	public static void main(String[] args){
		
		// create initial try structure and input stream
		trie = new LZWTrie();
		inputStream = new BufferedInputStream(System.in);
		
		try{
			
		    if(initializeTrie() == false) return;
		    
			int input = -1; // contains the next byte of input
		    byte first = -1;
			byte next = -1;
		    int i = 0;
			/* while the end of input stream hasn't been reached, 
			perform LZ78 encoding on input stream */
			while((input = inputStream.read()) != -1){
				if(next != -1) first = next;
				
				next = (byte) input;
				//System.out.write(next);
				trie.findByte(next);
				if(trie.hasOutput()){
					trie.dumpOutput();
					trie.findByte(next);
				}
				//if(++i == 200) break;
			}
			
			trie.finishOutput(next);
			
			inputStream.close(); // close inputStream
			
		}catch(Exception e){
			e.printStackTrace(); // get error information
		}
	}
	
	// initializes trie with all unique characters from file
	private static boolean initializeTrie() throws Exception{
		File f = new File("trieSetup.txt");
		ArrayList<Integer> initialPhrases = new ArrayList<Integer>();
		
		if(!f.exists()){
			System.err.println("error: trieSetup.txt does not exist");
			return false;
		}
		
		reader = new BufferedReader(new FileReader(f));
		
		String line;
		while((line = reader.readLine()) != null){
			int b = Integer.parseInt(line);
			initialPhrases.add(b);
		}
		
		reader.close();
		
		trie.initialize(initialPhrases);
		
		return true;
	}
}


/*
LZWTrie datastructure:
Datastructure is a self referential datastructure which includes 
a reference to an array of itself i.e an array of trie's. The array of 
trie's references branches off of the current trie.
Whenever a new phrase is found in the trie, the number of that phrase 
is saved as an int called phraseNumOutput. This is overwritten when the 
next phrase is found in the trie. When there appears a mismatch, a new 
trie is added as a branch off of the current trie and its phrase number 
equals n, where n is the number of phrases in the trie. */

class LZWTrie{
	// variables for each instance of Trie
	private int phraseNumber, numBranches; // the number for each phrase in the trie
	private byte mismatch; // the byte/phrase in each subtrie
	private LZWTrie[] branches; // the subsequent phrases from each subtrie
	
	// variables that belong only to the class
	private static int numPhrases; // the number of phrases in the trie
	private static boolean hasOutput; // indicates whether there is a phrase number available for output
	private static LZWTrie currentTrie; // pointer to keep track of where in the trie the last two matches were
	private static int phraseNumOutput; // the phrase number to next output
	
	
	
	// initial trie constructor
	public LZWTrie(){
		phraseNumber = 0;
		mismatch = (byte) 0;
		branches = new LZWTrie[256];
		numBranches = 0;
		numPhrases = 0;
		currentTrie = this;
		hasOutput = false;
		phraseNumOutput = 0;
	}
	
	// constructor for subsequent tries / sub-tries
	public LZWTrie(int phraseNum, byte mismatch){
		this.phraseNumber = phraseNum;
		this.mismatch = mismatch;
		this.branches = new LZWTrie[256];
		this.numBranches = 0;
	}
	
	
	
	// searches the trie from the current node, for the given byte b
	public void findByte(byte b){
		if(currentTrie.numBranches != 0){
			LZWTrie current = currentTrie.branches[0];
			
			int i = 0;
			do{ // search through all branches for the input byte
				
				if(current.mismatch == b){
					currentTrie = current;
					setOutputPhraseNumber(currentTrie.phraseNumber);
					return;
				}else{
					if(i+1 < currentTrie.numBranches)
						current = currentTrie.branches[i+1];
				}
				
			}while(++i < currentTrie.numBranches);
		}
		
		addPhrase(b); // add new phrase to trie
		hasOutput = true;
		currentTrie = this; // reset the currentTrie pointer to the root trie
	}
	
	// adds a new phrase to the trie
	public void addPhrase(byte mismatch){
		if(currentTrie.numBranches == currentTrie.branches.length)
			increaseBranches(); // handle number of branches is exceeding set length
        
        // add new trie to the branches
        currentTrie.branches[currentTrie.numBranches++] = new LZWTrie(++numPhrases, mismatch);
	}
	
	// searches trie for byte and adds it if it hasn't already been added
	public void initialize(ArrayList<Integer> phrases){
		if(currentTrie.numBranches == currentTrie.branches.length)
			increaseBranches(); // handle number of branches is exceeding set length
        
        // add new trie to the branches
        for(Integer phrase : phrases){
			int p = (int) phrase;
			currentTrie.branches[currentTrie.numBranches++] = new LZWTrie(++numPhrases, (byte) p);
		}
	}
	
	// updates the phrase number of output tuple
	public void setOutputPhraseNumber(int phraseNum){
		phraseNumOutput = phraseNum;
	}
	
	/* returns whether there is a phrase number 
	which can be output */
	public boolean hasOutput(){
		if(hasOutput){
			hasOutput = false;
			return true;
		}
		return false;
	}
	
	// prints the output phrase number
	public void dumpOutput() throws Exception{
		System.out.println(phraseNumOutput); // print the phrase number
	}
	
	// prints the final phrase number
	public void finishOutput(byte b) throws Exception{
		if(currentTrie == this) findLastByte(b);
		dumpOutput();
	}
	
	// finds the last byte and sets the output phrase number
	private void findLastByte(byte b){
		LZWTrie current = currentTrie.branches[0];
		
		// find branch which matches input byte
		int i = 0;
		while(current.mismatch != b) current = currentTrie.branches[++i];
		
		setOutputPhraseNumber(current.phraseNumber);
	}
	
	// increases the capacity of array for currentTrie branches
	private void increaseBranches(){
		LZWTrie[] temp = new LZWTrie[currentTrie.numBranches*2];
		for(int i = 0; i < currentTrie.numBranches; i++)
			temp[i] = currentTrie.branches[i];
		currentTrie.branches = temp;
	}
}
