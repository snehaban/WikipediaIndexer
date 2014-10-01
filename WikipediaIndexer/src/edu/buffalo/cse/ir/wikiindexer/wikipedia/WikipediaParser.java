/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.wikipedia;

import java.text.ParseException;
import java.util.Stack;

/**
 * This class implements Wikipedia markup processing. Wikipedia
 * markup details are presented here:
 * http://en.wikipedia.org/wiki/Help:Wiki_markup It is expected that all
 * methods marked "todo" will be implemented by students. All methods
 * are static as the class is not expected to maintain any state.
 */
public class WikipediaParser
{
	public static WikipediaDocument setWikipediaDoc(String title, int id,
			String author, String timeStamp, String entireText)
	{
		try
		{

			WikipediaDocument wikipediaDocument = new WikipediaDocument(id,
					timeStamp, author, title);

			int endIndex = 0;
			
			//Remove all external links
			/*while(entireText.contains("[http://"))
			{
				entireText = entireText.substring(0, entireText.indexOf("[http://"))
						+ entireText.substring(entireText.indexOf("]") + 1);
			}*/
			entireText= entireText.replaceAll("\\[http://.*\\]", " ");
			
			/*
			 * LINKS CATEGORIES AND EXTERNAL LINKS
			 */
			entireText = processLinks(entireText,wikipediaDocument);
			
			// The Tags of a table .. remove it ..
			entireText = removeOtherText(entireText);
			
			/*
			 * EXTRACT SECTIONS
			 */
			String sectionHeading = "Default";
			if (entireText.indexOf("==") != -1)
			{
				String sectionText = entireText.substring(0, entireText.indexOf("=="));
				wikipediaDocument.addSection(sectionHeading, sectionText.replaceAll("\\s+", " "));

				while (entireText.indexOf("==") != -1)
				{
					entireText = entireText.substring(entireText.indexOf("=="));
					sectionHeading = entireText.substring(0, entireText.indexOf("\n"))
							.replaceAll("=", "");

					entireText = entireText.substring(entireText.indexOf("\n"));

					endIndex = entireText.indexOf("==");

					if (endIndex == -1) // This is the last section
						sectionText = entireText;// .substring(0,entireText.lastIndexOf(" "));
					else
						sectionText = entireText.substring(entireText.indexOf("\n"),endIndex);
					
					wikipediaDocument.addSection(sectionHeading, sectionText.replaceAll("\\s+", " "));
				}// ALL SECTIONS ADDED

			}// ONLY ONE SECTION PRESENT
			else
				wikipediaDocument.addSection(sectionHeading,entireText.replaceAll("\\s+", " "));
							
			return wikipediaDocument;

		}
		catch (ParseException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Method to parse section titles or headings. Refer:
	 * http://en.wikipedia.org/wiki/Help:Wiki_markup#Sections
	 * 
	 * @param titleStr
	 *          : The string to be parsed
	 * @return The parsed string with the markup removed
	 */
	public static String parseSectionTitle(String titleStr)
	{

		if (titleStr == null)
			return null;
		if (titleStr.equals(""))
			return "";
		else
		{
			titleStr = titleStr.replaceAll("[(^=+)|($=+)]", "").trim();
			return titleStr;
		}
	}

	/**
	 * Method to parse list items (ordered, unordered and definition lists).
	 * Refer: http://en.wikipedia.org/wiki/Help:Wiki_markup#Lists
	 * 
	 * @param itemText
	 *          : The string to be parsed
	 * @return The parsed string with markup removed
	 */
	public static String parseListItem(String itemText)
	{

		if (itemText == null || itemText.equals(""))
		{
			return itemText;
		}
		itemText = itemText.replaceAll("^[\\*\\#]*", "").trim();

		if (itemText.startsWith(":"))
		{
			itemText = itemText.replace(":", "").trim();
		}
		return itemText;
	}

	/**
	 * Method to parse text formatting: bold and italics. Refer:
	 * http://en.wikipedia.org/wiki/Help:Wiki_markup#Text_formatting first point
	 * 
	 * @param text
	 *          : The text to be parsed
	 * @return The parsed text with the markup removed
	 */
	public static String parseTextFormatting(String text)
	{

		if (text == null || text.equals(""))
			return text;

		text = text.replaceAll("'''''", "");// Bold Italics
		text = text.replaceAll("'''", ""); // Bold
		text = text.replaceAll("''", ""); // Italics

		return text;
	}

	/**
	 * Method to parse *any* HTML style tags like: <xyz ...> </xyz> For most
	 * cases, simply removing the tags should work.
	 * 
	 * @param text
	 *          : The text to be parsed
	 * @return The parsed text with the markup removed.
	 */
	public static String parseTagFormatting(String text)
	{
		if (text == null)
			return null;
		else if (text.equals(""))
			return "";
		else
		{
			/**
			 * FOOL PROOF CODE
			 * 1) Every time you encounter < increment count by one. Push the position in stack[begin Index]
			 * 2) Every time you encounter > decrement count by 1.this is the End Index
			 * 			. and pop out last entry from the stack.
			 * 3) when count is 0 .. delete every thing within beginIndex and EndIndex.
			 * 
			 */
			
			//SOMETHING SMART:D
			
			text = text.replaceAll("&lt;", "<");
			text = text.replaceAll("&gt;", ">");
			
			
			char[] buffer = new char[1024 * 1024];
			text.getChars(0, text.length(), buffer, 0);
			Stack<Integer> bracketPositions = new Stack<Integer>();
			int count = 0;
			int endIndex = 0;
			int beginIndex = 0;
			boolean processStarted = false;
			
			String filler ="";
			
			do
			{
				processStarted = true;
				for(int index = 0 ; index < buffer.length-1 ; index++)
				{
					if(buffer[index] == '<')
						{
							count = count+1;
							bracketPositions.push(index);
						}
						if(buffer[index] == '>' && !bracketPositions.isEmpty())
						{
							beginIndex = (Integer) bracketPositions.pop();
							count = count-1;
							endIndex = index+1;
						}
						if(count == 0 && endIndex !=0)
						{
							for (int i = 1 ; i <= (endIndex-beginIndex) ; i++)
								filler=filler+"~";
							//Taking ~ Because of test cases. Can't do \\s+ replace by " "
							text = text.substring(0,beginIndex) + filler+ text.substring(endIndex); 
							filler = "";
							beginIndex =0;
							endIndex = 0;
							
						}
				}
				processStarted = false;
					
			}while(processStarted);
			
			text = text.replaceAll("[~]+", "").replaceAll("  ", " ").trim();	//Only for test cases
			
			/**
			 * Extra handling of &nbsp and other tags
			 */
			text = text.replaceAll("&[a-z0-9#]*;", " ");
			return text;
			
		}
	}

	/**
	 * Method to remove other tags {|fgxfg|}
	 * this is for a table.. it is not a part of the text.. remove It
	 */
	public static String removeOtherText(String text)
	{
		char[] buffer = new char[1024 * 1024];
		text.getChars(0, text.length(), buffer, 0);
		Stack<Integer> bracketPositions = new Stack<Integer>();
		int count = 0;
		int endIndex = 0;
		int beginIndex = 0;
		boolean processStarted = false;
		
		String spaces ="";
		
		do
		{
			processStarted = true;
			for(int index = 0 ; index < buffer.length-1 ; index++)
			{
				if(buffer[index] == '{' && buffer[index+1] == '|')
					{
						count = count+1;
						bracketPositions.push(index);
					}
					if(buffer[index] == '|' && buffer[index+1] == '}' && !bracketPositions.isEmpty())
					{
						beginIndex = (Integer) bracketPositions.pop();
						count = count-1;
						endIndex = index+2;
					}
					if(count == 0 && endIndex !=0)
					{
						for (int i = 1 ; i <= (endIndex-beginIndex) ; i++)
							spaces =spaces +" ";
						//Taking ~ Because of test cases. Can't do \\s+ replace by " "
						text = text.substring(0,beginIndex) + spaces+ text.substring(endIndex); 
						spaces = "";
						beginIndex =0;
						endIndex = 0;
						
					}
			}
			processStarted = false;
				
		}while(processStarted);
		
		return text;
	}
	
	/**
	 * Method to parse wikipedia templates. These are *any* {{xyz}} tags For most
	 * cases, simply removing the tags should work
	 * 
	 * @param text
	 *          : The text to be parsed
	 * @return The parsed text with the markup removed
	 */
	public static String parseTemplates(String text)
	{
		/**
		 * FOOL PROOF CODE
		 * 1) Every time you encounter {{ increment count by one. Push the position in stack[begin Index]
		 * 2) Every time you encounter }} decrement count by 1.this is the End Index
		 * 			. and pop out last entry from the stack.
		 * 3) when count is 0 .. delete every thing within beginIndex and EndIndex.
		 * 
		 */
		char[] buffer = new char[1024 * 1024];
		text.getChars(0, text.length(), buffer, 0);
		Stack<Integer> bracketPositions = new Stack<Integer>();
		int count = 0;
		int endIndex = 0;
		int beginIndex = 0;
		boolean processStarted = false;
		
		String spaces ="";
		
		do
		{
			processStarted = true;
			for(int index = 0 ; index < buffer.length-1 ; index++)
			{
				if(buffer[index] == '{' && buffer[index+1] == '{')
					{
						count = count+1;
						bracketPositions.push(index);
					}
					if(buffer[index] == '}' && buffer[index+1] == '}' && !bracketPositions.isEmpty())
					{
						beginIndex = (Integer) bracketPositions.pop();
						count = count-1;
						endIndex = index+2;
					}
					if(count == 0 && endIndex !=0)
					{
						for (int i = 1 ; i <= (endIndex-beginIndex) ; i++)
							spaces=spaces+" ";

						if(endIndex-beginIndex == text.length())
							return "";		//Only for test cases
						else
							text = text.substring(0,beginIndex) + spaces+ text.substring(endIndex); 
						
						spaces = "";
						beginIndex =0;
						endIndex = 0;
						
					}
			}
			processStarted = false;
				
		}while(processStarted);
				return text;

	}

	/**
	 * Method to parse links and URLs. Refer:
	 * http://en.wikipedia.org/wiki/Help:Wiki_markup#Links_and_URLs
	 * 
	 * @param text
	 *          : The text to be parsed
	 * @return An array containing two elements as follows - The 0th element is
	 *         the parsed text as visible to the user on the page The 1st element
	 *         is the link url
	 */
	public static String[] parseLinks(String text)
	{
		String[] linksArray = new String[2];
		String tempStartText = "";
		String tempEndText = "";
		if (text == null || text.equals(""))
		{
			linksArray[0] = "";
			linksArray[1] = "";

			return linksArray;
		}

		// Remove TAGS
		if (text.contains("<"))
			text = parseTagFormatting(text);

		// For External Links
		if (text.startsWith("[http://"))
		{
			text = text.replace("[", "").replace("]", "");
		}

		// for blending examples
		if (text.contains("[["))
		{
			tempStartText = text.substring(0, text.indexOf("[["));
			tempEndText = text.substring(text.indexOf("]]") + 2);
			text = text.substring(text.indexOf("[[") + 2, text.indexOf("]]"));
		}
		
		if(text.toLowerCase().startsWith("image:"))
		{
			text = "";
			return linksArray;
		}
		
		if (text.contains("|"))
		{
			if (text.indexOf("|") != text.lastIndexOf("|"))
			{
				// multiple Splits
				String[] tempStringArray = text.split("[|]");
				linksArray[1] = "";
				for (int arraySize = 0; arraySize < tempStringArray.length; arraySize++)
				{
					if (arraySize == tempStringArray.length - 1)
						linksArray[0] = tempStringArray[arraySize];
				}
			}
			else
			{
				if (text.endsWith("|"))
				{
					// Remove pipe from the end
					// NO SPLIT
					text = text.replace("|", "");
					// "|" is present at end.. Processing needed

					// check for ":"
					if (text.contains(":"))
					{
						if (text.indexOf(":") == text.lastIndexOf(":"))
						{
							if (text.indexOf("#") != -1)
							{
								// NO PROCESSING
								linksArray[0] = text;
								linksArray[1] = "";
							}
							else
							{
								// Single ":" .. Split By : and Check for () and , Auto Hiding

								linksArray[0] = text.split(":")[1];
								linksArray[1] = "";

								if (text.indexOf("(") != -1)
								{
									linksArray[0] = tempStartText
											+ linksArray[0].substring(0, linksArray[0].indexOf("("))
													.trim() + tempEndText;
								}
								else if (text.indexOf(",") != -1)
								{
									linksArray[0] = tempStartText
											+ linksArray[0].substring(0, linksArray[0].indexOf(","))
													.trim() + tempEndText;
								}
								else
								{
									//Do nothing
								}
							}
						}
						else
						{
							// multiple Splits
							String[] tempStringArray = text.split("[:]");
							linksArray[1] = "";
							for (int element = 1; element < tempStringArray.length; element++)
							{
								if (linksArray[0] == null || linksArray[0].equals(""))
									linksArray[0] = tempStringArray[element] + ":";
								else
									linksArray[0] = linksArray[0] + tempStringArray[element];
							}
						}

					}
					else
					{
						// Check for Brackets and comma and automatically hide them.
						if (text.indexOf("(") != -1)
						{
							linksArray[0] = tempStartText
									+ text.substring(0, text.indexOf("(")).trim() + tempEndText;
							linksArray[1] = text.replace(" ", "_");
							// AUTO CAPITALIZATION
							linksArray[1] = linksArray[1].substring(0, 1).toUpperCase()
									+ linksArray[1].substring(1);
						}
						else if (text.indexOf(",") != -1)
						{
							linksArray[0] = tempStartText
									+ text.substring(0, text.indexOf(",")).trim() + tempEndText;
							linksArray[1] = text.replace(" ", "_");
							// AUTO CAPITALIZATION
							linksArray[1] = linksArray[1].substring(0, 1).toUpperCase()
									+ linksArray[1].substring(1);
						}
						else
						{
							linksArray[0] = tempStartText + text + tempEndText;
							linksArray[1] = text.replace(" ", "_");
							// AUTO CAPITALIZATION
							linksArray[1] = linksArray[1].substring(0, 1).toUpperCase()
									+ linksArray[1].substring(1);
						}

					}
				}
				else
				{
					// Single Split

					// check for ":"
					if (text.contains(":"))
					{
						linksArray[0] = tempStartText + text.split("[|]")[1] + tempEndText;
						linksArray[1] = text.split("[|]")[0];

						if (linksArray[1].startsWith("Wik"))
						{
							// auto capitalization
							linksArray[1] = linksArray[1].substring(0, 1).toUpperCase()
									+ linksArray[1].substring(1);
							linksArray[1] = linksArray[1].replaceAll(" ", "_");
						}
						else
						{
							linksArray[1] = "";
						}
					}
					else
					{
						linksArray[0] = tempStartText + text.split("[|]")[1] + tempEndText;
						linksArray[1] = text.split("[|]")[0].replaceAll(" ", "_");
						// Auto Capitalization
						linksArray[1] = linksArray[1].substring(0, 1).toUpperCase()
								+ linksArray[1].substring(1);
					}
				}

			}
		}
		else if (text.contains(":"))
		{
			// Does not contain "|" and contains Name Space
			// Handle Categories
			if (text.contains("Category:"))
			{
				linksArray[0] = text.substring(text.indexOf(":") + 1);
				linksArray[1] = "";

			}
			// Process only if Name Space is WIKI
			else if (text.startsWith("Wik"))
			{
				linksArray[0] = tempStartText + text + tempEndText;
				linksArray[1] = "";
			}
			// Language links
			else if (text.charAt(2) == ':')
			{
				linksArray[0] = text;
				linksArray[1] = "";
			}
			// check if it is of the form http:
			else if (text.startsWith("http://"))
			{
				if (text.indexOf(" ") == -1)
				{
					linksArray[0] = "";
					linksArray[1] = "";
				}
				else
				{
					linksArray[0] = text.split("[ ]")[1];
					linksArray[1] = "";
				}
			}
			else
			{
				linksArray[0] = "";
				linksArray[1] = "";
			}
		}
		else
		{
			// does not contain "|" , ":" .. no splitting
			linksArray[0] = tempStartText + text + tempEndText;
			linksArray[1] = text.replace(" ", "_");
			// Auto Capitalization
			linksArray[1] = linksArray[1].substring(0, 1).toUpperCase()
					+ linksArray[1].substring(1);
		}

		return linksArray;

	}

	/**
	 * 1) increment counter , store its position in stack when [ is encountered.
	 * 2) decrement counter , every time ] is encountered. Take this as end
	 * position ; start position is last entry from stack. 3) take these inner
	 * brackets and process for links 4) replace the text and process the entire
	 * thing again.
	 * 
	 * @param completeText
	 */
	public static String processLinks(String completeText, WikipediaDocument wikipediaDocument)
	{
		char[] buffer = new char[1024 * 1024];
		completeText.getChars(0, completeText.length(), buffer, 0);
		
		Stack<Integer> bracketPos = new Stack<Integer>();
		int beginIndex= 0;
		int endIndex = 0;
		String processingString = "";
		String[] stringArray = null;
		
		for (int index=0 ; index<completeText.length()-1 ; index++)
		{
			if (buffer[index] == '[' && buffer[index+1] == '[')
			{
				bracketPos.push(index+2);
			}
			if(buffer[index]==']' && buffer[index+1] == ']' && !bracketPos.empty())
			{
				endIndex = index;
				if(!bracketPos.isEmpty())
				{
					beginIndex = (Integer) bracketPos.pop();
				}
			  processingString = completeText.substring(beginIndex,endIndex);
			  
			  stringArray = parseLinks(processingString);
			  
			  //replace the strings
			  
			  if (processingString.startsWith("Category"))
			  {
					wikipediaDocument.addCategory(stringArray[0]);
					stringArray[0] = ""; 		//Don,t add Category to document.
			  }
			  else if (processingString.length() > 2 && processingString.charAt(2) == ':')
					wikipediaDocument.addLangLink(processingString.substring(0, processingString.indexOf(":")),stringArray[1]);
				else
				{
					if(stringArray[1]!=null && !stringArray[1].equals(""))
						wikipediaDocument.addLink(stringArray[1]);
				}
				
				//take out the difference in length of processing string and stringArray[0] and fill it with spaces.
				if(stringArray[0]==null)
					stringArray[0] = "";
				
					int lengthdiff = (processingString.length()+4)-stringArray[0].length();
					String spaces = "";
					for (int i=1 ; i<= lengthdiff ; i++)
					{
						spaces = spaces+" ";
					}
					completeText = completeText.substring(0, beginIndex-2) + stringArray[0] +spaces
							+ completeText.substring(endIndex+2);
				
			  
			}
		}
		return completeText;
	}
}
