/*  
 *  This file is part of the computer assignment for the
 *  Natural Language Processing course at Williams.
 * 
 *  Author: Johan Boye
 */  

package nlp;

import java.io.*;
import java.util.*;

/**
 *   This class performs text classification using the Naive Bayes method.
 */
public class NaiveBayesTextClassifier {

	/** The mapping from category identifiers to category names. */
	String[] catName;

	/** The mapping from category names to category identifiers. */
	HashMap<String,Integer> catIndex;

	/**
	 *  Model parameters: P(word|cat) 
	 *  The index in the arraylist corresponds to the identifier
	 *  of the category (i.e. the first element contains the 
	 *  probabilities for category 1, the second for category 2,
	 *  etc.
	 */
	ArrayList<HashMap<String,Double>> likelihood = new ArrayList<HashMap<String,Double>>();

	/**  Prior probabilities P(cat) for all the categories. */
	double[] priorProb;

	/** 
	 *   The probability of a word we haven't seen before 
	 *	 ( should be -Math.log(size of vocabulary) ).
	 */
	double unknownWordProb;

	/**
	 *  Computes the posterior probability P(cat|d) = P(cat|w1 ... wn) =
	 *  = P(cat) * P(w1|cat) * ... *vP(wn|cat), for all categories cat.
	 *
	 *  @return The name of the winning category (i.e. argmax P(cat|d) ).
	 */
	String classifyDatapoint( Datapoint d ) {
		double postProb[] = new double[priorProb.length];

		for (int i = priorProb.length-1; i >= 0; i--)
			postProb[i] = priorProb[i];

		Iterator<String> it = d.iterator();
		while (it.hasNext()) {
			String w = it.next();
			for (int i = priorProb.length-1; i >= 0; i--) {
				Double prob = likelihood.get(i).get(w);
				if (prob == null)
					prob = unknownWordProb;
				/* XXX 2
				else prob = Math.log(Math.exp(prob) + Math.exp(unknownWordProb));
				*/
				//System.out.println(d.count(w) + " " + prob * d.count(w) + " " + i + " " + w + " " + postProb[i]);
				postProb[i] += d.count(w)*prob;
			}
		}

		int guess = 0;
		for (int i = 1; i < priorProb.length; i++) {
			if (postProb[guess] < postProb[i])
				guess = i;
		}

		//System.out.println(Arrays.toString(postProb));
		return catName[guess];
	}

	/**
	 *   Computes the prior probabilities P(cat) and likelihoods
	 *   P(word|cat), for all words and categories (also for
	 *   unseen words). To avoid underflow, log-probabilities are 
	 *   used. 
	 *
	 *   Laplace smoothing is used in order to avoid that certain
	 *   probabilities become zero.
	 */
	void buildModel( Dataset set ) {
		// First copy some essential info from the training_set.
		catName = set.catName;
		catIndex = set.catIndex;
		priorProb = new double[set.noOfCategories];
		HashMap<String,Boolean> vocav = new HashMap<String,Boolean>();

		for (int i = 0; i < set.noOfCategories; i++) {
			HashMap<String,Integer> freq = new HashMap<String,Integer>();

			priorProb[i] = Math.log(set.noOfDatapoints[i]) - Math.log(set.totNoOfDatapoints);
			for (Datapoint p : set.point) {
				if (catIndex.get(p.cat) != i) continue;

				Iterator<String> it = p.iterator();
				while (it.hasNext()) {
					String w = it.next();
					vocav.put(w, true);
					Integer got = freq.get(w);
					if (got == null) got = 0;
					freq.put(w, got + p.count(w));
				}
			}

			HashMap<String,Double> prob = new HashMap<String,Double>();
			for (String w : freq.keySet())
				prob.put(w, Math.log(freq.get(w))-Math.log(set.noOfWords[i]));
			likelihood.add(prob);
		}

		/* XXX 1
		int totalWords = 0;
		for (int i = 0; i < set.noOfCategories; i++)
			totalWords += set.noOfWords[i];
		unknownWordProb = -Math.log(totalWords);
		*/
		unknownWordProb = -Math.log(vocav.size());
	}

	/**
	 *   Goes through a testset, classifying each datapoint according 
	 *   to the model.
	 */
	void classifyTestset( Dataset testset ) {
		Iterator<Datapoint> iter = testset.iterator();
		while ( iter.hasNext() ) {
			Datapoint dp = iter.next();
			String cat = classifyDatapoint( dp );
			System.out.println( cat );
		}
	}

	/**
	 *   Constructor. Read the training file and, possibly, the test 
	 *   file. If test file is null, read input from the keyboard.
	 */
	public NaiveBayesTextClassifier( String training_file, String test_file ) {
		buildModel( new Dataset( training_file ));
		classifyTestset( new Dataset( test_file ));
	}

	/** Prints usage information. */
	static void printHelpMessage() {
		System.err.println( "The following parameters are available: " );
		System.err.println( "  -d <filename> : training file (mandatory)");
		System.err.println( "  -t <filename> : test file (mandatory)" );
	}

	public static void main( String[] args ) {
		// Parse command line arguments 
		String training_file = null;
		String test_file = null;
		int i=0; 
		while ( i<args.length ) {
			if ( args[i].equals( "-d" )) {
				i++;
				if ( i<args.length ) {
					training_file = args[i++];
				}
				else {
					printHelpMessage();
					return;
				}
			}
			else if ( args[i].equals( "-t" )) {
				i++;
				if ( i<args.length ) {
					test_file = args[i++];
				}
				else {
					printHelpMessage();
					return;
				}
			}
			else {
				printHelpMessage();
				return;
			}
		}
		if ( training_file != null && test_file != null ) {
			new NaiveBayesTextClassifier( training_file, test_file );
		}
		else {
			printHelpMessage();
		}
	}
}
