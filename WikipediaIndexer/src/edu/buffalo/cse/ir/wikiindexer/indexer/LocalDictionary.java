/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Properties;

/**
 * This class represents a subclass of a Dictionary class that is
 * local to a single thread. All methods in this class are
 * assumed thread safe for the same reason.
 */
public class LocalDictionary extends Dictionary 
{	
	HashMap<String,Integer> localDictionary = new HashMap<String, Integer>();
	static volatile int linkID = 0;
	Properties properties;
	FileOutputStream file;
	int totalTerms = 0;
	BufferedWriter bw;
	boolean fwdIndex = false;
	
	/**
	 * Public default constructor
	 * @param props: The properties file
	 * @param field: The field being indexed by this dictionary
	 */	
	public LocalDictionary(Properties props, INDEXFIELD field) 
	{
		super(props, field);
		if(field == INDEXFIELD.LINK)
			fwdIndex = true;
	}
	
	/**
	 * Method to lookup and possibly add a mapping for the given value
	 * in the dictionary. The class should first try and find the given
	 * value within its dictionary. If found, it should return its
	 * id (Or hash value). If not found, it should create an entry and
	 * return the newly created id.
	 * @param value: The value to be looked up
	 * @return The id as explained above.
	 */
	public synchronized int lookup(String value) 
	{		
		// Look for a value .. if found .. return ID else add with a new ID		
		if (linkDictionary.containsKey(value))
			return linkDictionary.get(value);
		else
		{	linkID++;
			linkDictionary.put(value, linkID);
			++totalTerms;
			return linkID;
		}
	}	
	
	public synchronized int getTotalTerms() 
	{
		return totalTerms;
	}
}
