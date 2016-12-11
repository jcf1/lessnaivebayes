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

	ArrayList<HashMap<String,HashMap<String,Double>>> bigramProbs = new ArrayList<HashMap<String,HashMap<String,Double>>>();

	/**  Prior probabilities P(cat) for all the categories. */
	double[] priorProb;

	/**
	 *   The probability of a word we haven't seen before
	 *	 ( should be -Math.log(size of vocabulary) ).
	 */
	double unknownWordProb;

	boolean useBigram;
	double threshold;
	int maxCategories;

	/**
	 *  Computes the posterior probability P(cat|d) = P(cat|w1 ... wn) =
	 *  = P(cat) * P(w1|cat) * ... *vP(wn|cat), for all categories cat.
	 *
	 *  @return The name of the winning category (i.e. argmax P(cat|d) ).
	 */
	String classifyDatapoint( Datapoint d ) {
		double postProb[] = new double[priorProb.length];

		for (int i = priorProb.length-1; i >= 0; i--) {
			postProb[i] = priorProb[i];
		}

		if(!useBigram) {
			Iterator<String> it = d.iterator();
			while (it.hasNext()) {
				String w = it.next();
				for (int i = priorProb.length-1; i >= 0; i--) {
					Double prob = likelihood.get(i).get(w);
					if (prob == null) {
						prob = unknownWordProb;
					}
					postProb[i] += d.count(w)*prob;
				}
			}
		} else {
			try {
				Tokenizer tokens = d.readText();
				String lastWord = "";
				while(tokens.hasMoreTokens()) {
					String w = tokens.nextToken();
					for (int i = priorProb.length-1; i >= 0; i--) {
						HashMap<String,Double> counts = bigramProbs.get(i).get(lastWord);
						Double prob = 0.0;
						if(counts != null) {
							prob = counts.get(w);
							if (prob == null) {
								prob = unknownWordProb;
							}
						} else {
							prob = unknownWordProb;
						}
						postProb[i] += d.countBigram(lastWord, w) * prob;
					}
					lastWord = w;
				}
			}
			catch ( IOException e ) {
				e.printStackTrace();
			}
		}

		String result = "";
		if(maxCategories == 1) {
			int guess = 0;
			for (int i = 1; i < priorProb.length; i++) {
				if (postProb[guess] < postProb[i]) {
					guess = i;
				}
			}
			result = catName[guess];
		} else {
			int count = 0;
			int[] guesses = new int[maxCategories];
			double[] probs = new double[maxCategories];

			for (int i = 0; i < priorProb.length; i++) {
				double guess = postProb[i];
				if(guess > threshold && (count < maxCategories || guess > probs[maxCategories - 1])) {
					int index = count;
					for(int j = 0; j < count; j++) {
						if(guess > probs[j]) {
							index = j;
							break;
						}
					}
					int ind = i;
					double val = guess;
					for(int j = index; j < count; j++) {
						int tempInd = guesses[j];
						double tempVal = probs[j];
						guesses[j] = ind;
						probs[j] = val;
						ind = tempInd;
						val = tempVal;
					}
				}
			}

			for(int i = 0; i < count; i++) {
				result += i + ". " + guesses[i] + " " + probs[i] + "\n";
			}
		}

		return result;
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
		HashSet<String> vocav = new HashSet<String>();

		System.out.println(set.noOfCategories);
		for (int i = 0; i < set.noOfCategories; i++) {
			System.out.println(catName[i]);
			HashMap<String,Integer> freq = new HashMap<String,Integer>();
			HashMap<String,HashMap<String,Integer>> freq2 = new HashMap<String, HashMap<String,Integer>>();

			freq.put("", set.noOfDatapoints[i]);
			priorProb[i] = Math.log(set.noOfDatapoints[i]) - Math.log(set.totNoOfDatapoints);

			for (Datapoint p : set.point) {
				if (catIndex.get(p.cat) != i) { continue; }

				Iterator<String> it = p.iterator();
				String lastW = "";
				while (it.hasNext()) {
					String w = it.next();
					vocav.add(w);

					Integer got = freq.get(w);
					if (got == null) { got = 0; }
					freq.put(w, got + p.count(w));

					HashMap<String,Integer> test = freq2.get(lastW);
					if (test == null) { test = new HashMap<String,Integer>(); }
					HashMap<String,Integer> bigramCount = p.getBigrams(lastW);
					if(bigramCount != null) {
						for(String word : bigramCount.keySet()) {
							test.put(word, p.countBigram(lastW, word));
							freq2.put(lastW, test);
						}
					}
					lastW = w;
				}
			}

			HashMap<String,Double> prob = new HashMap<String,Double>();
			for (String w : freq.keySet()) {
				prob.put(w, Math.log(freq.get(w))-Math.log(set.noOfWords[i]));
			}
			likelihood.add(prob);

			HashMap<String,HashMap<String,Double>> prob2 = new HashMap<String,HashMap<String,Double>>();
			for (String w1 : freq2.keySet()) {
				HashMap<String,Integer> counts = freq2.get(w1);
				HashMap<String,Double> temp = new HashMap<String,Double>();
				for(String w2 : counts.keySet()) {
					temp.put(w2, Math.log(counts.get(w2)) - Math.log(freq.get(w1)));
				}
				prob2.put(w1, temp);
			}
			bigramProbs.add(prob2);
		}

		unknownWordProb = - Math.log(vocav.size());
	}

	/**
	 *   Goes through a testset, classifying each datapoint according
	 *   to the model.
	 */
	void classifyTestset( Dataset testset ) {
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
	public NaiveBayesTextClassifier( String training_file, String test_file, boolean useBigram, double threshold, int maxCategories ) {
		this.useBigram = useBigram;
		this.threshold = threshold;
		this.maxCategories = maxCategories;
		buildModel( new Dataset( training_file ));
		classifyTestset( new Dataset( test_file ));
	}

	/** Prints usage information. */
	static void printHelpMessage() {
		System.err.println( "The following parameters are available: " );
		System.err.println( "  -d <filename> : training file (mandatory)");
		System.err.println( "  -t <filename> : test file (mandatory)" );
		System.err.println( "  -1 : Force use of unigrams (default)" );
		System.err.println( "  -2 : Use bigram probabilities" );
		System.err.println( "  -bi : Compatibility name for -2" );
		System.err.println( "  -th <double> : Only return a category above this threshold score (optional, defualt negative infinity)");
		System.err.println( "  -max <int> : Maximum amount of categories that is returned (optional, default 1)" );
	}

	public static void main( String[] args ) {
		// Parse command line arguments
		String training_file = null;
		String test_file = null;
		boolean useBigram = false;
		double threshold = Double.NEGATIVE_INFINITY;
		int maxCategories = 1;
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
			else if ( args[i].equals( "-th" )) {
				i++;
				if ( i<args.length ) {
					try {
						threshold = Double.parseDouble(args[i++]);
					} catch(NumberFormatException e) {
						printHelpMessage();
						return;
					}
				}
				else {
					printHelpMessage();
					return;
				}
			}
			else if ( args[i].equals( "-max" )) {
				i++;
				if ( i<args.length ) {
					try {
						maxCategories = Integer.parseInt(args[i++]);
					} catch(NumberFormatException e) {
						printHelpMessage();
						return;
					}
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
			new NaiveBayesTextClassifier( training_file, test_file, useBigram, threshold, maxCategories );
		}
		else {
			printHelpMessage();
		}
	}
}
