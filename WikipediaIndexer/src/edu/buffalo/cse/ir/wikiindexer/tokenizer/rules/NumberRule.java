package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

@RuleClass(className = RULENAMES.NUMBERS)
public class NumberRule implements TokenizerRule 
{
	@Override
	public void apply(TokenStream stream) throws TokenizerException 
	{
		if (stream != null && stream.hasNext())
		{
			TokenStream ts = new TokenStream((String)null);
			String tokens =stream.next().toString();
			int tempMnth = 0;
			int tempDay = 0;
			int tempHr = 0;
			int tempMin = 0;
			int tempSec = 0;
			boolean isSingleToken = true;
			String tempStr = "";
			
			if(tokens.contains(" "))
			{
				for(String str: tokens.trim().split("\\s+"))
				{
					ts.append(str);	//Single token with Spaces
				}
			}
			else
			{
				ts.merge(stream);//Multiple Token
				isSingleToken = false;
			}
			
			while(ts.hasNext())
			{
				tokens = ts.next();//Fetch the next value
				
				//Check if token contains a digit
				if(tokens.matches(".*\\d.*"))
				{
					if(tokens.length() == 8 && tokens.matches("\\d+"))	//Check For dates
					{
						//should be of the form YYYMMDD
						tempMnth = Integer.parseInt(tokens.substring(3, 5));
						if(tempMnth >=01 && tempMnth >=12)
						{
							tempDay = Integer.parseInt(tokens.substring(6));
							if(tempDay>=01 && tempDay<=31)
							{
								//don't remove the token.DO NOTHING
							}
						}
					}
					//else if (tokens.contains(":")&& (tokens.indexOf(":") != tokens.lastIndexOf(":"))) //Check for Time
					else if(tokens.matches("\\d\\d:\\d\\d:\\d\\d"))
					{
						String[] timeArray =tokens.split(":");
						tempHr = Integer.parseInt(timeArray[0]);
						tempMin = Integer.parseInt(timeArray[1]);
						tempSec = Integer.parseInt(timeArray[2]);
						if(tempHr >= 00 && tempHr <= 23)
						{
							if(tempMin>=00 && tempMin <=59)
							{
								if(tempSec>=00 && tempSec<=59)
								{
									//don't remove the token.DO NOTHING
								}
							}
						}
					}
					else if(tokens.replaceAll("(\\d+)([,])(\\d+)","$1$3").matches("\\d+"))	
					{
						//something like 75,00,908 .. or 137979 remove it
						ts.previous();
						ts.remove();
					}
					else if(tokens.contains("-"))
					{
						//dont remove it.. Do nothing
					}
					else
					{
						//NONE OF THE CONDITIONS ARE SATISFIED..REMOVE ALL THE DIGITS IN IT
						tokens = tokens.replaceAll("(\\d+)([\\.])(\\d+)","$1$3").replaceAll("\\d+","");
						ts.previous();
						//ts.set(tokens);
						if(tokens.trim().length()>0)
				        	ts.set(tokens.trim());
				        else
				        	ts.remove();
						ts.next();
						
					}	
				}//Does not contain a digit.. Do nothing
				stream.previous();
				stream.remove();
			}
			ts.reset();
			if(isSingleToken)
			{
				while(ts.hasNext())
				{
					tempStr = tempStr+ts.next()+" ";
				}
				stream.append(tempStr.trim());
			}
			else
			{
				stream.merge(ts);
			}
		}
		
	}
}

