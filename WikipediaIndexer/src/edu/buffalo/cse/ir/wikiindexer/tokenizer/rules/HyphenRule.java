package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

@RuleClass(className = RULENAMES.HYPHEN)
public class HyphenRule implements TokenizerRule 
{
	@Override
	public void apply(TokenStream stream) throws TokenizerException 
	{
		if (stream != null && stream.hasNext())
		{
			while(stream.hasNext())
			{
				String token= stream.next();
				token = token.replaceAll("(^[-]+|\\s+[-]+\\s+|[-]+$|\\s+[-]+|[-]+\\s+)", " ").trim().replaceAll("\\s+"," "); // remove starting, ending and single hyphens		
				token = token.replaceAll("([a-zA-Z]+)[-]([a-zA-Z]+)\\s+", "$1 $2 ").trim(); //remove hyphen between words
				token = token.replaceAll("([a-zA-Z]+)[-]([a-zA-Z]+)$+", "$1 $2"); //remove from last hyphened token
		        stream.previous();
		        if(token.trim().length()>0)
		        	stream.set(token);
		        else
		        	stream.remove();
		        stream.next();
	        }
			stream.reset();
		}
	}

}
