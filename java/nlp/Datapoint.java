/*
 *  This file is part of the computer assignment for the
 *  Natural Language Processing course at Williams.
 *
 *  Author: Johan Boye
 */


package nlp;

import java.util.*;
import java.io.*;


/**
 *   This class represents a text as a bag-of-words + a category.
 */
public class Datapoint {

	public final String text;

	/** The text represented as a map from the word to the number of occurrences. */
	private HashMap<String,Integer> word = new HashMap<String,Integer>();

	private HashMap<String,HashMap<String,Integer>> bigramCount = new HashMap<String,HashMap<String,Integer>>();

	/** The total number of words in the text. */
	public int noOfWords;

	/** The category. */
	public String cat;

	public Datapoint( String text, String cat ) {
		this.text = text;
		this.cat = cat;
		try {
			StringReader reader = new StringReader( text + "\n" );
			Tokenizer tok = new Tokenizer( reader );
			String lastWord = "";
			while ( tok.hasMoreTokens() ) {
				noOfWords++;
				String w = tok.nextToken();
				Integer count = word.get( w );
				if ( count == null ) {
					count = 1;
				}
				else {
					count++;
				}
				word.put( w, count );

				HashMap<String,Integer> counts = bigramCount.get(lastWord);
				if(counts == null) {
					counts = new HashMap<String,Integer>();
				}
				count = counts.get(w);
				if(count == null) {
					count = 1;
				} else {
					count++;
				}
				counts.put(w, count);
				bigramCount.put(lastWord, counts);
				lastWord = w;
			}
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
	}


	public Iterator<String> iterator() {
		return word.keySet().iterator();
	}

	public Integer count( String w ) {
		return word.get( w );
	}

	public HashMap<String,Integer> getBigrams(String word) {
		return bigramCount.get(word);
	}

	public Integer countBigram(String w1, String w2) {
		HashMap<String,Integer> counts = bigramCount.get(w1);
		if(counts == null) {
			return null;
		}
		//System.out.println(w1 + " " + w2 + " " + bigramCount);
		return counts.get(w2);
	}

	public String toString() {
		return word.toString();
	}

}
