package fw.xml;

import java.util.Hashtable;
import java.util.Vector;

import fw.text.FWParsingTools;


public class XMLEntry {

	final static private String CONTENT_TAG = "content";
	final static private String OPENING_CONTENT_MARK = "<"+CONTENT_TAG+">";
	final static private String CLOSING_CONTENT_MARK = "</"+CONTENT_TAG+">";
	final private String tag;
	final private Vector<XMLEntry> children = new Vector<XMLEntry>();
	final private Hashtable<String, String> attributes = new Hashtable<String, String>();
	private String content = null;

	public final static XMLReader NULL_ENTRY = new XMLEntry("null").getReader();


	private XMLEntry(String t) {
		if (t.length()<1)
			System.err.println(new XMLException("Empty tag"));
		this.tag = t;
	}

	protected XMLEntry(XMLTagged c) {
		this(c.getXMLTag());
	}
	
	protected String tag() {
		return tag;
	}

	
	protected Vector<XMLEntry> getChildren() {
		return children;
	}

	protected Hashtable<String, String> getAttributes() {
		return attributes;
	}

	protected String getContent() {
		return content;
	}

	protected void setContent(String content) {
		this.content = content;
	}


	protected XMLReader getReader() {
		return new XMLReader(this);
	}

	protected static XMLReader parse(String text) throws XMLException {
		return XMLParser.parse(text).getReader();
	}
	
	private static String escapeXMLCharacters(String text) {
		String eText = text;
		eText = eText.replace("&", "&amp;");
		eText = eText.replace("<", "&lt;");
		eText = eText.replace(">", "&gt;");
		eText = eText.replace("\"", "&quot;");
		return eText;
	}
	
	protected static String retrieveXMLCharacters(String text) {
		String eText = text;
		eText = eText.replace("&lt;", "<");
		eText = eText.replace("&gt;", ">");
		eText = eText.replace("&quot;", "\"");
		eText = eText.replace("&amp;", "&");
		return eText;
	}

	private String getCode() {
		String code = "\n<"+tag;

		if (!attributes.isEmpty()){
			for (String key : attributes.keySet()){
				String value = attributes.get(key);
				value = escapeXMLCharacters(value);
				code+=" "+key+"=\""+value+"\"";
			}
		}

		if (content==null){
			if (children.isEmpty())
				code+="/>";
			else {
				code+=">";
				for (XMLEntry child  : children)
					code+=child.getCode().replace("\n", "\n\t");
				code+="\n</"+tag+">";
			}
		} else {
			String text = content.startsWith("<") ? 
					OPENING_CONTENT_MARK+"\n\t\t"
					+content.replace("\n", "\n\t\t")
					+"\n\t"+CLOSING_CONTENT_MARK : 
						escapeXMLCharacters(content.replace("\n", "\n\t"));
					code+=">\n\t"+text+"\n</"+tag+">";				
		}
		return code;
	}

	String getXMLCode(){
		String code="<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		code+=getCode();
		return code;
	}

	static XMLReader getReader(XMLEntry e) {
		XMLEntry root = new XMLEntry("root");
		root.children.add(e);
		return root.getReader();
	}

	/*
	 * PARSER
	 */

	private static class XMLParser {
		private static XMLEntry parse(String text) throws XMLException {
			text = FWParsingTools.removeHTMLComments(text);
			if (text==null)
				throw new XMLException("Cannot parse null text");
			XMLEntry root = new XMLEntry("root");
			root.children.addAll(parseChildren(text));
			return root;
		}

		private static Vector<XMLEntry> parseChildren(final String text) throws XMLException {
			Vector<XMLEntry> children = new Vector<XMLEntry>();
			int offset=0;
			while (offset<text.length() && offset>=0){

				int headBeginIndex = text.indexOf("<", offset);
				if (headBeginIndex<0)
					throw new XMLException("Error while parsing XML (malformed syntax) :\n"+text);
				headBeginIndex++;

				int headEndIndex = text.indexOf(">", headBeginIndex);
				if (headEndIndex<0)
					throw new XMLException("Error while parsing XML (malformed syntax) :\n"+text);

				boolean hasNoChild = text.charAt(headEndIndex-1)=='/';
				if (hasNoChild)
					headEndIndex--;

				int attIndex = text.indexOf(" ", headBeginIndex);
				if  (attIndex>headEndIndex)
					attIndex=-1;

				String tag;
				Hashtable<String, String> attributes = new Hashtable<String, String>();
				if (attIndex<0)
					tag=text.substring(headBeginIndex, headEndIndex);
				else { 
					tag=text.substring(headBeginIndex, attIndex);
					attributes = parseAttributes(text.substring(attIndex, headEndIndex));
				}

				if (FWParsingTools.containsDelimiter(tag))
					throw new XMLException("Error while parsing XML (invalid tag) : "+tag);

				final XMLEntry e = new XMLEntry(tag);
				if (hasNoChild){
					offset =  headEndIndex+1;
				} else {
					int contentBeginIdx = headEndIndex+1; 
					int contentEndIdx = text.indexOf("</"+tag+">", offset);
					if (contentEndIdx<0)
						throw new XMLException("Error while parsing XML (no closing mark for "+tag+") :\n"
								+text.substring(headBeginIndex));

					String content = text.substring(contentBeginIdx, contentEndIdx).trim();
					boolean isVerbatim = content.startsWith(OPENING_CONTENT_MARK);
					if (content.startsWith("<") && (!isVerbatim)) {
						e.children.addAll(parseChildren(content));
					} else {
						if (isVerbatim){
							int closingContentMarkIdx = content.lastIndexOf(CLOSING_CONTENT_MARK);
							if (closingContentMarkIdx<0)
								throw new XMLException("Missing closing content mark in : "+content);
							content = content.substring(OPENING_CONTENT_MARK.length(), closingContentMarkIdx);
							e.content = content.trim().replace("\t", "");
						} else
							e.content = retrieveXMLCharacters(content).replace("\t", "");
					}
					offset = contentEndIdx + tag.length() + 3;
				}

				e.attributes.putAll(attributes);
				children.add(e);
				offset = text.indexOf("<", offset);
			}
			return children;
		}

		private static Hashtable<String, String>  parseAttributes(String text) throws XMLException{
			Hashtable<String, String> attributes = new Hashtable<String, String>();
			int offset=0;
			while (offset>=0 && offset<text.length()){
				int splitIdx = text.indexOf("=", offset);
				if (splitIdx<0)
					break;

				String key = text.substring(offset, splitIdx).trim();

				int valueBeginIdx = text.indexOf("\"", splitIdx);
				if (valueBeginIdx<0)
					throw new XMLException("Error while parsing XML (invalid attributes) :\n"+text);
				valueBeginIdx++;
				int valueEndIdx = text.indexOf("\"", valueBeginIdx);
				if (valueEndIdx<0)
					throw new XMLException("Error while parsing XML (invalid attributes) :\n"+text);
				String value = text.substring(valueBeginIdx, valueEndIdx);
				value = retrieveXMLCharacters(value);

				attributes.put(key, value);
				offset=valueEndIdx+1;
			}
			return attributes;
		}
	}
}