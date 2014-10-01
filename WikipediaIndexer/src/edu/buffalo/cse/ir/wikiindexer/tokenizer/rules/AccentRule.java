/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

import java.text.Normalizer;
import java.util.regex.Pattern;


@RuleClass(className = RULENAMES.ACCENTS)
public class AccentRule implements TokenizerRule
{
	public void apply(TokenStream stream) throws TokenizerException {
		if (stream != null)
		{
			String tokens ="";
			while(stream.hasNext())
			{
				tokens = stream.next();
				String normalisedStr = Normalizer.normalize(tokens, Normalizer.Form.NFD);
				Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
				tokens = pattern.matcher(normalisedStr).replaceAll("");
				stream.previous();
				if(tokens.trim().length()>0)
		        	stream.set(tokens);
		        else
		        	stream.remove();
				stream.next();
			}
			stream.reset();
		}
	}
}
