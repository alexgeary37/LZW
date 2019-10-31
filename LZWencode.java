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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;


public class LZWencode {
    private static LZWTrie trie; // LZW trie structure
    private static BufferedInputStream inputStream; // stdin reader reads file to be compressed
    private static FileInputStream fileInputStream; // reads from file to be compressed
    private static LZWpack packer;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java LZWencode <filename.suffix>");
        } else {

            // create initial try structure and input stream
            trie = new LZWTrie();
            packer = new LZWpack();
            inputStream = new BufferedInputStream(System.in);

            try {

                boolean initialize = initializeTrie(args[0]);
                if (initialize == false) {
                    return;
                }

                /* prints the number of unique bytes in the trie
                followed by all unique bytes for LZWpack */
                packer.outputUniqueBytes(trie.getUniqueBytes());

                int input = -1; // will contain the next byte of input

                // get past the first phrase num output which is predetermined to be '1'
                while ((input = inputStream.read()) != -1) {
                    trie.findByte((byte) input);
                    
                    if (trie.hasOutput()) {
                        trie.findByte((byte) input);
                        break;
                    }
                }

                /* while the end of input stream hasn't been reached,
                perform LZ78 encoding on input stream */
                while ((input = inputStream.read()) != -1) {
                    trie.findByte((byte) input);
                    
                    if (trie.hasOutput()) {
                        packer.prepareOutput(trie.getOutput());
                        trie.findByte((byte) input);
                    }
                }

                // finish output
                packer.prepareOutput(trie.finishOutput((byte) input));
                packer.finishOutput();

                inputStream.close(); // close inputStream

            } catch (Exception e) {
                e.printStackTrace(); // get error information
            }
        }
    }

    // initializes trie with all unique characters from file
    private static boolean initializeTrie(String file) throws Exception {
        ArrayList<Integer> initialPhrases = new ArrayList<Integer>();
        File f = new File(file);

        if (!f.exists()) {
            System.err.println("error: " + file + " does not exist");
            return false;
        }

        fileInputStream = new FileInputStream(f);

        int input = 0;
        while ((input = fileInputStream.read()) != -1) {
            if (!initialPhrases.contains(input)) {
                initialPhrases.add(input);
            }
        }

        fileInputStream.close();

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
equals n, where n is the number of phrases in the trie.
*/
class LZWTrie {
    // variables for each instance of Trie
    private int phraseNumber; // the number for each phrase in trie
    private int numBranches; // the number of branches in the calling trie
    private byte mismatch; // the byte/phrase in each subtrie
    private LZWTrie[] branches; // the subsequent phrases from each subtrie

    // variables that belong only to the class
    private static int numPhrases; // the number of phrases in the trie
    private static boolean hasOutput; // indicates whether there is a phrase number available for output
    private static LZWTrie currentTrie; // pointer to keep track of where in the trie the last two matches were
    private static int phraseNumOutput; // the phrase number to next output


    // initial trie constructor
    public LZWTrie() {
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
    public LZWTrie(int phraseNum, byte mismatch) {
        this.phraseNumber = phraseNum;
        this.mismatch = mismatch;
        this.branches = new LZWTrie[256];
        this.numBranches = 0;
    }


    // searches the trie from the current node, for the given byte b
    public void findByte(byte b) {
        if (currentTrie.numBranches != 0) {
            LZWTrie current = currentTrie.branches[0];

            int i = 0;
            do { // search through all branches for the input byte

                if (current.mismatch == b) {
                    currentTrie = current;
                    setOutputPhraseNumber(currentTrie.phraseNumber);
                    return;
                }
                
                i++;
                
                if (i < currentTrie.numBranches) {
                    current = currentTrie.branches[i];
                }

            } while (i < currentTrie.numBranches);
        }

        addPhrase(b); // add new phrase to trie
        hasOutput = true;
        currentTrie = this; // reset the currentTrie pointer to the root trie
    }

    // adds a new phrase to the trie
    public void addPhrase(byte mismatch) {
        if (currentTrie.numBranches == currentTrie.branches.length) {
            increaseBranches(); // handle number of branches is exceeding set length
        }

        // add new trie to the branches
        numPhrases++;
        currentTrie.branches[currentTrie.numBranches] = new LZWTrie(numPhrases, mismatch);
        numBranches++;
    }

    // searches trie for byte and adds it if it hasn't already been added
    public void initialize(ArrayList<Integer> phrases) {
        if (currentTrie.numBranches == currentTrie.branches.length) {
            increaseBranches(); // handle number of branches is exceeding set length
        }

        // add new trie to the branches
        for (Integer phrase : phrases) {
            int p = (int) phrase;
            numPhrases++;
            currentTrie.branches[currentTrie.numBranches] = new LZWTrie(numPhrases, (byte) p);
            numBranches++;
        }
    }

    // updates the phrase number of output tuple
    public void setOutputPhraseNumber(int phraseNum) {
        phraseNumOutput = phraseNum;
    }

    /* returns whether there is a phrase number
    which can be output */
    public boolean hasOutput() {
        if (hasOutput) {
            hasOutput = false;
            return true;
        }

        return false;
    }

    /* prints the number of unique bytes in the trie
    followed by all unique bytes for setting up LZWpack */
    public ArrayList<Integer> getUniqueBytes() throws Exception {
        ArrayList<Integer> uniqueBytes = new ArrayList<>();

        uniqueBytes.add(numBranches);
        for (int i = 0; i < numBranches; i++) {
            int mismatch = (int) branches[i].mismatch;
            uniqueBytes.add(mismatch);
        }

        return uniqueBytes;
    }

    // returns the output phrase number
    public int getOutput() throws Exception {
        return phraseNumOutput;
    }

    // returns the final phrase number
    public int finishOutput(byte b) throws Exception {
        if (currentTrie == this) {
            findLastByte(b);
        }

        return phraseNumOutput;
    }

    // finds the last byte and sets the output phrase number
    private void findLastByte(byte b) {
        LZWTrie current = currentTrie.branches[0];

        // find branch which matches input byte
        int i = 1;
        while (current.mismatch != b) {
            current = currentTrie.branches[i];
            i++;
        }

        setOutputPhraseNumber(current.phraseNumber);
    }

    // increases the capacity of array for currentTrie branches
    private void increaseBranches() {
        LZWTrie[] temp = new LZWTrie[currentTrie.numBranches * 2];
        for (int i = 0; i < currentTrie.numBranches; i++) {
            temp[i] = currentTrie.branches[i];
        }
        currentTrie.branches = temp;
    }
}

/*
LZW bit packer:

Program receives input as a stream of numbers, one per line created by the LZW encoder.
Each number is a phrase number which is output in log2p bits where p is the number of
phrases that the encoder has at that time of its output.
*/
class LZWpack {
    private static BufferedOutputStream outputStream; // writes to stdout
    private static int numPhrases; // to keep track of how many bits the phrase number needs to be packed with
    private static int numFreeOutputBits; // keeps track of how many bits are left to fill in output int
    private static int outputInt; // int to be filled with packed bits for output


    public LZWpack() {
        numFreeOutputBits = 32;
        outputInt = 0;
        outputStream = new BufferedOutputStream(System.out);
    }

    // returns the number of phrases the trie is initialized with
    public boolean outputUniqueBytes(ArrayList<Integer> uniqueBytes) throws Exception {
        numPhrases = uniqueBytes.get(0);

        System.out.println(numPhrases); // print number of unique bytes

        // write all unique bytes to output stream
        int i = 1;
        while (i <= numPhrases) {
            int b = uniqueBytes.get(i);
            outputStream.write((byte) b); // write unique byte
            outputStream.flush();
            i++;
        }

        numPhrases++;
        return true;
    }

    /* returns the number of bits required for the phrase number,
    log2(y) bits, where y is the number of phrases currently read in */
    private int getPhraseNumBitCount(int y) {
        int x = 0;
        while ((Math.pow(2, x)) <= y) {
            x++;
        }
        return x;
    }

    // prepares output int to be filled with phrase number and mismatch bits
    public void prepareOutput(int phraseNum) throws Exception {
        int numPhraseBits = getPhraseNumBitCount(numPhrases); // num bits for phrase number

        // pack the output int with the phrase number
        packPhrase(numPhraseBits, phraseNum);

        numPhrases++; // increment num phrases in the LZW trie
    }

    // packs output int with compressed phrase number bits
    private void packPhrase(int numPhraseBits, int phraseNum) throws Exception {
        if (numFreeOutputBits >= numPhraseBits) {

            // copy phraseNum bits into output int and update number of unused bits
            outputInt = outputInt | phraseNum << (numFreeOutputBits - numPhraseBits);
            numFreeOutputBits = numFreeOutputBits - numPhraseBits;
        } else {

            int shiftRightAmount = numPhraseBits - numFreeOutputBits;

            // fill rest of output int with limited num of phraseNum bits
            outputInt = outputInt | phraseNum >>> shiftRightAmount;
            writeOutput(4);
            
            /* copy remaining phraseNum bits into output int
            and update the number of unused bits */
            outputInt = phraseNum << 32 - shiftRightAmount;
            numFreeOutputBits -= shiftRightAmount;
        }
    }

    // output whatever is remaining in the outputInt
    public void finishOutput() throws Exception {
        if (numFreeOutputBits < 32) {
            if (((32 - numFreeOutputBits) % 8) == 0) {
                writeOutput((32 - numFreeOutputBits) / 8);
            } else {
                writeOutput((32 - numFreeOutputBits) / 8 + 1);
            }
        }
        
        outputStream.close();
    }

    // writes 4 bytes to output stream, possibly less if end of input has been reached
    private void writeOutput(int numBytes) throws Exception {
        int shiftAmount = 24;
        byte[] output = new byte[numBytes];

        // copy bits from output int into 4 separate bytes for writing to standard out
        for (int i = 0; i < output.length; i++) {
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
