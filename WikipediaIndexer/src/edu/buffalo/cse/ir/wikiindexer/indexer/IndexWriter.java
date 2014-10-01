/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.buffalo.cse.ir.wikiindexer.IndexerConstants;

/**
 * This class is used to write an index to the disk
 * 
 */
public class IndexWriter implements Writeable 
{
	SortedMap<String, PostingsList> invertedIndex = null;
	SortedMap<Integer, PostingsList> linkIndex = null; // forward index
	
	int currPartition = 1;
	String indexType = "L";	// for filename
	Properties properties;
	FileOutputStream file;
	BufferedWriter bw;
	
	/**
	 * Constructor that assumes the underlying index is inverted
	 * Every index (inverted or forward), has a key field and the value field
	 * The key field is the field on which the postings are aggregated
	 * The value field is the field whose postings we are accumulating
	 * For term index for example:
	 * 	Key: Term (or term id) - referenced by TERM INDEXFIELD
	 * 	Value: Document (or document id) - referenced by LINK INDEXFIELD
	 * @param props: The Properties file
	 * @param keyField: The index field that is the key for this index
	 * @param valueField: The index field that is the value for this index
	 */
	public IndexWriter(Properties props, INDEXFIELD keyField, INDEXFIELD valueField) 
	{
		this(props, keyField, valueField, false);
	}
	
	/**
	 * Overloaded constructor that allows specifying the index type as
	 * inverted or forward
	 * Every index (inverted or forward), has a key field and the value field
	 * The key field is the field on which the postings are aggregated
	 * The value field is the field whose postings we are accumulating
	 * For term index for example:
	 * 	Key: Term (or term id) - referenced by TERM INDEXFIELD
	 * 	Value: Document (or document id) - referenced by LINK INDEXFIELD
	 * @param props: The Properties file
	 * @param keyField: The index field that is the key for this index
	 * @param valueField: The index field that is the value for this index
	 * @param isForward: true if the index is a forward index, false if inverted
	 */
	public IndexWriter(Properties props, INDEXFIELD keyField, INDEXFIELD valueField, boolean isForward) 
	{
		properties = props;
		if(keyField == INDEXFIELD.LINK && valueField == INDEXFIELD.LINK)
		{
			linkIndex = new TreeMap<Integer, PostingsList>();			
			indexType = "L";  // forward index
		}
		else
		{
			invertedIndex = new TreeMap<String, PostingsList>();	
			if(keyField == INDEXFIELD.CATEGORY) indexType = "C";
			else if(keyField == INDEXFIELD.TERM) indexType = "T";
			else indexType = "A";
		}		
	}
	
	/**
	 * Method to make the writer self aware of the current partition it is handling
	 * Applicable only for distributed indexes.
	 * @param pnum: The partition number
	 */
	public void setPartitionNumber(int pnum) 
	{
		currPartition = pnum;
	}
	
	/**
	 * Method to add a given key - value mapping to the index
	 * @param keyId: The id for the key field, pre-converted
	 * @param valueId: The id for the value field, pre-converted
	 * @param numOccurances: Number of times the value field is referenced by the key field. Ignore if a forward index
	 * @throws IndexerException: If any exception occurs while indexing
	 * Called by the LinkIndex
	 */
	public void addToIndex(int keyId, int valueId, int numOccurances) throws IndexerException 
	{
		boolean isPresent = false;
		PostingsList links;
		
		if(linkIndex == null)
		{
			linkIndex = new TreeMap<Integer, PostingsList>();
		}
		else
		{
			isPresent = linkIndex.containsKey(keyId);
		}
								
		if(isPresent)
		{
			links = linkIndex.get(keyId);				
		}
		else
		{
			links = new PostingsList();
		}
		links.add(valueId, numOccurances);
		linkIndex.put(keyId, links);
	}
	
	/**
	 * Method to add a given key - value mapping to the index
	 * @param keyId: The id for the key field, pre-converted
	 * @param value: The value for the value field
	 * @param numOccurances: Number of times the value field is referenced
	 *  by the key field. Required for forward index only
	 * @throws IndexerException: If any exception occurs while indexing
	 */
	public void addToIndex(int keyId, String value, int numOccurances) throws IndexerException 
	{
		boolean isPresent = false;
		PostingsList links;
		
		if(linkIndex == null)
		{
			linkIndex = new TreeMap<Integer, PostingsList>();
		}
		else
		{
			isPresent = linkIndex.containsKey(keyId);
		}
								
		if(isPresent)
		{
			links = linkIndex.get(keyId);				
		}
		else
		{
			links = new PostingsList();
		}
		links.add(keyId, numOccurances);
		linkIndex.put(keyId, links);
	}
	
	/**
	 * Method to add a given key - value mapping to the index
	 * @param key: The key for the key field
	 * @param valueId: The id for the value field, pre-converted
	 * @param numOccurances: Number of times the value field is referenced by the key field. Required for inverted index
	 * @throws IndexerException: If any exception occurs while indexing
	 * Called by Term, Category and Author inverted indices
	 */
	public void addToIndex(String key, int valueId, int numOccurances) throws IndexerException 
	{
		boolean isPresent = false;
		PostingsList p;
		
		key = key.trim();
		if(key.length() == 1 && key.matches("[\\.,!#-_?]")) return;		
		key = key.replace("\"","").replace("*","").replaceAll("[#';,!]+", "").replaceAll("\\s+"," ").trim();		
		key = key.replaceAll("(\\.|-)\\1+", "").trim();
		if(key.startsWith(".") || key.startsWith("-")) key = key.substring(1);
		key = key.replaceAll("(\\.\\s+|\\-\\s+)", " ").trim();
		if(key.length() == 0) return;	
		
		if(invertedIndex == null)
		{
			invertedIndex = new TreeMap<String, PostingsList>();
		}
		else
		{
			isPresent = invertedIndex.containsKey(key);
		}
								
		if(isPresent)
		{
			p = invertedIndex.get(key);				
		}
		else
		{
			p = new PostingsList();
		}
		if(key.trim().length()>0)
		{
			p.add(valueId, numOccurances);
			invertedIndex.put(key, p);
		}		
	}
	
	/**
	 * Method to add a given key - value mapping to the index
	 * @param key: The key for the key field
	 * @param value: The value for the value field
	 * @param numOccurances: Number of times the value field is referenced
	 *  by the key field. Ignore if a forward index
	 * @throws IndexerException: If any exception occurs while indexing 
	 */
	public void addToIndex(String key, String value, int numOccurances) throws IndexerException 
	{
		boolean isPresent = false;
		PostingsList p;
		
		key = key.trim();
		if(key.length() == 1 && key.matches("[\\.,!#-_?]")) return;		
		key = key.replace("\"","").replace("*","").replaceAll("[#';,!]+", "").replaceAll("\\s+"," ").trim();		
		key = key.replaceAll("(\\.|-)\\1+", "").trim();
		if(key.startsWith(".") || key.startsWith("-")) key = key.substring(1);
		key = key.replaceAll("(\\.\\s+|\\-\\s+)", " ").trim();
		if(key.length() == 0) return;	
		
		if(invertedIndex == null)
		{
			invertedIndex = new TreeMap<String, PostingsList>();
		}
		else
		{
			isPresent = invertedIndex.containsKey(key);
		}
								
		if(isPresent)
		{
			p = invertedIndex.get(key);				
		}
		else
		{
			p = new PostingsList();
		}
		if(key.trim().length()>0)
		{
			p.add(String.valueOf(key), numOccurances);
			invertedIndex.put(key, p);
		}	
	}

	public void writeToDisk() throws IndexerException 
	{
		String fileName;
		if(indexType == "L") fileName = "LinkIndex.txt";
		else if(indexType == "C") fileName = "CategoryIndex.txt";
		else if(indexType == "T") fileName = "TermIndex.txt";
		else fileName = "AuthorIndex.txt";
		
		try
	    {
			String path = properties.getProperty(IndexerConstants.TEMP_DIR);			
			file = new FileOutputStream(path+fileName);				
			bw = new BufferedWriter(new OutputStreamWriter(file, "UTF-8"));	
						
	        if(linkIndex!=null && indexType == "L")
	        {
	        	Iterator<Entry<Integer, PostingsList>> itr = linkIndex.entrySet().iterator();
	        	while(itr.hasNext())
	        	{	        		
	        		Entry<Integer, PostingsList> indexTuple = itr.next();
	        		String str = indexTuple.getKey() + "|";	  // Token  
	        		
	        		HashMap<Integer, Integer> list = indexTuple.getValue().list;  // Postings List
	        		Iterator<Entry<Integer, Integer>> i = list.entrySet().iterator();
	        		while(i.hasNext())
	        		{
	        			Entry<Integer, Integer> post = i.next();
	        			str += String.valueOf(post.getKey()) + ":" + String.valueOf(post.getValue());
	        			if(i.hasNext()) str += "~";
	        		}
	        		
	        		str += "|" + indexTuple.getValue().numOccurances;  // Total no of docs in postings
	        		bw.write(str + System.getProperty("line.separator"));
	        	}	        	
	        	System.out.println("\n"+fileName + " records: "  + linkIndex.size());
	        }
	        else if(invertedIndex!=null)
	        {	        	
	        	Iterator<Entry<String, PostingsList>> itr = invertedIndex.entrySet().iterator();
	        	while(itr.hasNext())
	        	{	        		
	        		Entry<String, PostingsList> indexTuple = itr.next();
	        		String str = indexTuple.getKey() + "|";  // Token
	        			        		
	        		HashMap<Integer, Integer> list = indexTuple.getValue().list;  // Postings List
	        		Iterator<Entry<Integer, Integer>> i = list.entrySet().iterator();
	        		while(i.hasNext())
	        		{
	        			Entry<Integer, Integer> post = i.next();
	        			str += String.valueOf(post.getKey()) + ":" + String.valueOf(post.getValue());
	        			if(i.hasNext()) str += "~";
	        		}
	        		
	        		str += "|" + indexTuple.getValue().numOccurances;  // Total no of docs in postings
	        		bw.write(str + System.getProperty("line.separator"));	        			        	
	        	}		        		        	
	        	System.out.println("\n"+fileName + " records: "  +invertedIndex.size());
	        }	        
	    }
		catch(IOException i)
	    {
	    }

	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.ir.wikiindexer.indexer.Writeable#cleanUp()
	 */
	public void cleanUp() 
	{
		try 
		{
			bw.close();
			file.close();			
		}
		catch (IOException i) 
		{
		}  
	}
	
	/* PostingsList class for an Inverted Index */
	class PostingsList
	{
		HashMap<Integer, Integer> list; // <DocID:frequency>
		int numOccurances;
		
		public PostingsList()
		{
			list = new HashMap<Integer, Integer>();
			numOccurances = 0;
		}
		
		public int getTotalOccurance()
		{
			return numOccurances;
		}
		
		public void add(int docID, int frequency)
		{
			list.put(docID, frequency);
			numOccurances += 1;
		}	
		
		public void add(String docID, int frequency)
		{
			list.put(Integer.parseInt(docID), frequency);
			numOccurances += 1;
		}
	}
}
