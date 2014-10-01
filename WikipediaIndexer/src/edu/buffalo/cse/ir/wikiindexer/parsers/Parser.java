package edu.buffalo.cse.ir.wikiindexer.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.buffalo.cse.ir.wikiindexer.wikipedia.WikipediaDocument;
import edu.buffalo.cse.ir.wikiindexer.wikipedia.WikipediaParser;

public class Parser extends DefaultHandler {
	/* Variables */

	boolean titleElementStart = false;
	boolean textElementStart = false;
	boolean idElementStart = false;
	boolean revisionElementStart = false;
	boolean timestampElementStart = false;
	boolean ipElementStart = false;
	boolean usernameElementStart = false;
	int count=0;
	String title = "";
	String timeStamp = "";
	String author = "";
	String textString = "";
	StringBuilder textElementCompleteData = new StringBuilder("");
	long idFromXml = 0;
	int id = 0;
	Collection<WikipediaDocument> collectionWikiPediaDocs = new ArrayList<WikipediaDocument>();
	
	private final Properties props;

	/**
	 * 
	 * @param idxConfig
	 * @param parser
	 */
	public Parser(Properties idxProps) {
		props = idxProps;
	}
	
	/**
	 * Overriden Methods
	 */
	
	public void startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException
	{
		if (qName.equalsIgnoreCase("title"))
			titleElementStart = true;
		if (qName.equalsIgnoreCase("id"))
			idElementStart = true;
		if (qName.equalsIgnoreCase("revision"))
			revisionElementStart = true;
		if (qName.equalsIgnoreCase("timestamp"))
			timestampElementStart = true;
		if (qName.equalsIgnoreCase("ip"))
			ipElementStart = true;
		if (qName.equalsIgnoreCase("username"))
			usernameElementStart = true;
		if (qName.equalsIgnoreCase("text"))
		{
			textElementStart = true;
			textElementCompleteData.setLength(0);
			textString = "";
		}
	}
	
	public void endElement(String uri, String localName,
			String qName)  throws SAXException
	{
		if (qName.equalsIgnoreCase("title"))
			titleElementStart = false;
		if (qName.equalsIgnoreCase("revision"))
			revisionElementStart = false;
		if (qName.equalsIgnoreCase("id"))
			idElementStart = false;
		if (qName.equalsIgnoreCase("timestamp"))
			timestampElementStart = false;
		if (qName.equalsIgnoreCase("ip"))
			ipElementStart = false;
		if (qName.equalsIgnoreCase("username"))
			usernameElementStart = false;
		if (qName.equalsIgnoreCase("text"))
		{
			textElementStart = false;
			String tempString ="";
			tempString = WikipediaParser.parseTemplates(textElementCompleteData.toString());
			tempString = WikipediaParser.parseTagFormatting(tempString);
			tempString = WikipediaParser.parseListItem(tempString);
			tempString = WikipediaParser.parseTextFormatting(tempString);
			
			WikipediaDocument wikipediaDocument = WikipediaParser.setWikipediaDoc(title,id,author,timeStamp,tempString);
			if(wikipediaDocument != null)
			{
				this.add(wikipediaDocument);
			}
		}
	}
		
	public void characters(char ch[], int start, int length)  throws SAXException
	{
		// Assign values to WikiPediaDoc and add it to the queue
		if (titleElementStart) 
			title = new String(ch, start, length);
			
		if (idElementStart && !revisionElementStart) 
		{
			idFromXml = Integer.parseInt(new String(ch, start,
						length));
			id = (int) idFromXml;
		}

		if (timestampElementStart) 
			timeStamp = new String(ch, start, length);
		
		if (ipElementStart) 
			author = new String(ch, start, length);
		
		if (usernameElementStart)
			author= new String(ch, start, length);
		
		if (textElementStart) 
		{
			textString = new String(ch, start, length);
			textElementCompleteData = textElementCompleteData.append(textString);
		}
	}

	/**
	 * Method performs parsing of document and populates the collection with the
	 * Parsed document
	 * 
	 * @param filename
	 * @param docs
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void parse (String filename, Collection<WikipediaDocument> docs) 
	{
		// SAX Parser
		SAXParserFactory saxParserfactory = SAXParserFactory.newInstance();
		
		try
		{
			SAXParser saxParser = saxParserfactory.newSAXParser();
			//Parser handler = new Parser(props);
			
			if (filename !=null && !filename.equals(""))
				saxParser.parse(new File(filename), this);
			
			docs.addAll(this.collectionWikiPediaDocs);
		}
		catch (SAXException saxException)
		{
			saxException.printStackTrace();
		}
		catch(FileNotFoundException fnfException)
		{
			System.out.println("File   : " + filename	+ " Not found at the specified location.");
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
		}
		catch (ParserConfigurationException parserConfigurationException)
		{
			parserConfigurationException.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Method to add the given document to the collection. PLEASE USE THIS
	 * METHOD TO POPULATE THE COLLECTION AS YOU PARSE DOCUMENTS For better
	 * performance, add the document to the collection only after you have
	 * completely populated it, i.e., parsing is complete for that document.
	 * 
	 * @param doc
	 *            : The WikipediaDocument to be added
	 * @param documents
	 *            : The collection of WikipediaDocuments to be added to
	 */
	/*private synchronized void add(WikipediaDocument doc, Collection<WikipediaDocument> documents)
	{
		documents.add(doc);
	}
	*/private synchronized void add(WikipediaDocument doc)
	{
		this.collectionWikiPediaDocs.add(doc);
		}
}
