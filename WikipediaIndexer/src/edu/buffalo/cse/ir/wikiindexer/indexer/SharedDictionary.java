/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;

import java.util.Properties;

/**
 * This class represents a subclass of a Dictionary class that is
 * shared by multiple threads. All methods in this class are
 * synchronized for the same reason.
 */
public class SharedDictionary extends Dictionary 
{	
	static volatile int linkID = 0;
	boolean fwdIndex = false;
	
	/**
	 * Public default constructor. takes the arguments to specify what dictionary you are creating
	 * @param props: The properties file
	 * @param field: The field being indexed by this dictionary
	 */
	public SharedDictionary(Properties props, INDEXFIELD field) 
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
		if(fwdIndex == true)
			value = value.toLowerCase();
		if (linkDictionary.containsKey(value))
		{
			return linkDictionary.get(value);
		}
		else
		{	linkID++;
			linkDictionary.put(value, linkID);
			return linkID;
		}
	}		
}
