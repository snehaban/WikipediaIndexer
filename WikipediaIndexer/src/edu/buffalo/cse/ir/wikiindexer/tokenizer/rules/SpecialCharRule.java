package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

@RuleClass(className = RULENAMES.SPECIALCHARS)
public class SpecialCharRule implements TokenizerRule 
{
	public void apply(TokenStream stream) throws TokenizerException 
	{		
		if (stream != null && stream.hasNext())
		{
			int count = 0;			
			while(stream.hasNext())
			{
				count++;
				String token= stream.next();				
				// Remove punctuations
				token = token.trim().replace("\"", ""); // Remove all double quotes
				token = token.trim().replaceAll("^[']+|\\s+[']", " "); // Remove all starting single quotes
				token = token.replaceAll("(\\p{Punct}+(?<!-)\\s+|\\p{Punct}+(?<!-)$)", " ").trim().replaceAll("\\s+"," "); // remove remaining punctuations except hyphen
				token = token.replaceAll("[()\\[\\]\\{\\}]", " ").replaceAll("\\s+"," ").trim(); // remove remaining punctuations				        
				
				// Replace other chars with space
				token = token.replaceAll("[^\\w-'&&[^\\p{P}]]", " ").replaceAll("\\s+"," ").trim(); 
				token = token.replaceAll("[@#/<>+=%\\&\\*\\$\\^]", " ").replaceAll("\\s+"," ").trim();  // * ^ $ &
				token = token.replaceAll("[^\\x00-\\x7F]", " ");
				
		        stream.previous();		        
		        if(token==null || token.length()==0)
		        {
		        	stream.remove();
		        }
		        else
		        {
		        	stream.set(token);
		        	stream.next();
		        }		        
	        }
			
			if(count>1 && stream!=null) // for post-tokenization
			{
				TokenStream ts = new TokenStream((String)null);
				stream.reset();
				while(stream.hasNext())
				{
					String token = stream.next();	//Read the first token				
					for(String str: token.split("\\s+"))
					{					
						ts.append(str);													
					} 
					stream.previous();
					stream.remove();							
				}
				if(ts!=null) stream.merge(ts);
			}
			stream.reset();
		}
	}	
}