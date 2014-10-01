/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.wikipedia;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import edu.buffalo.cse.ir.wikiindexer.indexer.INDEXFIELD;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.Tokenizer;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;

/**
 * A Callable document transformer that converts the given WikipediaDocument object
 * into an IndexableDocument object using the given Tokenizer
 *
 */
public class DocumentTransformer implements Callable<IndexableDocument> 
{
	Map<INDEXFIELD, Tokenizer> tknizerMap = null;
	WikipediaDocument doc = null;
	IndexableDocument indexableDoc = null;
	
	/**
	 * Default constructor, DO NOT change
	 * @param tknizerMap: A map mapping a fully initialized tokenizer to a given field type
	 * @param doc: The WikipediaDocument to be processed
	 */
	public DocumentTransformer(Map<INDEXFIELD, Tokenizer> wikiTknizerMap, WikipediaDocument wikiDoc) 
	{
		doc = wikiDoc;
		tknizerMap = wikiTknizerMap;
		try
		{
			indexableDoc = call();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	/**
	 * Method to trigger the transformation
	 * 
	 * 1) Read fields from Doc
	 * 2) Tokenize it to create streams 
	 * 3) Set these streams on Indexable doc
	 * 
	 * @throws TokenizerException In case any tokenization error occurs
	 */
	public IndexableDocument call() throws TokenizerException 
	{							
		String title = doc.getTitle().trim().toLowerCase().replaceAll("\\s+", "_");	
		IndexableDocument indexableDocument = new IndexableDocument(title);	
		
		TokenStream tknStrm = new TokenStream(this.doc.getAuthor());
		tknizerMap.get(INDEXFIELD.AUTHOR).tokenize(tknStrm);
		indexableDocument.addField(INDEXFIELD.AUTHOR, tknStrm);
		
		Set<String> links = doc.getLinks();
		Iterator<String> itr = links.iterator();
		StringBuilder strngBldr = new StringBuilder();
		
		while(itr.hasNext())
			strngBldr.append(itr.next()).append(" ");;
		
		TokenStream tknStrm1 = new TokenStream(strngBldr);
		tknizerMap.get(INDEXFIELD.LINK).tokenize(tknStrm1);
		indexableDocument.addField(INDEXFIELD.LINK, tknStrm1);
		strngBldr.setLength(0);
		
		List <WikipediaDocument.Section> sections = this.doc.getSections();
		StringBuilder strngBldr1 = new StringBuilder();
		
		for (int index = 0 ; index < sections.size() ; index++)
			strngBldr1.append(sections.get(index).getText()).append(" ");
		
		
		TokenStream tknStrm2 = new TokenStream(strngBldr1);
		tknizerMap.get(INDEXFIELD.TERM).tokenize(tknStrm2);
		indexableDocument.addField(INDEXFIELD.TERM, tknStrm2);
		strngBldr1.setLength(0);

		List <String> categories = doc.getCategories();
		StringBuilder strngBldr2 = new StringBuilder();
		for(int index = 0 ; index < categories.size() ; index++)
		strngBldr2.append(categories.get(index)).append(" ");
		
		TokenStream tknStrm3 = new TokenStream(strngBldr2);
		tknizerMap.get(INDEXFIELD.CATEGORY).tokenize(tknStrm3);
		indexableDocument.addField(INDEXFIELD.CATEGORY, tknStrm3);
		strngBldr2.setLength(0);
						
		//Returns Indexable Document
		return indexableDocument;
	}
	
}
