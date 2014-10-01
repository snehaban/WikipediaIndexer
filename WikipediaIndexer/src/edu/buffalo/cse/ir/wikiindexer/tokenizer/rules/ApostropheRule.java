/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.util.HashMap;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

/**
 * Rules : --
 * 1) Any possessive apostrophes should be removed (‘s s’ or just ‘at the end of a word).
 * 2) Common contractions should be replaced with expanded forms but treated as one token. (e.g. should’ve => should have).
 * 3) All other apostrophes should be removed.
 * 
 * AFTER TOKENIZATION.
 */
@RuleClass(className = RULENAMES.APOSTROPHE)
public class ApostropheRule implements TokenizerRule
{
	public void apply(TokenStream stream) throws TokenizerException 
	{
		if (stream != null)
		{		
			TokenStream ts = new TokenStream((String)null);
			while(stream.hasNext())
			{
				String token = stream.next();	//Read the first token				
				for(String str: token.split("\\s+"))
				{
					if(str.contains("\'"))
					{
						//Check if it has an expansion
						String tempStr=Contractions.getExpansion(str);
												
						if (tempStr !=null && !tempStr.equals(""))
						{
							ts.append(tempStr.split("\\s+"));
						}
						else if(str.endsWith("'") || str.endsWith("'s") || str.endsWith("'S"))
						{							
							String s = str.replace("'s", "").replace("'S", "").replace("'", "").trim();
							if(s!=null && s.length()>0)
								ts.append(s);					
						}
						else
						{
							String s = str.replaceAll("'", "").trim();
							if(s!=null && s.length()>0)
								ts.append(s);	
						}											
					} // end if (')
					else
					{
						if(str!=null && str.trim().length()>0)
						ts.append(str.trim());
					}
														
				} // end for
				stream.previous();
				stream.remove();							
			}
			if(ts!=null) stream.merge(ts);
		}
	}
	
	static class Contractions
	{
		static HashMap <String,String> contractionsMap = new HashMap<String,String>();
		
		static String key = "";
		static String value = "";
		
		static  String contractionsStr = 
						 "aren't are not/can't cannot/couldn't could not/didn't did not/doesn't does not/don't do not/hadn't had not/hasn't has not/"
						+"haven't have not/he'd he would/he'll he will/he's he is/I'd I would/I'll I will/I'm I am/I've I have/isn't is not/"
						+"it's it is/let's let us/mightn't might not/mustn't must not/shan't shall not/she'd she would/she'll she will/"
						+"she's she is/should've should have/shouldn't should not/that's that is/there's there is/they'll they will/they're they are/they've they have/"
						+"they'd they would/we'd we would/we're we are/we've we have/weren't were not/what'll what will/what're what are/what's what is/"
						+"what've what have/where's where is/who'd who had/who'll who will/who're who are/who's who is/who've who have/"
						+"won't will not/would've would have/wouldn't would not/you'd you had/you'll you will/you're you are/you've you have/'em them/";
		
		static HashMap<String, String> rules = new HashMap<String,String>();
		
		static
		{
			while(contractionsStr.contains("/"))
			{
				key = contractionsStr.substring(0,contractionsStr.indexOf(" ")).trim().toLowerCase();
				value = contractionsStr.substring(contractionsStr.indexOf(" "), contractionsStr.indexOf("/")).trim().toLowerCase();				
				contractionsMap.put(key,value);
				
				if(contractionsStr!="/")
				contractionsStr = contractionsStr.substring(contractionsStr.indexOf("/")+1);				
			}
			rules.put("'d", " would");
			rules.put("'ll", " will");
			rules.put("'ve", " have");
			rules.put("n't", " not");
			rules.put("'re", " are");
			rules.put("'em", " them");
		}
		
		public static String getExpansion(String contratctedStr)
		{
			String resultToken = (String)null;
			if (contratctedStr != null && !contratctedStr.equals(""))
			{
				resultToken = contractionsMap.get(contratctedStr.toLowerCase());
			}
						
			if(resultToken == null) // Try applying rules
			{
				int index = contratctedStr.lastIndexOf('\'');
				String key = contratctedStr.substring(index); 				
				if(rules.containsKey(key))
				{
					if(index > 0)
						resultToken = contratctedStr.substring(0, contratctedStr.lastIndexOf('\'') - 1) + rules.get(key);
					else
						resultToken = rules.get(key).trim();
				}	
			}
			
			if(resultToken!=null && Character.isUpperCase(contratctedStr.charAt(0))) // Capitalize first char
			{
				resultToken = resultToken.substring(0, 1).toUpperCase() + resultToken.substring(1);
			}
			
			return resultToken;
		}
		
	}
}
