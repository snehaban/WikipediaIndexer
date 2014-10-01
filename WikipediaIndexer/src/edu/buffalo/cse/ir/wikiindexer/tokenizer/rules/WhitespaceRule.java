package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

@RuleClass(className = RULENAMES.WHITESPACE)
public class WhitespaceRule implements TokenizerRule 
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
					ts.append(str);													
				} 
				stream.previous();
				stream.remove();							
			}
			if(ts!=null) stream.merge(ts);
		}
	}
}
