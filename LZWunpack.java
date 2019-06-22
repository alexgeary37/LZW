/*
	Alex Geary
	1188083

	Program receives a stream of bytes as input. The stream of bytes
	is a list of integers representing phrase numbers for an LZW trie.
	The number of bits used to represent each phrase number is basically
	log2(number of phrases) in the trie. Each phrase number is represented
	in binary format.
*/

import java.io.BufferedInputStream;


public class LZWunpack {
    private static BufferedInputStream inputStream; // reads stdin
    private static int numPhrases; // number of possible phrases in trie

    // holds left over bits which partially made the phrase number
    private static int leftOverOutputBits;
    private static int numLeftOverOutputBits; // number of bits


    public static void main(String[] args) {
        leftOverOutputBits = 0;
        numLeftOverOutputBits = 0;
        inputStream = new BufferedInputStream(System.in);

        try {

            // output all the unique bytes
            outputUniqueBytes();

            outputPhraseNumber(1); // output first phrase number

            int input = -1; // contains the next byte of input
            int offset = 24; // offset amount for filling preparationInt with input

			/* holds 4 bytes of input which, once either full or the input
			has ended, can be prepared for output, numPhrases bits at a time */
            int preparationInt = 0;

            // read all of input stream
            while ((input = inputStream.read()) != -1) {

                // fill prepInt from left to right with each input byte
                preparationInt = preparationInt | input << offset;
                offset -= 8; // update offset to shift next input byte to the left by

				/* 4 bytes have been read in to preparationInt so
				it's ready to be output */
                if (offset == -8) {
                    outputPreparationInt(preparationInt, 32);
                    preparationInt = 0;
                    offset = 24;
                }
            }

            // finish output with whatever remains in the preparationInt
            finishOutput(preparationInt, offset);

        } catch (Exception e) {
            e.printStackTrace(); // print exception information
        }
    }


    // returns the number of phrases the trie is initialized with
    private static void outputUniqueBytes() throws Exception {
        String s = "";
        int numPhraseInput = 0;
        while ((numPhraseInput = inputStream.read()) != 13)
            s += Integer.toString(numPhraseInput - 48);

        numPhrases = Integer.parseInt(s); // initialize number of phrases

        System.out.println(numPhrases); // print number of unique bytes

        inputStream.read(); // read the \n character from println in LZWpack

        // write all unique bytes to output stream
        int i = 0;
        while (i++ < numPhrases) {
            int input = inputStream.read();
            System.out.println(input); // write unique bytes
        }
    }

    /* returns the number of bits required for the phrase number,
    log2(y) bits, where y is the number of phrases
    currently read in */
    private static int getPhraseNumBitCount(int y) {
        int x = 0;
        while ((Math.pow(2, x)) <= y) x++;
        return x;
    }

    // finishes outputting the preparationInt
    private static void finishOutput(int preparationInt, int offset) {
        int numPrepBits;

        // this covers any anomaly which may occur at end of unpacking
        if (offset == -8) numPrepBits = 32;
        else if (offset == 0) numPrepBits = 24;
        else if (offset == 8) numPrepBits = 16;
        else if (offset == 16) numPrepBits = 8;
        else numPrepBits = 0;

        outputPreparationInt(preparationInt, numPrepBits);
    }

    /* outputs numPhrases worth of bits from the preparationInt
    which has just been filled by a number of bytes of input */
    private static void outputPreparationInt(int preparationInt, int numPrepBitsLeft) {
        int prepInt = preparationInt;
        int prepBitsRemaining = numPrepBitsLeft;

		/* while the number of bits in the preparation int
		is more than num bits to output */
        while (prepBitsRemaining >= getPhraseNumBitCount(numPhrases) - numLeftOverOutputBits) {

            // number of bits which consitute the output number
            int numBits = getPhraseNumBitCount(numPhrases) - numLeftOverOutputBits;
            int outputNumber = leftOverOutputBits >>> 32 - (numLeftOverOutputBits + numBits);
            outputNumber = outputNumber | prepInt >>> 32 - numBits;
            numLeftOverOutputBits = leftOverOutputBits = 0;

			/* there will never be a zero phrase number
			so this means unpacking is finished */
            if (outputNumber == 0) break;

            outputPhraseNumber(outputNumber); // output number to stdout

            // update prepInt and the number of bits left to output
            prepInt = prepInt << numBits;
            prepBitsRemaining -= numBits;
        }

        // save bits for following round of output
        if (prepBitsRemaining != 0) {
            leftOverOutputBits = prepInt;
            numLeftOverOutputBits = prepBitsRemaining;
        }
    }

    // prints the phrase number to standard out
    private static void outputPhraseNumber(int phraseNum) {
        System.out.println(phraseNum);
        numPhrases++; // increment number of possible phrases in trie
    }
}
