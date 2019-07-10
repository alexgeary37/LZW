File compressor and decompressor using LZW algorithm.

There are four programs, LZWencode, LZWpack, LZWunpack and LZWdecode.
LZWencode reads the file to be compressed and outputs the file as a series of numbers, each number seperated by a newline character.
This output is used as input for LZWpack which compresses each of these numbers in to as few bits as possible, log2 of the number of possible phrases output by the LZW algorithm.
This compressed version is output as bytes and can be redirected to a filename of choice given by the user in the command line.
LZWunpack reads as input the compressed version of the file and outputs a series of numbers, exactly the same as the output from LZWencode.
LZWdecode reads as input the output from LZWunpack and uses the LZW decoding algorithm with the use of a dictionary, recover the original file and output it as bytes which can be redirected to a filename of choice given by the user in the command line.
