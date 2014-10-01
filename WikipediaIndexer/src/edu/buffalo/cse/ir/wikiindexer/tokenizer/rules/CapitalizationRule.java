/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

/**
 *Convert everything to lowercase
 *Unless upper case occurs at any position except the first alphabet
 *Upper case is at the first position but the word is not the first in the sentence
 *
 *Caps at fist post.. word not First in sentence -- convert to lowercase
 */
@RuleClass(className = RULENAMES.CAPITALIZATION)
public class CapitalizationRule implements TokenizerRule
{
	public void apply(TokenStream stream) throws TokenizerException {
		if (stream != null)
		{
			TokenStream ts = new TokenStream((String)null);
			boolean isFirstToken = true;
			String tokens ="";
			int count = 0;
			
			while (stream.hasNext())
			{
				++count;
				tokens = stream.next();
				for(String str: tokens.split("\\s+"))
				{
					if(isFirstToken)
					{
						//lower case if not all caps
						if(!str.matches("[A-Z]+"))
						{
							str = str.toLowerCase();
							if(str.trim().length() > 0)
								ts.append(str);
						}
						isFirstToken = false;
					}
					else
					{	
						//to small case unless all caps or camel-cased
						if(str.length()>0)
						{
							if (str.matches("[A-Z]+") || str.matches("[a-z]+[A-Z]+[a-z]*")||str.substring(0, 1).matches("[A-Z]"))
							{
								if(str.trim().length()>0)
									ts.append(str.trim());
							}
							else
							{
								str = str.toLowerCase();
								if(str.trim().length()>0)
								ts.append(str);
							}	
						}
					}
				}
				stream.previous();
				stream.remove();
			}
			if(ts!=null) stream.merge(ts);
			
			// Merge entire Stream together
			if (count <=1)
			{
				while(stream.hasNext())
				{
					//read first
					//merge with next
					stream.mergeWithNext();
				}
			}
		}
	}
}

