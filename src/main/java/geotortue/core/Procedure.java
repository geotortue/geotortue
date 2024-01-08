package geotortue.core;

import org.nfunk.jep.addon.JEPException;

import fw.text.FWParsingTools;
import fw.text.FWParsingTools.ParsingException;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;
import geotortue.core.GTCommandProcessor.GTInterruptionException;
import geotortue.core.GTMessageFactory.GTTrouble;
import geotortue.core.ProcedureUpdater.ProtoProcedure;
import jep2.JEP2Exception;
import type.JObjectI;


public class Procedure implements XMLCapabilities {

	final static XMLTagged XML_TAG = XMLTagged.Factory.create("Procedure");
	
	private final SourceLocalization localization;
	private String key;
	private String[] argumentNames;
	private GTCommandBundles bundles;
	private GTException constructionException = null;
	private final GTCommandBundle header;
	
	public Procedure(KeywordManager keywordManager, SourceLocalization sourceLoc) throws GTException, ProcedureParsingException {
		this.localization = sourceLoc;
		
		SourceProvider provider = sourceLoc.getProvider();
		
		String text = sourceLoc.getRawText();
		int splitIdx = text.indexOf("\n");
		int len = sourceLoc.getLength();
		if (splitIdx<0)
			splitIdx = len;
		else {
			int blockCommentStart = text.substring(0, splitIdx).indexOf("/*");
			if (blockCommentStart>0) {
				int blockCommentEnd = text.indexOf("*/");
				if (blockCommentEnd>0)
					splitIdx = blockCommentEnd+2;
			}
		}
		
		int startOffset = sourceLoc.getOffset();		
		SourceLocalization loc = new SourceLocalization(provider, startOffset, splitIdx);
		GTCommandBundles headBundles = GTCommandBundle.parse(loc);
		if (headBundles.size() != 1) // should not occur
			System.err.println("Procedure.Procedure() "+headBundles);
		
		this.header = headBundles.firstElement();
//		if (!header.getKey().equals(KeywordManager.START_KEY.translate())) 
//			throw new ProcedureParsingException(); // should not occur
		
		int headTokenCount = header.getArgumentsCount();
		if (headTokenCount==0) 
			throw new ProcedureParsingException();

		// header : key
		loc = header.getLocalizationAt(1);
		this.key = keywordManager.testValidity(loc);
		
		// header : arguments
		this.argumentNames = new String[headTokenCount-1];
		for (int idx = 0; idx < headTokenCount-1; idx++) {
			loc = header.getLocalizationAt(idx+2);
			argumentNames[idx] = keywordManager.testValidity(loc);
		}
		
		// commands
		int clen = len-splitIdx-1;
		if (clen>0) {
			loc = new SourceLocalization(provider, startOffset+splitIdx+1, clen);
			try {
				this.bundles = GTCommandBundle.parse(loc);
			} catch (GTException ex) {
				constructionException = ex;
				this.bundles = new GTCommandBundles();
			}
		} else 
			this.bundles = new GTCommandBundles();
	}
	
	public Procedure(KeywordManager keywordManager, ProtoProcedure p) throws GTException, ProcedureParsingException {
		this(keywordManager, p.getLocalization());
	}
	
	public Procedure(ProtoProcedure p_, Procedure p) {
		this.localization = p_.getLocalization();
		this.key = p.key;
		this.argumentNames = p.argumentNames;
		this.bundles = p.bundles;
		this.constructionException = p.constructionException;
		this.header = p.header;
	}
	
	public String toString() {
		return "Procedure : "+getKey()+"\n"+bundles;
	}

	public String getKey() {
		return key;
	}
	
	public JObjectI<?> execute(final GTCommandBundle bundle, final GTProcessingContext context) 
			throws GTException, GTInterruptionException {
		if (constructionException != null) {
			throw constructionException;
		}

		int arity = argumentNames.length;
		int count = bundle.getArgumentsCount();

		if (count !=  arity) {
			throw new GTException(GTTrouble.GTJEP_PROCEDURE_ARITY, bundle, key, argumentNames.length + "");
		}
		
		JObjectI<?>[] values = new JObjectI<?>[arity];
		for (int idx = 0; idx < arity; idx++) {
			JObjectI<?> d =  context.getJObjectAt(bundle, idx + 1);
			values[idx] = d;
		}
		
		try {
			context.openLocalParser(argumentNames, values);
			return context.process(bundles.getCopy());
		} catch (JEP2Exception ex) {
			throw new GTException(ex, localization);
		} catch (JEPException ex) {
			throw new GTException(ex, localization);
		} finally {
			context.closeLocalParser();
		}
	}
	
	public String[] getLocalVariables() {
		return argumentNames;
	}

	public boolean hasSameKeyAs(final Procedure p) {
		if (p == null) {
			return false;
		}

		return key.equals(p.key);
	}

	public boolean isSimilarTo(final ProtoProcedure p) {
		return p.getLength() == localization.getLength();
	}

	public String getHtmlText(final KeywordManager keywordManager) {
		String text = localization.getRawText();
		return doGetHtmlText(keywordManager, text + "\nfin", argumentNames);
	}
	
	public static String getHtmlText(final KeywordManager keywordManager, final String command) {
		return doGetHtmlText(keywordManager, command);
	}
	
	private static String doGetHtmlText(final KeywordManager keywordManager, final String text, final String... localVars) {
		String html = "";
		
		String[] pieces = FWParsingTools.split(text, "\"");
		for (int idx = 0; idx < pieces.length; idx++) {
			if (idx%2==0)
				html += pieces[idx];
			else
				html += "<span class=\"string\">\"" + pieces[idx] + "\"</span>";
		}
		
		try {
			html = FWParsingTools.surroundTokens(html, localVars, "<span class=\"varloc\">", "</span>" );
			html = keywordManager.addHTMLTags(html);
		} catch (ParsingException ex) {
			ex.printStackTrace();
		}
		html = html.replace("\n", "\n<br>\n");
		html = html.replace(":=", "<b>:=</b>");
		return html;
	}
	
	public String getRawText(){ 
		return localization.getRawText();
	}
	

	/*
	 * XML
	 */
	@Override
	public String getXMLTag() {
		return XML_TAG.getXMLTag();
	}
	
	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		return e;
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.setContent(getRawText());
		return e;
	}
	
	public class ProcedureParsingException extends Exception {

		private static final long serialVersionUID = 3563922628904581000L;
	}
	
	private boolean hidden = false;

	public boolean isHidden() {
		return hidden;
	}

	public void hide() {
		hidden = true;
	}
}