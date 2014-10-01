package edu.buffalo.cse.ir.wikiindexer.tokenizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class represents a stream of tokens as the name suggests.
 * It wraps the token stream and provides utility methods to manipulate it
 */
public class TokenStream implements Iterator<String>
{
	ArrayList<String> tokenList = null;
	Map<String, Integer> tokenMap = null;
	ListIterator<String> iterator = null;
	
	/**
	 * Default constructor
	 * @param bldr: The stringbuilder to seed the stream
	 * 
	 */
	public TokenStream(StringBuilder bldr) 
	{
		if(bldr != null && bldr.toString().trim() != "") 
		{
			String string = bldr.toString();
			init();
			 			
			tokenList.add(string.trim());
			updateMap(string.trim());	
			iterator = tokenList.listIterator();
		}						 	
	}
	
	/**
	 * Overloaded constructor
	 * @param bldr: The stringbuilder to seed the stream
	 */
	public TokenStream(String string) 
	{
		if(string != null && string.trim() != "") 
		{
			init();		 
			tokenList.add(string.trim());
			updateMap(string.trim());	
			iterator = tokenList.listIterator();
		}		
	}
	
	private void init()
	{
		tokenList = new ArrayList<String>();
		tokenMap = new HashMap<String, Integer>();		
	}
	
	private void updateMap(String key)
	{
		int frequency = 1;
        if(key!=null)
        {
        	if(tokenMap.containsKey(key))
	        {
        		frequency = tokenMap.get(key) + 1;
	        }          	  
        	tokenMap.put(key, frequency);  
        }         
	}
	
	/**
	 * Method to append tokens to the stream
	 * @param tokens: The tokens to be appended
	 */
	public void append(String... tokens) 
	{		
		if(tokens != null)
		{
			if(tokenList==null) 
				init();
			for(String str: tokens)
			{
				if(str!=null && str.trim()!="")
				{
					tokenList.add(str);
					updateMap(str);
				}
			}						
			reset();
		}		
	}
	
	/**
	 * Method to retrieve a map of token to count mapping
	 * This map should contain the unique set of tokens as keys
	 * The values should be the number of occurrences of the token in the given stream
	 * @return The map as described above, no restrictions on ordering applicable
	 */
	public Map<String, Integer> getTokenMap() 
	{
		if(tokenMap == null)
			return null;
		else
			return new TreeMap<String, Integer>(tokenMap);
	}
	
	/**
	 * Method to get the underlying token stream as a collection of tokens
	 * @return A collection containing the ordered tokens as wrapped by this stream
	 * Each token must be a separate element within the collection.
	 * Operations on the returned collection should NOT affect the token stream
	 */
	public Collection<String> getAllTokens() 
	{
		return tokenList;
	}
	
	/**
	 * Method to query for the given token within the stream
	 * @param token: The token to be queried
	 * @return: THe number of times it occurs within the stream, 0 if not found
	 */
	public int query(String token) 
	{
		if(token == null || token.trim() == "" || tokenMap == null) 
			return 0; 
		if(tokenMap.containsKey(token))
		{
			return tokenMap.get(token);
		}
		return 0;
	}
	
	/**
	 * Iterator method: Method to check if the stream has any more tokens
	 * @return true if a token exists to iterate over, false otherwise
	 */
	public boolean hasNext() 
	{
		if(iterator != null && iterator.hasNext())
			return true;
		else
			return false;
	}
	
	/**
	 * Iterator method: Method to check if the stream has any more tokens
	 * @return true if a token exists to iterate over, false otherwise
	 */
	public boolean hasPrevious() 
	{		
		if(iterator != null && iterator.hasPrevious())
			return true;
		else
			return false;
	}
	
	/**
	 * Iterator method: Method to get the next token from the stream
	 * Callers must call the set method to modify the token, changing the value
	 * of the token returned by this method must not alter the stream
	 * @return The next token from the stream, null if at the end
	 */
	public String next() 
	{
		if(iterator!=null && hasNext())
		{								
			return iterator.next();
		}
		return null;
	}
	
	/**
	 * Iterator method: Method to get the previous token from the stream
	 * Callers must call the set method to modify the token, changing the value
	 * of the token returned by this method must not alter the stream
	 * @return The next token from the stream, null if at the end
	 */
	public String previous() 
	{
		if(iterator!=null && iterator.hasPrevious())
		{
			return iterator.previous();
		}
		return null;
	}
	
	private void removeFromMap(String key)
	{
		if(tokenMap.containsKey(key))
		{
			int frequency = tokenMap.get(key);
			if(frequency-1>0)
				tokenMap.put(key, frequency-1);
			else
				tokenMap.remove(key);
		}
	}
	/**
	 * Iterator method: Method to remove the current token from the stream
	 */
	public void remove() 
	{
		if(tokenList != null && hasNext())
		{
			int index = iterator.nextIndex();
			String token = iterator.next();
			iterator.remove();	
			removeFromMap(token);
			
			reset();
			if(hasNext() && index > 0)
			{
				while(iterator.nextIndex() != index)
				{
					next();
				}
			}
		}			
	}
	
	/**
	 * Method to merge the current token with the previous token, assumes whitespace
	 * separator between tokens when merged. The token iterator should now point
	 * to the newly merged token (i.e. the previous one)
	 * @return true if the merge succeeded, false otherwise
	 */
	public boolean mergeWithPrevious() 
	{
		if(tokenList != null && hasPrevious() && hasNext())
		{
			String prevString = previous();
			next();
			String nextString = next();
			
			if(prevString != null && nextString != null)
			{								
				previous();
				remove();
				int index = iterator.previousIndex();					
				tokenList.set(index, prevString + " " + nextString);
				
				removeFromMap(prevString);
				removeFromMap(nextString);
				updateMap(prevString + " " + nextString);
				
				reset();
				while(iterator.nextIndex() != index)
				{
					next();
				}
				return true;
			}	
		}
		return false;
	}
	
	/**
	 * Method to merge the current token with the next token, assumes whitespace
	 * separator between tokens when merged. The token iterator should now point
	 * to the newly merged token (i.e. the current one)
	 * @return true if the merge succeeded, false otherwise
	 */
	public boolean mergeWithNext() 
	{
		if(tokenList != null && hasNext())
		{
			String string1 = next();			
			String string2 = hasNext() ? next() : null;
			
			if(string1 != null && string2 != null)
			{								
				previous();
				remove();
				int index = iterator.previousIndex();
				tokenList.set(index, string1 + " " + string2);
				
				removeFromMap(string1);
				removeFromMap(string2);
				updateMap(string1 + " " + string2);
				
				reset();
				while(iterator.nextIndex() != index)
				{
					next();
				}
				return true;
			}	
		}
		return false;
	}
	
	/**
	 * Method to replace the current token with the given tokens
	 * The stream should be manipulated accordingly based upon the number of tokens set
	 * It is expected that remove will be called to delete a token instead of passing
	 * null or an empty string here.
	 * The iterator should point to the last set token, i.e, last token in the passed array.
	 * @param newValue: The array of new values with every new token as a separate element within the array
	 */
	public void set(String... newValue) 
	{
		if(newValue != null && tokenList!=null && hasNext())
		{
			int index = iterator.nextIndex();
			String key = iterator.next();
			boolean first = true;			
			
			for(String str: newValue)
			{
				if(str!=null && str.trim()!="")
				{
					if(first) // execute this only first time
					{
						iterator.remove();
						removeFromMap(key);
						first = false;
					}
					tokenList.add(index, str.trim());
					updateMap(str);
					index++;
				}
			}						
			reset();
			while(iterator.nextIndex() < index-1)
			{
				next();
			}
		}		
	}
	
	/*
	 * Iterator method: Method to reset the iterator to the start of the stream
	 * next must be called to get a token
	 */
	public void reset() 
	{
		if(tokenList != null)
		{
			iterator = tokenList.listIterator();
		}
		else
		{
			iterator = null;
		}
	}
	
	/**
	 * Iterator method: Method to set the iterator to beyond the last token in the stream
	 * previous must be called to get a token
	 */
	public void seekEnd() 
	{
		if(iterator!=null)
		{
			while(iterator.hasNext())
			{
				iterator.next();
			}
		}
	}
		
	/**
	 * Method to merge this stream with another stream
	 * @param other: The stream to be merged
	 */
	public void merge(TokenStream other) 
	{
		if(other!=null && other.tokenList!=null)
		{
			if(tokenList==null) init();
			
			//reset();
			ListIterator<String> it = other.iterator;
			while(it.hasNext())
			{
				String str = it.next();
				if(str!=null && str.trim()!="")
				{					
					tokenList.add(str.trim());
					updateMap(str);
				}
			}
			reset();
		}
	}
}
