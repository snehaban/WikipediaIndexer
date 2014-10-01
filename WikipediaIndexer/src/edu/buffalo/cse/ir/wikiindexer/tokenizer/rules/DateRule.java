package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

@RuleClass(className = RULENAMES.DATES)
public class DateRule implements TokenizerRule {

	@Override
	public void apply(TokenStream stream) throws TokenizerException {
		// TODO Auto-generated method stub
		
		if (stream != null && stream.hasNext())
		{
			TokenStream ts = new TokenStream((String)null);
			String tokens =stream.next().toString();
			boolean isSingleToken = true;
			String tempStr = "";
			ArrayList<String> months =new ArrayList<>(Arrays.asList("JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE","JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER"
													,"DECEMBER","JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","AUG","SEP","OCT","NOV","DEC"));
			
			String prevToken = "";
			String nextToken = "";
			String nextToNextToken ="";
			String date = "";
			String zeros = "";
			
			
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
				tokens = ts.next().replace("(", "").replace(")","");//Fetch the next value
				
				/**
				 * 2011-12 case
				 */
				if(tokens.replace(".", "").matches("\\d\\d\\d\\d.\\d\\d") || tokens.replace(".", "").matches("\\d\\d\\d\\d.\\d\\d\\d\\d"))
				{
					String anyPunct="";
					if(tokens.endsWith("."))
					{
						anyPunct=tokens.substring(tokens.length()-1);
						tokens = tokens.substring(0, tokens.length()-1);
					}
					String connector = tokens.substring(4,5);
					String startYr = tokens.substring(0,4);
					String endYr = tokens.substring(5);
						if(endYr.length()==2)
							endYr = startYr.substring(0, 2)+endYr;
					tokens = startYr+"0101"+connector+endYr+"0101"+anyPunct;
					
					ts.previous();
					ts.set(tokens);
					ts.next();
				}
				
				
				/**
				 * AM or PM time
				 */
				if((tokens.replace(".", "").toLowerCase().endsWith("am") ||tokens.replace(".", "").toLowerCase().endsWith("pm"))
						&& !(tokens.replace(".", "").toLowerCase().startsWith("am") || tokens.replace(".", "").toLowerCase().startsWith("pm")))
				{
					String anyPunct="";
					if(tokens.endsWith("."))
					{
						anyPunct=tokens.substring(tokens.length()-1);
						tokens = tokens.substring(0, tokens.length()-1);
					}
					String appendThis = tokens.substring(tokens.length()-2);
					tokens = tokens.substring(0,tokens.length()-2);
					ts.previous();
					ts.set(tokens);
					ts.next();
					ts.append(appendThis+anyPunct);
					
				}
				if(tokens.replace(".", "").equalsIgnoreCase("AM") || tokens.replace(".", "").equalsIgnoreCase("PM"))
				{
					String time = "";
					String anyPunct="";
					if(tokens.endsWith("."))
					{
						anyPunct=tokens.substring(tokens.length()-1);
						tokens = tokens.substring(0, tokens.length()-1);
					}
					ts.previous();
					time = ts.previous();
					
					if(time != null && !time.equals("") && time.matches("\\d+:\\d\\d"))
					{
						if(tokens.equalsIgnoreCase("PM"))
						{
							String[] timeArray = time.split(":");
							tokens = (Integer.parseInt(timeArray[0])+12)+":"+timeArray[1]+":00"+anyPunct;
						}
						else
						{
							String[] timeArray = time.split(":");
							if(timeArray[0].matches("\\d"))
								tokens = "0"+timeArray[0]+":"+timeArray[1]+":00"+anyPunct;
							else
								tokens = timeArray[0]+":"+timeArray[1]+":00"+anyPunct;
						}
							
					}
					ts.remove();
					ts.set(tokens);
					ts.next();
					
				}
				
				/**
				 * AD or BC
				 */
				if(tokens.replace(".", "").equalsIgnoreCase("AD") || tokens.replace(".", "").equalsIgnoreCase("BC"))
				{
					String anyPunct ="";
					if(tokens.endsWith("."))
					{
						anyPunct=tokens.substring(tokens.length()-1);
						tokens = tokens.substring(0, tokens.length()-1);
					}
					ts.previous();
					String year = ts.previous();
					if(year != null)
					{
						if(tokens.equalsIgnoreCase("AD"))
						{
							for(int i =0 ; i < 4-year.length() ; i++)
								{ zeros = zeros + "0";}
							tokens = zeros+year+"0101"+anyPunct;
						}
						else
						{
							for(int i =0 ; i < 4-year.length() ; i++)
								{ zeros = zeros + "0";}
							tokens = "-"+zeros+year+"0101"+anyPunct;
						}
					}
					else
					{
						//Don'tdo anything
					}
					//Now Properly Set the token in Token Stream
					ts.remove();
					ts.set(tokens);
					ts.next();
					
				}
				if(months.contains(tokens.toUpperCase()))
				{
					/**
					 * Check previous and next token
					 * if previous is not a number.. check next two tokens.. they should be numbers
					 * Pass them to Date formatter and replace
					 */
					ts.previous();//ts.previous();
					prevToken = ts.previous();
					
					if(prevToken!=null && !prevToken.equals("") && prevToken.matches("\\d+"))
					{
						ts.next();ts.next();
						
						if(ts.hasNext())
							nextToken = ts.next();
						else
						{	
							//NEXT TOKEN is not present .. only month and previous term.
							// check if previous token is year .. 4 digit.. Yes .. then take month as default.
							if (prevToken.matches("\\d\\d\\d\\d"))
							{
								date = formatDate(prevToken+" "+tokens+" "+"01");
								if(date.substring(date.length()-1)!="2")
								{
								tokens = date.replaceAll("-", "").substring(0, 8);
								
								//Properly set it into token stream.
								ts.previous();ts.previous();
								ts.remove();
								ts.set(tokens);
								ts.next();
								}
								else
								{
									//Invalid format
								}
							}
							else
							{
								//if prev token is not YYYY then
								//check if more than 31 .. take as year else take as date verifying the month
								if(Integer.parseInt(prevToken) > 31)
								{
									//take as year
									date = formatDate("19"+prevToken+" "+tokens+" 01");
									if(date.substring(date.length()-1)!="2")
									{
									tokens = date.replaceAll("-", "").substring(0, 8);
									
									//properly assign it to token.
									ts.previous();ts.previous();
									ts.remove();
									ts.set(tokens);
									ts.next();
									}
									else
									{
									
									}
								}
								else
								{
									//take as date check for month
									date = formatDate("1900 "+prevToken+" "+ tokens);
									if(date.substring(date.length()-1)!="2")
									{
									tokens = date.replaceAll("-", "").substring(0, 8);
									
									//properly assign it to token.
									ts.previous();ts.previous();
									ts.remove();
									ts.set(tokens);
									ts.next();
									}
									else
									{
										
									}
									
								}
							}
							
						}//Prev token is a digit Next token Not present
						
						//Prev token is a digit Next token present
						if(!nextToken.equals("") && nextToken.matches("\\d+"))
						{
							String monthPunct= "";
							
							if(nextToken.matches("\\d+[,|\\.]*"))
							{
								int indexOfComma= nextToken.indexOf(",");
								int indexOfPeriod= nextToken.indexOf(".");
								
								if((indexOfComma == -1 && indexOfPeriod!=-1) || (indexOfPeriod == -1 && indexOfComma!=-1))
								{
									monthPunct = nextToken.substring(nextToken.length()-1);
									nextToken = nextToken.substring(0, nextToken.length()-1);
								}
								else
								{
									if(indexOfComma == -1 && indexOfPeriod == -1)
									{
										//Dont doanything
									}
									else if(indexOfComma<indexOfPeriod)
									{
										monthPunct = nextToken.substring(indexOfComma);
										nextToken = nextToken.substring(0, indexOfComma);
									}
									else
									{
										monthPunct = nextToken.substring(indexOfPeriod);
										nextToken = nextToken.substring(0,indexOfPeriod);
									}
								}
							}
							date = formatDate(prevToken+" "+tokens+" "+nextToken);
							if(date.substring(date.length()-1)!="2")
							{
							tokens = date.replaceAll("-", "").substring(0, 8)+monthPunct;
							
							//Properly set it into token stream.
							ts.previous();ts.previous();ts.previous();
							ts.remove();ts.remove();
							ts.set(tokens);
							ts.next();
							}
							else
							{
								
							}
						}
						else		// NEXT token NOT A DIGIT .. CREATE DATE FROM PREV AND CURRENT TOKEN
						{
							if (prevToken.matches("\\d\\d\\d\\d"))
							{
								date = formatDate(prevToken+" "+tokens+" "+"01");
								if(date.substring(date.length()-1)!="2")
								{
								tokens = date.replaceAll("-", "").substring(0, 8);
								
								//Properly set it into token stream.
								ts.previous();ts.previous();ts.previous();
								ts.remove();
								ts.set(tokens);
								ts.next();
								}
								else
								{
									
								}
							}
							else
							{
								//if prev token is not YYYY then
								//check if more than 31 .. take as year else take as date verifying the month
								if(Integer.parseInt(prevToken) > 31)
								{
									//take as year
									date = formatDate("19"+prevToken+" "+tokens+" 01");
									if(date.substring(date.length()-1)!="2")
									{
									tokens = date.replaceAll("-", "").substring(0, 8);
									
									//properly assign it to token.
									ts.previous();ts.previous();
									ts.remove();
									ts.set(tokens);
									ts.next();
									}
									else
									{
										
									}
								}
								else
								{
									//take as date check for month
									date = formatDate("1900 "+prevToken+" "+ tokens);
									if(date.substring(date.length()-1)!="2")
									{
									tokens = date.replaceAll("-", "").substring(0, 8);
									
									//Properly set it into token stream.
									ts.previous();ts.previous();ts.previous();
									ts.remove();
									ts.set(tokens);
									ts.next();
									}
									else
									{
										
									}
									
									
								}
							}
						}//next token present..but not a digit
					}
				
					else		// Previous element is not a digit. Read Next two
					{
						//read the next two
						if(prevToken!=null)
							ts.next();
						ts.next();
						String monthPunct= "";
						String yearPunct= "";
						
						if(ts.hasNext())
						{
							nextToken = ts.next().trim();
							if(nextToken.matches("\\d+[,|\\.]*"))
							{	
									int indexOfComma= nextToken.indexOf(",");
									int indexOfPeriod= nextToken.indexOf(".");
									
									if((indexOfComma == -1 && indexOfPeriod!=-1) || (indexOfPeriod == -1 && indexOfComma!=-1))
									{
										monthPunct = nextToken.substring(nextToken.length()-1);
										nextToken = nextToken.substring(0, nextToken.length()-1);
									}
									else
									{
										if(indexOfComma == -1 && indexOfPeriod == -1)
										{
											//Dont doanything
										}
										else if(indexOfComma<indexOfPeriod)
										{
											monthPunct = nextToken.substring(indexOfComma);
											nextToken = nextToken.substring(0, indexOfComma);
										}
										else
										{
											monthPunct = nextToken.substring(indexOfPeriod);
											nextToken = nextToken.substring(0,indexOfPeriod);
										}
									}
								if(ts.hasNext())
								{
									nextToNextToken = ts.next();
									if(nextToken !=null)
										//System.out.println(nextToken);
									if(nextToNextToken.matches("\\d+[,|\\.]*"))
									{
										/*if(nextToNextToken.matches("\\d+[,|\\.]"))
										{*/
											/*yearPunct = nextToNextToken.substring(nextToNextToken.length()-1);
											nextToNextToken = nextToNextToken.substring(0, nextToNextToken.length()-1);
											*/
											indexOfComma= nextToNextToken.indexOf(",");
											indexOfPeriod= nextToNextToken.indexOf(".");
											
											if((indexOfComma == -1 && indexOfPeriod!=-1) || (indexOfPeriod == -1 && indexOfComma!=-1))
											{
												yearPunct = nextToNextToken.substring(nextToNextToken.length()-1);
												nextToNextToken = nextToNextToken.substring(0, nextToNextToken.length()-1);
											}
											else
											{
												if(indexOfComma == -1 && indexOfPeriod == -1)
												{
													//Dont doanything
												}
												else if(indexOfComma<indexOfPeriod)
												{
													yearPunct = nextToken.substring(indexOfComma);
													nextToNextToken = nextToNextToken.substring(0, indexOfComma);
												}
												else
												{
													yearPunct = nextToken.substring(indexOfPeriod);
													nextToNextToken = nextToNextToken.substring(indexOfPeriod);
												}
											}
										//}
										
										//MONTH NEXT AND NEXTTONEXT ARE PRESENT
										date = formatDate(tokens+" "+nextToken+" "+nextToNextToken);
										if(date.substring(date.length()-1)!="2")
										{
										tokens = date.replaceAll("-", "").substring(0, 8)+yearPunct;
										//Now properly set token into token stream
										ts.previous();ts.previous();ts.previous();
										ts.remove();ts.remove();
										ts.set(tokens);
										ts.next();
										}
										else
										{
											
										}
									}
									else
									{
										//only month an a number are present .. it should be four digit taken as year ..and take date as default.
										if(nextToken.matches("\\d\\d\\d\\d"))
										{
											//append month punct.
											date = formatDate("01"+" "+tokens+" "+nextToken);
											if(date.substring(date.length()-1)!="2")
											{
											tokens = date.replaceAll("-", "").substring(0, 8)+monthPunct;
											
											//Now properly set token into token stream
											ts.previous();ts.previous();
											ts.remove();
											ts.set(tokens);
											ts.next();
											}
											else
											{
											
											}
										}
										else
										{
											if(Integer.parseInt(nextToken) > 31)
											{
												//take as year
												date = formatDate("19"+nextToken+" "+tokens+" 01");
												tokens = date.replaceAll("-", "").substring(0, 8)+monthPunct;
												
												//properly assign it to token.
												ts.previous();
												ts.previous();
												ts.remove();
												ts.set(tokens);
												ts.next();
											}
											else
											{
												//take as date check for month
												date = formatDate("1900 "+tokens+" "+nextToken);
												if(date.substring(date.length()-1)!="2")
												{
												tokens = date.replaceAll("-", "").substring(0, 8)+monthPunct;
												
												//properly assign it to token.
												ts.previous();ts.previous();ts.previous();
												ts.remove();
												ts.set(tokens);
												ts.next();
												}
												else
												{
												
												}
												
											}
										}
									} // Month and Next term as digit are present
								}
								else
								{
									
								}
							}
							else
							{
								//Next element is not a digit
								//Only Month is present
								date = formatDate("1900 "+tokens+" 01");
								if(date.substring(date.length()-1)!="2")
								{
								tokens = date.replaceAll("-", "").substring(0, 8);
								
								//properly assign it to token.
								ts.previous();ts.previous();
								ts.set(tokens);
								ts.next();
								}
								else
								{
									
								}
							
							}
							
						}
						else
						{
							//Prev token is not a number and no more tokens are present
							date = formatDate("1900"+" "+tokens+" 01");
							if(date.substring(date.length()-1)!="2")
							{
							tokens = date.replaceAll("-", "").substring(0, 8);
							
							//properly assign it to token.
							ts.previous();
							ts.set(tokens);
							ts.next();
							}else
							{
								
							}
						}
					
					}
				}
				//Does not contain month
				if(tokens.matches("\\d\\d\\d\\d"))
				{
					//check if next or prev token is a month.
					ts.previous();
					if(ts.hasPrevious())
					{
						if(!months.contains(ts.previous().toUpperCase()))
						{
							ts.next();ts.next();
							if(ts.hasNext())
							{
								if(!months.contains(ts.next().toString()))
								{
									//NO ..
									tokens = tokens+"0101";
									
									ts.previous();ts.previous();
									ts.set(tokens);
									ts.next();
								}
								//Yes .. dont do any thing .. it will be handled
							}
							else
							{
								//NO ..
								tokens = tokens+"0101";
								ts.previous();
								ts.set(tokens);
								ts.next();
							}
						}
					}
					//yes -- dont do anything.. it will be handled later 
				}
				stream.previous();
				stream.remove();//emptying stream .. so that new ts can be assigned to it.
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
	
	public String formatDate(String str)
	{
		Format formatter;
		String returnCode = "1";
		try
		{
			Date date = new Date(str);			
			formatter = new SimpleDateFormat("yyyy-MM-dd");
			str = formatter.format(date)+returnCode;
		}
		catch (Exception e)
		{
			returnCode = "2";
			//System.out.println("Invalid date Format    :"+str);
			return str+returnCode;
		}
			return str;
	}
	
}
