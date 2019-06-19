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
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class LZWdecode{
	private static Scanner sc;
	private static BufferedReader reader;
	private static LZWDecodeTrie trie;
	
	
	public static void main(String[] args){
		trie = new LZWDecodeTrie();
		sc = new Scanner(System.in);
		
		try{
			
			initializeTrie(); // initialize fake trie with node for each symbol
			
			while(sc.hasNextLine()){
				String line = sc.nextLine();
				int phrase = Integer.parseInt(line);
				trie.findPhrase(phrase);
			}
			
//			trie.printTriePaths();
			
		}catch(Exception e){
			e.printStackTrace(); // get error information
		}
	}
	
	// initializes fake trie with a node for each unique symbol in file
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
		
		trie.initializeTrie(initialPhrases);
		
		return true;
	}
}



/*
LZWDecodeTrie data structure holds a list of paths in a trie structure 
and has operations to find which path a phrase number belongs to and 
operations to add new phrase numbers to paths
*/

class LZWDecodeTrie{
	private static int numPhrases; // number of phrases in the trie
	private static ArrayList<Path> paths; // list of paths down the trie
	private static Path currentPath; // the current position in the trie
	private static int currentPathPhraseNum; // previous phraseNumber encounter in path
	private static boolean isFirstPhrase;
	
	
	public LZWDecodeTrie(){ // LZWDecodeTrie constructor
		numPhrases = 0;
		paths = new ArrayList<Path>();
		isFirstPhrase = true;
	}
	
	
	// creates a new path in the trie
	private void createNewPath(int currentPathPhraseNumber, byte data){
		Path p = currentPath.clone(currentPathPhraseNumber);
		p.extendPath(++numPhrases, data);
		paths.add(p);
	}
	
	// finds the given phrase in the trie
	public void findPhrase(int phraseNum){
		if(isFirstPhrase){
			isFirstPhrase = false;
			currentPath.printPath(-1);
		}else{
			
			int i = 0;
			Path cur = paths.get(i);
			while(!cur.hasPhraseNumber(phraseNum)){
				if(i+1== paths.size()){
					currentPath.extendPath(++numPhrases, currentPath.getHeadData());
					currentPathPhraseNum = phraseNum;
					currentPath.printPath(-1); // print entire path
					return;
				}
				
				cur = paths.get(++i);
			}
			
			/* if we're currently at the end of a path, extend it
			else create a new path which includes part of this one */
			if(currentPathPhraseNum == currentPath.getTailPhraseNumber())
				currentPath.extendPath(++numPhrases, cur.getHeadData());
			else
				createNewPath(currentPathPhraseNum, cur.getHeadData());
			
			currentPath = cur;
			currentPathPhraseNum = phraseNum;
			currentPath.printPath(phraseNum); // print part of this path
		}
	}
	
	// initializes trie with a node for each unique symbol in file
	public void initializeTrie(ArrayList<Integer> phrases){
        for(Integer phrase : phrases){
			int p = (int) phrase;
			paths.add(new Path(++numPhrases, (byte) p));
		}
		
//		for(Path p : paths) System.out.println(p);
		currentPath = paths.get(0);
		currentPathPhraseNum = paths.get(0).getTailPhraseNumber();
	}
	
	// prints all paths in the trie from root to end of path
	public void printTriePaths(){
		for(Path p : paths) p.printPath(-1);
	}
}


// data structure of linked list form holds a list of nodes
class Path{
	private Node head, tail;
	
	
	public Path(){ // default constructor
		head = null; tail = null;
	}
	
	public Path(int phraseNum, byte data){ // Path constructor
		head = new Node(phraseNum, data);
		tail = head;
	}
	
	
	// getters for head data and tail phrase number
	public byte getHeadData(){ return head.getData(); }
	public int getTailPhraseNumber(){ return tail.getPhraseNumber(); }
	
	// returns a clone of this path up to the given phrase number
	public Path clone(int phraseNum){
		Path path = new Path();
		Node cur = head;
		
		while(cur != null){
			path.extendPath(cur.getPhraseNumber(), cur.getData());
			if(cur.getPhraseNumber() == phraseNum) break;
			cur = cur.getNext();
		}
		
		return path;
	}
	
	// returns whether the given phrase number is in this Path
	public boolean hasPhraseNumber(int number){
		if(head == null) return false;
		
		Node cur = head;
		while(cur != null){
			if(cur.getPhraseNumber() == number) return true;
			cur = cur.getNext();
		}
		
		return false;
	}
	
	// extends a path with a new node
	public void extendPath(int phraseNum, byte data){
		Node n = new Node(phraseNum, data);
		
		if(head == null){
			head = n;
			tail = head;
		}else{
			tail.setNext(n);
		}
		
		tail = n;
	}
	
	/* if phraseNum = -1 method prints the entire path in the 
	trie ELSE it prints only up to phraseNum */
	public void printPath(int phraseNum){
		//System.out.print("pathid: "+this+" ");
		Node cur = head;
		while(cur != null){
			//System.out.print(cur.getPhraseNumber()+":"+cur.getData()+" ");
			System.out.write(cur.getData());
			if(phraseNum != -1 && cur.getPhraseNumber() == phraseNum) break;
			cur = cur.getNext();
		}
		//System.out.println();
	}
}


// self-referential data structure holds phrase number and byte of data
class Node{
	private int phraseNumber;
	private byte data;
	private Node next;
	
	public Node(int phraseNum, byte datum){ // Node constructor
		phraseNumber = phraseNum;
		data = datum;
		next = null;
	}
	
	// getters and setters for phrase number, data and next node
	public int getPhraseNumber(){ return phraseNumber; }
	public byte getData(){ return data; }
	public Node getNext(){ return next; }
	public void setNext(Node n){ next = n; }
}
