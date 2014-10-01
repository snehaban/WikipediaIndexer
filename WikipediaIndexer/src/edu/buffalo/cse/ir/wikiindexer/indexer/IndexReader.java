/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import edu.buffalo.cse.ir.wikiindexer.IndexerConstants;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.EnglishStemmer;

/**
 * This class is used to introspect a given index
 * The expectation is the class should be able to read the index
 * and all associated dictionaries.
 */
public class IndexReader 
{
	TreeMap<String, PostingsList> invertedIndex;
	String fileName;				
	FileReader fileReader;				
	BufferedReader br;
	int totalTerms = 0;
	Properties props;
	static TreeMap<String, Integer> sharedDictionary;
	static int totalTermsInSharedDict = 0;
	boolean fwdIndex = false;
	
	/**
	 * Constructor to create an instance 
	 * @param props: The properties file
	 * @param field: The index field whose index is to be read
	 */
	public IndexReader(Properties properties, INDEXFIELD field) 
	{
		props = properties;
		String filepath = props.getProperty(IndexerConstants.TEMP_DIR)+"/";
		switch(field)
		{
			case TERM:
				fileName = filepath + "TermIndex.txt";
				break;
			case CATEGORY:
				fileName = filepath + "CategoryIndex.txt";
				break;
			case AUTHOR:
				fileName = filepath + "AuthorIndex.txt";
				break;
			case LINK:
				fileName = filepath + "LinkIndex.txt";
				fwdIndex = true;
				break;
		}				
		
		try
		{
			fileReader = new FileReader(fileName);
			br = new BufferedReader(fileReader);
			invertedIndex = new TreeMap<String, PostingsList>();			
			String indexRow, term;
			String[] postings, docList;
			PostingsList p;			
			try 
			{
				while ((indexRow = br.readLine()) != null) 
				{
					String[] obj = indexRow.split("[|]"); // term, postings, occurances
					term = obj[0].toString();
					postings = obj[1].split("~");
					p = new PostingsList();
					for(int i=0; i<postings.length;i++)
					{
						docList = postings[i].split(":");
						if(docList.length == 2)
							p.add(Integer.parseInt(docList[0]), Integer.parseInt(docList[1]));
					}
					invertedIndex.put(term, p);
					totalTerms++;
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		} 
		catch (FileNotFoundException e1) 
		{			
			e1.printStackTrace();
		}
		try 
		{
			fileReader.close();
			br.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		FileReader fileReader1 = null;
		BufferedReader br1 = null;
		try // Read Index
		{
			fileReader1 = new FileReader(filepath+"SharedDict.txt");			
			br1 = new BufferedReader(fileReader1);
			sharedDictionary = new TreeMap<String, Integer>();			
			String indexRow;		
			try 
			{
				while ((indexRow = br1.readLine()) != null) 
				{
					String[] obj = indexRow.split("[|]"); // doc name, ID
					if(obj.length == 2)
					sharedDictionary.put(obj[0], Integer.parseInt(obj[1]));
					totalTermsInSharedDict++;
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		} 
		catch (FileNotFoundException e1) 
		{			
			e1.printStackTrace();
		}
		
		try 
		{
			fileReader1.close();
			br1.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to get the total number of terms in the key dictionary
	 * @return The total number of terms as above
	 */
	public int getTotalKeyTerms() 
	{
		if(fwdIndex == true)
			return totalTermsInSharedDict;
		return totalTerms;
	}
	
	/**
	 * Method to get the total number of terms in the value dictionary
	 * @return The total number of terms as above
	 */
	public int getTotalValueTerms() 
	{
		if(fwdIndex == true)
			return totalTermsInSharedDict;
		return totalTerms;
	}
	
	/**
	 * Method to retrieve the postings list for a given dictionary term
	 * @param key: The dictionary term to be queried
	 * @return The postings list with the value term as the key and the
	 * number of occurrences as value. An ordering is not expected on the map
	 */
	public Map<String, Integer> getPostings(String key) // postings list for one term
	{		
		EnglishStemmer stemmer = new EnglishStemmer();
		TokenStream ts = new TokenStream(key);
		try 
		{
			stemmer.apply(ts);
		} 
		catch (TokenizerException e) 
		{
			e.printStackTrace();
		}
		String stemmedKey = ts.next();
		PostingsList p = invertedIndex.get(stemmedKey); // get the stemmed key postingslist
		if(p==null) return null;
		
		Map<String, Integer> postings = new LinkedHashMap<String, Integer>();
		Iterator<Entry<Integer, Integer>> itr = p.list.entrySet().iterator();
		while(itr.hasNext())
		{
			Entry<Integer, Integer> post = itr.next();
			postings.put(String.valueOf(post.getKey()), post.getValue());
		}
		System.out.println("\nPostings list for '"+key+"':\n");
		postings = showResults(postings);
		return postings;
	}
	
	/**
	 * Wild card queries - returns all postings for a wild card query string
	 */ 
	public Map<String, Integer> getAllPostings(String pattern) 
	{				
		Map<String, Integer> postingsMap = new LinkedHashMap<String, Integer>(); // Final PostingsList 
		
		Iterator<Entry<String, PostingsList>> itr = invertedIndex.entrySet().iterator(); // to traverse the entire index
		while(itr.hasNext())
		{
			Entry<String, PostingsList> currTerm = itr.next();
			EnglishStemmer stemmer = new EnglishStemmer();
			TokenStream ts = new TokenStream(currTerm.getKey());
			try 
			{
				stemmer.apply(ts);
			} 
			catch (TokenizerException e) 
			{
				e.printStackTrace();
			}
			String stemmedKey = ts.next();
			if(stemmedKey.matches(pattern)) // put match in PostingsArray
			{
				Map<Integer, Integer> ps = (Map<Integer, Integer>) currTerm.getValue().list; // postingslist of current term
				Iterator<Entry<Integer, Integer>> i = ps.entrySet().iterator(); // current postingslist
				while(i.hasNext())
				{
					Entry<Integer, Integer> post = i.next();
					if(!postingsMap.containsKey(post.getKey()))
					{
						postingsMap.put(String.valueOf(post.getKey()), post.getValue());
					}
					else
					{
						int occurance = postingsMap.get(post.getKey()) + post.getValue();
						postingsMap.put(String.valueOf(post.getKey()), occurance);						
					}					
				}
			}	
		}
		if(postingsMap.size() > 0)
		{
			System.out.println("\nPostings list for '"+pattern+"':\n");
			postingsMap = showResults(postingsMap);
			return postingsMap;
		}				
		return null;			
	}
	
	/**
	 * Method to get the top k key terms from the given index
	 * The top here refers to the largest size of postings.
	 * @param k: The number of postings list requested
	 * @return An ordered collection of dictionary terms that satisfy the requirement
	 * If k is more than the total size of the index, return the full index and don't 
	 * pad the collection. Return null in case of an error or invalid inputs
	 */
	public Collection<String> getTopK(int k) 
	{
		LinkedHashMap<String, Integer> list = new LinkedHashMap<String, Integer>();
		list = (LinkedHashMap<String, Integer>) sortIndex();
		
		int count = 0;
		Collection<String> topK = new ArrayList<String>();
		
		Iterator<Entry<String, Integer>> itr = list.entrySet().iterator();
		
		System.out.println("\nTop "+k+" results:\n");
		while(itr.hasNext())
		{
			count++;
			if(count<=k)
			{
				Entry<String, Integer> entry = itr.next();
				topK.add(entry.getKey());
				System.out.println(entry.getKey()+" -> "+entry.getValue());
			}
			else
			{
				break;
			}
		}
		return topK;		
	}
	
	/**
	 * Method to execute a boolean AND query on the index
	 * @param terms The terms to be queried on
	 * @return An ordered map containing the results of the query
	 * The key is the value field of the dictionary and the value
	 * is the sum of occurrences across the different postings.
	 * The value with the highest cumulative count should be the
	 * first entry in the map.
	 */
	public Map<String, Integer> query(String... terms) 
	{
		ArrayList<Map<String, Integer>> maps = new ArrayList<Map<String, Integer>>();
		Map<String, Integer> result = new LinkedHashMap<String, Integer>();
		StringBuilder query = new StringBuilder();
		
		for(int i=0; i<terms.length;i++)
		{
			query = query.append(terms[i]+" ");
			if(terms[i].contains("?") || terms[i].contains("*")) // Wild card query
			{
				String pattern = "^" + terms[i].replaceAll("\\*", ".*").replaceAll("\\?", ".") + "$";
				Map<String, Integer> allPosts = getAllPostings(pattern);
				
				if(allPosts != null)
				{
					maps.add((LinkedHashMap<String, Integer>) allPosts);
				}
			}
			else
			{
				LinkedHashMap<String, Integer> map = (LinkedHashMap<String, Integer>) getPostings(terms[i]); // Boolean query
				if(map != null)
				{
					maps.add((LinkedHashMap<String, Integer>) map);
				}				
			}			
		}	
		if(maps.size() == 1)
		{
			result = (LinkedHashMap<String, Integer>) maps.get(0);
			System.out.println("\nOutput of (stemmed) Query -> "+query+":\n");
			result = showResults(result);
		}
		else if(maps.size() > 1)
		{
			result = checkPostings(maps); // output before sorting
			System.out.println("\nOutput (stemmed) of Query -> "+query+":\n");
			result = showResults(result);
		}
		else
		{
			result = null;
			System.out.println("\nThe query returned no results.\n");
		}
		return result;
	}
	
	/**
	 * Method to map document ids to their titles
	 */
	private Map<String, Integer> showResults(Map<String, Integer> result)
	{
		if(result!=null) 
		{
			int count = result.size();
			Iterator<Entry<String, Integer>> i = sharedDictionary.entrySet().iterator();
			while(i.hasNext())
			{
				Entry<String, Integer> e = i.next();
				int docID = e.getValue();
				if(result.containsKey(String.valueOf(docID)))
				{
					int frequency = result.get(String.valueOf(docID));
					result.put(e.getKey(), frequency);
					result.remove(String.valueOf(docID));
					count--;
					if(count==0) break;
				}
			}
			result = (LinkedHashMap<String, Integer>) sortList(result); // Sort the results
						
			Iterator<Entry<String, Integer>> itr = result.entrySet().iterator();
			while(itr.hasNext())
			{
				Entry<String, Integer> entry = itr.next();
				System.out.println("<"+entry.getKey()+", "+entry.getValue()+">");
			}					
		}
		else
		{
			System.out.println("The Query returned no results.\n");
		}
		return (LinkedHashMap<String, Integer>) result;
	}
	
	/**
	 *  Method to check if term is present in each postingslist
	 *  Returns the intersection of all postings list
	 */
	private Map<String, Integer> checkPostings(ArrayList<Map<String, Integer>> maps)
	{
		LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();	 
		Iterator<Entry<String, Integer>> itr = maps.get(0).entrySet().iterator(); // get first map
		
		while(itr.hasNext())
		{
			Entry<String, Integer> entry = itr.next();
			int count = entry.getValue();
			String docID = String.valueOf(entry.getKey());
			boolean found = true; // flag to check if current doc is present in all maps, else discard
			for(int i=1;i<maps.size();i++)
			{
				found = maps.get(i).containsKey(entry.getKey());
				if(found == false)
					break; // don't continue as entry not found in one of the postings list, so discard the entry.
				count += maps.get(i).get(entry.getKey());
			}
			if(found == true)
			{
				result.put(String.valueOf(docID), count);
			}
		}		
		if(result.size() > 0)
		{
			return result;
		}
		return null;	
	}	
	
	/**
	 *  Method to sort postings list in descending order of the occurances in any document
	 */
	private Map<String, Integer> sortList(Map<String, Integer> result2)  // <DocName, frequency>
	{
		ArrayList<IndexDetails> sortedList = new ArrayList<IndexDetails>();				
		Iterator<Entry<String, Integer>> itr = result2.entrySet().iterator();
		while(itr.hasNext())
		{
			Entry<String, Integer> entry = itr.next();
			IndexDetails docDetails = new IndexDetails(entry.getKey(), entry.getValue());
			sortedList.add(docDetails);
		}
		
		for (int i=0; i<sortedList.size()-1;i++) 
		{
			for (int j=i+1; j<sortedList.size(); j++) 
	        {
				if (sortedList.get(i).size < sortedList.get(j).size) 
	            {
	            	IndexDetails temp = sortedList.get(i);
	                sortedList.set(i,sortedList.get(j));
	                sortedList.set(j, temp);
	            } 
	        }
		}
		
		LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>(); // result map
		for(int k=0; k<sortedList.size();k++)
		{
			result.put(sortedList.get(k).id, sortedList.get(k).size);
		}
		return result;
	}
	
	/**
	 *  Method to sort the entire index in descending order of the occurances in its terms
	 */
	private Map<String, Integer> sortIndex()
	{
		ArrayList<IndexDetails> sortedList = new ArrayList<IndexDetails>();				
		Iterator<Entry<String, PostingsList>> itr = invertedIndex.entrySet().iterator();
		while(itr.hasNext())
		{
			Entry<String, PostingsList> entry = itr.next();
			IndexDetails postings = new IndexDetails(entry.getKey(), entry.getValue().numOccurances);
			sortedList.add(postings);
		}
		
		for (int i=0; i<sortedList.size()-1;i++) 
		{
			for (int j=i+1; j<sortedList.size(); j++) 
	        {
	            if (sortedList.get(i).size < sortedList.get(j).size) 
	            {
	            	IndexDetails temp = sortedList.get(i);
	                sortedList.set(i,sortedList.get(j));
	                sortedList.set(j, temp);
	            } 
	        }
		}
		
		LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>(); // result map
		for(int k=0; k<sortedList.size(); k++)
		{
			result.put(sortedList.get(k).id, sortedList.get(k).size);
		}
		return result;
	}
	
	class IndexDetails
	{
		String id;
		Integer size;
		
		public IndexDetails(String docId, int pSize)
		{
			id = docId;
			size = pSize;
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
	}
}
