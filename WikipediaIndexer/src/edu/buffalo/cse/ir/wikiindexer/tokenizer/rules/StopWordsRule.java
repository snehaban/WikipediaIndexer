package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.util.ArrayList;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

@RuleClass(className = RULENAMES.STOPWORDS)
public class StopWordsRule implements TokenizerRule 
{		
	public void apply(TokenStream stream) throws TokenizerException 
	{
		if (stream != null)
		{
			String token;
			while (stream.hasNext()) 
			{ 
				token = stream.next(); //read next token
				String sw = StopWords.isStopWord(token);
				if (sw == null) 
				{										
					stream.previous();
					stream.remove();															
				}
				else
				{
					stream.previous();
					stream.set(sw);
					stream.next();
				}
			}			
			stream.reset();
		}
	}
	
	static class StopWords
	{
		static ArrayList<String> stopWordsList = new ArrayList<String>();
		
		static final String words = 
				"a able	about	above	according "+
				"accordingly	across	actually	after	afterwards "+
				"again	against	ain't	all	allow "+
				"allows	almost	alone	along	already "+
				"also	although	always	am	among "+
				"amongst	an	and	another	any "+
				"anybody	anyhow	anyone	anything	anyway "+
				"anyways	anywhere	apart	appear	appreciate "+
				"appropriate	are	aren't	around	as "+
				"aside	ask	asking	associated	at "+
				"available	away	awfully b	be	became "+
				"because	become	becomes	becoming	been "+
				"before	beforehand	behind	being	believe "+
				"below	beside	besides	best	better "+
				"between	beyond	both	brief	but "+
				"by	c'mon	c	came	can "+
				"can't	cannot	cant	cause	causes "+
				"certain	certainly	changes	clearly	co "+
				"com	come	comes	concerning	consequently "+
				"consider	considering	contain	containing	contains "+
				"corresponding	could	couldn't	course	currently "+
				"definitely	described d	despite	did	didn't "+
				"different	do	does	doesn't	doing "+
				"don't	done	down	downwards	during "+
				"each e	edu	eg	eight	either "+
				"else	elsewhere	enough	entirely	especially "+
				"et	etc	even	ever	every "+
				"everybody	everyone	everything	everywhere	ex "+
				"exactly	example	except f	far	few "+
				"fifth	first	five	followed	following "+
				"follows	for	former	formerly	forth "+
				"four	from	further	furthermore	get "+
				"gets g	getting	given	gives	go "+
				"goes	going	gone	got	gotten "+
				"greetings h had	hadn't	happens	hardly "+
				"has	hasn't	have	haven't	having "+
				"he	he's	hello	help	hence "+
				"her	here	here's	hereafter	hereby "+
				"herein	hereupon	hers	herself	hi "+
				"him	himself	his	hither	hopefully "+
				"how	howbeit	however i i'd	i'll "+
				"i'm	i've	ie	if	ignored "+
				"immediate	in	inasmuch	inc	indeed "+
				"indicate	indicated	indicates	inner	insofar "+
				"instead	into	inward	is	isn't "+
				"it	it'd	it'll	it's	its "+
				"itself	just j k	keep	keeps	kept "+
				"know	known	knows l	last	lately "+
				"later	latter	latterly	least	less "+
				"lest	let	let's	like	liked "+
				"likely	little	look	looking	looks "+
				"ltd m	mainly	many	may	maybe "+
				"me	mean	meanwhile	merely	might "+
				"more	moreover	most	mostly	much "+
				"must	my	myself n	name	namely "+
				"nd	near	nearly	necessary	need "+
				"needs	neither	never	nevertheless	new "+
				"next	nine	no	nobody	non "+
				"none	noone	nor	normally	not "+
				"nothing	novel	now	nowhere o	obviously "+
				"of	off	often	oh	ok "+
				"okay	old	on	once	one "+
				"ones	only	onto	or	other "+
				"others	otherwise	ought	our	ours "+
				"ourselves	out	outside	over	overall "+
				"own p	particular	particularly	per	perhaps "+
				"placed	please	plus	possible	presumably "+
				"probably	provides q	que	quite	qv "+
				"r rather	rd	re	really	reasonably "+
				"regarding	regardless	regards	relatively	respectively "+
				"right	said s	same	saw	say "+
				"saying	says	second	secondly	see "+
				"seeing	seem	seemed	seeming	seems "+
				"seen	self	selves	sensible	sent "+
				"serious	seriously	seven	several	shall "+
				"she	should	shouldn't	since	six "+
				"so	some	somebody	somehow	someone "+
				"something	sometime	sometimes	somewhat	somewhere "+
				"soon	sorry	specified	specify	specifying "+
				"still	sub	such	sup	sure t "+
				"t's	take	taken	tell	tends "+
				"th	than	thank	thanks	thanx "+
				"that	that's	thats	the	their "+
				"theirs	them	themselves	then	thence "+
				"there	there's	thereafter	thereby	therefore "+
				"therein	theres	thereupon	these	they "+
				"they'd	they'll	they're	they've	think "+
				"third	this	thorough	thoroughly	those "+
				"though	three	through	throughout	thru "+
				"thus	to	together	too	took "+
				"toward	towards	tried	tries	truly "+
				"try	trying	twice	two	un u "+
				"under	unfortunately	unless	unlikely	until "+
				"unto	up	upon	us	use "+
				"used	useful	uses	using	usually "+
				"value v	various	very	via	viz "+
				"vs	want w	wants	was	wasn't "+
				"way	we	we'd	we'll	we're "+
				"we've	welcome	well	went	were "+
				"weren't	what	what's	whatever	when "+
				"whence	whenever	where	where's	whereafter "+
				"whereas	whereby	wherein	whereupon	wherever "+
				"whether	which	while	whither	who "+
				"who's	whoever	whole	whom	whose "+
				"will	willing	wish	with "+
				"within	without	won't	wonder	would "+
				"wouldn't x	yes	yet	you	you'd y "+
				"you'll	you're	you've	your	yours "+
				"yourself	yourselves	zero z";
		
		static
		{
			for(String str: words.split("\\s+")) 
			{
				if(str != null && str.trim() != "") 
				{					
					stopWordsList.add(str);
				}
			}			
		}
		
		public static String isStopWord(String token)
		{			
			if(token==null || token.trim()=="")
				return null;
			
			String[] list = token.trim().toLowerCase().split("\\s+");
			
			for(String str: list) 
			{
				if(stopWordsList.contains(str))
				{
					token = token.replace(token, "");					
				}
			}
			token = token.replaceAll("\\s+", " ").trim();
			if(token!=null && token.length()>0)
				return token;
			else
				return null;
		}
	}
}

