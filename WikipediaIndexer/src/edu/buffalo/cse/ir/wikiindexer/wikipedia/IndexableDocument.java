/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.wikipedia;

import edu.buffalo.cse.ir.wikiindexer.indexer.INDEXFIELD;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;

/**
 * A simple map based token view of the transformed document
 *
 */
public class IndexableDocument 
{
	/**
	 * Default constructor
	 */
	TokenStream termStream = null;
	TokenStream categoryStream = null;
	TokenStream authorStream = null;
	TokenStream linkStream = null;
	String docIdentifier = null;
	
	public IndexableDocument(String id) 
	{		
		docIdentifier = id;
	}
	
	/**
	 * MEthod to add a field and stream to the map
	 * If the field already exists in the map, the streams should be merged
	 * @param field: The field to be added
	 * @param stream: The stream to be added.
	 */
	public void addField(INDEXFIELD field, TokenStream stream) 
	{
		switch(field)
		{
			case TERM:
				termStream = stream;
				break;
				
			case CATEGORY:
				categoryStream =  stream;
				break;
				
			case AUTHOR:
				authorStream = stream;
				
			case LINK:
				linkStream = stream;
		}	
	}
	
	/**
	 * Method to return the stream for a given field
	 * @param key: The field for which the stream is requested
	 * @return The underlying stream if the key exists, null otherwise
	 */
	public TokenStream getStream(INDEXFIELD key) 
	{
		switch(key)
		{
			case TERM:
				return termStream;
				
			case CATEGORY:
				return categoryStream;
				
			case AUTHOR:
				return authorStream;
				
			case LINK:
				return linkStream;
		}
		return null;	
	}
	
	/**
	 * Method to return a unique identifier for the given document.
	 * It is left to the student to identify what this must be
	 * But also look at how it is referenced in the indexing process
	 * @return A unique identifier for the given document
	 */
	public String getDocumentIdentifier() 
	{
		return docIdentifier;
	}	
}
