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
 *  This class represents a set of datapoints (= texts + categories). 
 */
public class Dataset {

    /** The number of categories (=classes). */
    int noOfCategories;

    /** The mapping from category identifiers to category names. */
    String[] catName;

    /** The mapping from category names to category identifiers. */
    HashMap<String,Integer> catIndex = new HashMap<String,Integer>();

    /** The number of data points (=texts). */
    public int totNoOfDatapoints;

    /** The number of data points per category. */
    public int[] noOfDatapoints;

    /** The number of words per category. */
    public int[] noOfWords;

    /** The datapoints. */
    public ArrayList<Datapoint> point = new ArrayList<Datapoint>();

    public Dataset( String filename ) {
	try {
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line = null; 
	    Scanner scan = new Scanner( in );
	    // Read the categories
	    noOfCategories = scan.nextInt();
	    catName = new String[noOfCategories];
	    noOfDatapoints = new int[noOfCategories];
	    noOfWords = new int[noOfCategories];
	    for ( int i=0; i<noOfCategories; i++ ) {
		String name = scan.next(); 
		catName[i] = name;
		catIndex.put( name, i );
	    }
	    // Read the datapoints
	    totNoOfDatapoints = scan.nextInt();
	    for ( int i=0; i<totNoOfDatapoints; i++ ) {
		String category = scan.next();
		Integer cIndex = catIndex.get( category );
		String text = scan.nextLine();
		if ( cIndex == null ) {
		    // This category does not exist! 
		    // (probably a spelling error in the training file)
		    System.err.println( "Warning: Unknown category " + category + " for datapoint number " + i );
		    continue;
		}
		if ( text!=null && category!=null ) {
		    Datapoint dp = new Datapoint( text, category );
		    point.add( dp );
		    noOfWords[cIndex] += dp.noOfWords;
		    noOfDatapoints[cIndex]++;
		}
	    }
	}
	catch ( IOException e  ) {
	    e.printStackTrace();
	}
    }

    public Iterator<Datapoint> iterator() {
	return point.iterator();
    }

    public String toString() {
	StringBuffer buf = new StringBuffer();
	Iterator<Datapoint> iter = iterator();
	while ( iter.hasNext() ) {
	    buf.append( iter.next().toString() );
	    buf.append( "\n" );
	}
	return buf.toString();
    }
}