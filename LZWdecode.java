/*
	Alex Geary
	1188083

	Program receives input from .... which are phrase numbers for
	a trie datastructure. Trie is initialized with the same nodes
	as the LZW encoder and each phrase number that is received is
	searched for in the trie and the data within each node on the
	path down to the first mismatch is printed as output. The first
	mismatch is added on to the end of that path in the trie, with
	a new phrase number and the mismatch symbol included in the node.
*/

import java.util.Scanner;
import java.util.ArrayList;
import java.io.BufferedOutputStream;


public class LZWdecode {
    private static Scanner sc; // reads stdin
    private static LZWDecodeTrie trie; // LZWtrie structure


    public static void main(String[] args) {
        trie = new LZWDecodeTrie();
        sc = new Scanner(System.in);

        try {

            initializeTrie(); // initialize fake trie with node for each symbol
            sc.nextLine(); // skip first line as phrase has already been written

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                int phrase = Integer.parseInt(line);
                trie.findPhrase(phrase);
            }

        } catch (Exception e) {
            e.printStackTrace(); // get error information
        }
    }

    // initializes fake trie with a node for each unique symbol in file
    private static boolean initializeTrie() throws Exception {
        ArrayList<Integer> initialPhrases = new ArrayList<Integer>();

        String line = sc.nextLine();
        int numPhrases = Integer.parseInt(line);
        int i = 0;
        while (i++ < numPhrases) {
            line = sc.nextLine();
            int uniqueByte = Integer.parseInt(line);
            initialPhrases.add(uniqueByte);
        }

        trie.initializeTrie(initialPhrases);
        return true;
    }
}



/*
LZWDecodeTrie data structure holds a list of paths in a trie structure
and has operations to find which path a phrase number belongs to and
operations to add new phrase numbers to paths
*/

class LZWDecodeTrie {
    private static int numPhrases; // number of phrases in the trie
    private static int currentPathPhraseNum; // previous phraseNumber encounter in path
    private static boolean isFirstPhrase; // will be set to false after 1st phrase written
    private static int[][] dict; // holds phrase numbers and mismatches of LZWtrie
    private static BufferedOutputStream outputStream;


    public LZWDecodeTrie() { // LZWDecodeTrie constructor
        numPhrases = 0;

        dict = new int[1024][2];
        dict[0][0] = 0;
        dict[0][1] = 0;
        outputStream = new BufferedOutputStream(System.out);
    }


    // initializes trie with a node for each unique symbol in file
    public void initializeTrie(ArrayList<Integer> phrases) throws Exception {
        for (Integer phrase : phrases) {
            int p = (int) phrase;
            addToDictionary(0, p);
        }

        currentPathPhraseNum = 1;
        printPath(1);
    }

    /* finds the given phrase in the trie and adds the
    next one to the dictionary */
    public void findPhrase(int phraseNum) throws Exception {
        if (phraseNum == numPhrases + 1) {
            addToDictionary(currentPathPhraseNum,
                    currentPathHeadValue(currentPathPhraseNum));
        } else {
            addToDictionary(currentPathPhraseNum,
                    currentPathHeadValue(phraseNum));
        }

        currentPathPhraseNum = phraseNum;
        printPath(phraseNum);
    }

    // adds a new phrase to the dictionary
    private void addToDictionary(int phraseNum, int mismatch) {
        if (++numPhrases == dict.length) {
            int[][] temp = new int[numPhrases + 1024][2];

            for (int i = 0; i < dict.length; i++) {
                temp[i][0] = dict[i][0];
                temp[i][1] = dict[i][1];
            }

            dict = temp;
        }

        dict[numPhrases][0] = phraseNum;
        dict[numPhrases][1] = mismatch;
    }

    // returns the mismatch of the first node in the phrase path
    private int currentPathHeadValue(int phraseNum) {
        int phraseNumber = phraseNum;
        int number = dict[phraseNumber][0];

        while (number != 0) {
            phraseNumber = number;
            number = dict[phraseNumber][0];
        }

        return dict[phraseNumber][1];
    }

    // prints all bytes that make up this phrase in dictionary
    private void printPath(int phraseNum) throws Exception {
        if (dict[phraseNum][0] != 0) printPath(dict[phraseNum][0]);

        outputStream.write(dict[phraseNum][1]);
        outputStream.flush();
    }
}
