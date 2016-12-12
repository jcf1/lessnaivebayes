/*
 *  This file is part of the computer assignment for the
 *  Natural Language Processing course at Williams.
 *
 *  Author: Johan Boye
 *  Edited by John Freeman and Michael Zuo
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

	/**
	 *  Model parameters: P(P(word1|word2)|cat)
	 *  The index in the arraylist corresponds to the identifier
	 *  of the category, key for first hashmap corresponds to
	 *	last word seen, and key for second hashmap corresponds to
	 *	current word being processed.
	 */
	ArrayList<HashMap<String,HashMap<String,Double>>> bigramProbs = new ArrayList<HashMap<String,HashMap<String,Double>>>();

	/**  Prior probabilities P(cat) for all the categories. */
	double[] priorProb;

	/**
	 *   The probability of a word we haven't seen before
	 *	 ( should be 1/(size of vocabulary) ).
	 */
	double unknownWordProb;

	/**
	 *	If true, use bigram probabilities in classify Datapoint.  Else use unigram probabilities.
	*/
	boolean useBigram;

	/**
	 *  Computes the posterior probability P(cat|d) = P(cat|w1 ... wn) =
	 *  = P(cat) * P(w1|cat) * ... *vP(wn|cat), for all categories cat.
	 *
	 *  @return The name of the winning category (i.e. argmax P(cat|d) ).
	 */
	String classifyDatapoint( Datapoint d ) throws IOException {
		double postProb[] = new double[priorProb.length];

		for (int i = priorProb.length-1; i >= 0; i--)
			postProb[i] = Math.log(priorProb[i]);

		String lastWord = "^head";
		Tokenizer tokens = d.readText();
		while (tokens.hasMoreTokens()) {
			String w = tokens.nextToken();
			for (int i = priorProb.length-1; i >= 0; i--) {
				Double prob1 = likelihood.get(i).get(w);
				if (prob1 == null)
					prob1 = 0.0;
				prob1 += unknownWordProb;

				if (!useBigram)
					postProb[i] += Math.log(prob1);
				else {
					HashMap<String,Double> counts = bigramProbs.get(i).get(lastWord);
					if (counts == null)
						counts = likelihood.get(i);
					Double prob2 = counts.get(w);
					if (prob2 == null)
						prob2 = 0.0;
					prob2 += unknownWordProb;

					postProb[i] += Math.log(prob2*0.1 + prob1*0.9);
					lastWord = w;
				}
			}
		}

		int guess = 0;
		for (int i = 1; i < priorProb.length; i++) {
			if (postProb[guess] < postProb[i]) {
				guess = i;
			}
		}
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
	void buildModel( Dataset set ) throws IOException {
		// First copy some essential info from the training_set.
		catName = set.catName;
		catIndex = set.catIndex;
		priorProb = new double[set.noOfCategories];
		HashSet<String> allVocav = new HashSet<String>();

		for (Datapoint p : set.point) {
			Iterator<String> it = p.iterator();
			while (it.hasNext())
				allVocav.add(it.next());
		}
		unknownWordProb = 1.0/allVocav.size();

		System.out.println(set.noOfCategories);
		for (int i = 0; i < set.noOfCategories; i++) {
			System.out.println(catName[i]);
			int allWords = 0;
			int catWords = 0;
			HashMap<String,Integer> freq = new HashMap<String,Integer>();
			HashMap<String,HashMap<String,Integer>> freq2 = new HashMap<String, HashMap<String,Integer>>();
			HashSet<String> catVocav = new HashSet<String>();

			priorProb[i] = 1.0*set.noOfDatapoints[i]/set.totNoOfDatapoints;

			// impossible token used as initial value of lastW ~ head of string
			freq.put("^head", set.noOfDatapoints[i]);
			for (Datapoint p : set.point) {
				if (catIndex.get(p.cat) != i) { continue; }

				String lastW = "^head";
				Tokenizer tokens = p.readText();
				while (tokens.hasMoreTokens()) {
					String w = tokens.nextToken();
					catVocav.add(w);

					Integer oldfreq = freq.get(w);
					if (oldfreq == null)
						oldfreq = 0;
					freq.put(w, oldfreq+1);

					HashMap<String,Integer> counts = freq2.get(lastW);
					if (counts == null) {
						counts = new HashMap<String,Integer>();
						counts.put(w, 1);
						freq2.put(lastW, counts);
					}
					else {
						Integer oldfreq2 = counts.get(w);
						if (oldfreq2 == null)
							oldfreq2 = 0;
						counts.put(w, oldfreq2+1);
					}
					lastW = w;
				}
			}

			HashMap<String,Double> prob = new HashMap<String,Double>();
			for (String w : allVocav) {
				Integer seen = freq.get(w);
				if (seen == null)
					seen = 0;
				prob.put(w, (seen+1.0)/set.noOfWords[i]);
			}
			likelihood.add(prob);

			HashMap<String,HashMap<String,Double>> prob2 = new HashMap<String,HashMap<String,Double>>();
			for (String w1 : freq2.keySet()) {
				HashMap<String,Integer> counts = freq2.get(w1);
				HashMap<String,Double> cond = new HashMap<String,Double>();
				int total = counts.size(); // extra for smoothing
				for (int partial : counts.values())
					total += partial;

				for (String w2 : counts.keySet())
					cond.put(w2, (counts.get(w2)+1.0) / total);
				prob2.put(w1, cond);
			}
			bigramProbs.add(prob2);
		}
	}

	/**
	 *   Goes through a testset, classifying each datapoint according
	 *   to the model.
	 */
	void classifyTestset( Dataset testset ) throws IOException {
		System.out.println(testset.totNoOfDatapoints);
		Iterator<Datapoint> iter = testset.iterator();
		while ( iter.hasNext() ) {
			Datapoint dp = iter.next();
			String cat = classifyDatapoint( dp );
			System.out.println( cat + dp.text );
		}
	}

	/**
	 *   Constructor. Read the training file and, possibly, the test
	 *   file. If test file is null, read input from the keyboard.
	 */
	public NaiveBayesTextClassifier( String training_file, String test_file, boolean useBigram ) {
		this.useBigram = useBigram;
		try {
			buildModel( new Dataset( training_file ));
			classifyTestset( new Dataset( test_file ));
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/** Prints usage information. */
	static void printHelpMessage() {
		System.err.println( "The following parameters are available: " );
		System.err.println( "  -d <filename> : training file (mandatory)");
		System.err.println( "  -t <filename> : test file (mandatory)" );
		System.err.println( "  -1 : Force use of unigrams (default)" );
		System.err.println( "  -2 : Use bigram probabilities" );
		System.err.println( "  -bi : Compatibility name for -2" );
	}

	public static void main( String[] args ) {
		// Parse command line arguments
		String training_file = null;
		String test_file = null;
		boolean useBigram = false;
		int i = 0;
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
			else if ( args[i].equals( "-1" )) {
				i++;
				useBigram = false;
			}
			else if ( args[i].equals( "-2" ) ||  args[i].equals( "-bi" )) {
				i++;
				useBigram = true;
			}
			else {
				printHelpMessage();
				return;
			}
		}
		if ( training_file != null && test_file != null ) {
			new NaiveBayesTextClassifier( training_file, test_file, useBigram );
		}
		else {
			printHelpMessage();
		}
	}
}
