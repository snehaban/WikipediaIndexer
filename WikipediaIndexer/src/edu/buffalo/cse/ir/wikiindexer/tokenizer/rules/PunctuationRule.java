package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

@RuleClass(className = RULENAMES.PUNCTUATION)
public class PunctuationRule implements TokenizerRule 
{
	public void apply(TokenStream stream) throws TokenizerException 
	{
		if (stream != null && stream.hasNext())
		{
			while(stream.hasNext())
			{
				String token= stream.next();
				token = token.trim().replace("\"", ""); // Remove all double quotes
				//token = token.trim().replaceAll("^[']+|\\s+[']", " "); // Remove all starting single quotes
				token = token.replaceAll("(\\p{Punct}+(?<!')\\s+|\\p{Punct}+(?<!')$)", " ").trim().replaceAll("\\s+"," "); // remove remaining punctuations
				
				if(token.startsWith("'")) ;
				else if(!token.matches("^[!]\\w+"))
				{
					token = token.replaceAll("^(\\p{Punct}+)","").trim(); 
				}
				token = token.replaceAll("[()\\[\\]\\{\\}]", " ").replaceAll("\\s+"," "); // remove remaining braces
				token = token.replaceAll("[;,|]|\\s*[\\p{Punct}+&&[^\\.!]{0,1}](?<![\\w\\d]+)\\s*", " ");
				token = token.replaceAll("(?<=\\w+)[;,|](?=\\w+)", " ");
				token = token.replaceAll("\\s+\\p{Punct}+\\s+", " ").trim();
		        stream.previous();
		        if(token.trim().length()>0)
		        	stream.set(token.trim());
		        else
		        	stream.remove();
		        stream.next();
	        }
			stream.reset();
		}
	}	
}
