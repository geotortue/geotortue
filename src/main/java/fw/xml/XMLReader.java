/**
 * 
 */
package fw.xml;

import java.awt.Color;

import fw.app.FWConsole;

public class XMLReader {
	private final XMLEntry entry;
	private String  errorMessage = null;

	public XMLReader(XMLEntry e) {
		this.entry = e;
	}
	
	public boolean hasError() {
		return (errorMessage!=null);
	}
	
	public XMLException getException() {
		if (errorMessage!=null)
			return new XMLException(errorMessage);
		return null;
	}

	public String getXMLCode(){
		return entry.getXMLCode();
	}
	
	public boolean hasChild(XMLTagged c) {
		for(XMLEntry child : entry.getChildren())
			if (child.tag().equals(c.getXMLTag()))
				return true;
		return false;
	}
	
	public XMLReader popChild(XMLTagged c) {
		for(XMLEntry child : entry.getChildren()){
			if (child.tag().equals(c.getXMLTag())) {
				entry.getChildren().remove(child);
				return child.getReader();
			}
		}
		errorMessage = "No Such XMLEntry <"+c.getXMLTag()+"> in <"+entry.tag()+">";
		FWConsole.printWarning(this, errorMessage);
		return XMLEntry.NULL_ENTRY;
	}

	public String getNextChildTag() {
		if (entry.getChildren().isEmpty())
			return null;
		return entry.getChildren().firstElement().tag();
		
	}

	public String getContent() throws XMLException {
		if (entry.getContent()==null)
			throw new XMLException("no content available in : " + entry.tag() +"\n"+entry.getXMLCode());
		return entry.getContent();
	}

	public String getAttribute(String key) throws XMLException{
		String value =  entry.getAttributes().get(key);
		if (value!=null)
			return XMLEntry.retrieveXMLCharacters(value);
		throw new XMLException("No Attribute \""+key+"\" in <"+entry.tag()+">");
	}

	public String getAttribute(String key, String def) {
		try {
			return getAttribute(key);
		} catch (XMLException ex) {
			FWConsole.printWarning(this, ex.getMessage());
		}
		return def;
	}


	public String[] getAttributeAsStringArray(String key) throws XMLException {
		String list = getAttribute(key);
		if (!list.startsWith("[") || !list.endsWith("]"))
			throw new XMLException(list+"isn't a String[]");
		if (list.equals("[]"))
			return new String[0];
		list = list.substring(1, list.length()-1);
		String[] tokens = list.split(",");
		for (int idx = 0; idx < tokens.length; idx++)
			tokens[idx]=tokens[idx].trim();
		return tokens;
	}

	public String[] getAttributeAsStringArray(String key, String[] def) {
		try {
			return getAttributeAsStringArray(key);
		} catch (XMLException ex) {
			FWConsole.printWarning(this, ex.getMessage());
		}
		return def;
	}

	public boolean getAttributeAsBoolean(String key) throws XMLException {
		return Boolean.valueOf(getAttribute(key));
	}

	public boolean getAttributeAsBoolean(String key, boolean def) {
		try {
			return getAttributeAsBoolean(key);
		} catch (XMLException ex) {
			FWConsole.printWarning(this, ex.getMessage());
		}
		return def;
	}

	public int getAttributeAsInteger(String key) throws XMLException {
		try {
			return Integer.valueOf(getAttribute(key));
		} catch (NumberFormatException ex) {
			return Double.valueOf(getAttribute(key)).intValue();
		}
	}

	public int getAttributeAsInteger(String key, int def) {
		try {
			return getAttributeAsInteger(key);
		} catch (XMLException ex) {
			FWConsole.printWarning(this, ex.getMessage());
		}
		return def;
	}
	
	public long getAttributeAsLong(String key) throws XMLException {
		return Long.valueOf(getAttribute(key));
	}

	public long getAttributeAsLong(String key, long def) {
		try {
			return getAttributeAsLong(key);
		} catch (NumberFormatException ex){
			ex.printStackTrace();
		} catch (XMLException ex) {
			FWConsole.printWarning(this, ex.getMessage());
		}
		return def;
	}

	public double getAttributeAsDouble(String key) throws XMLException {
		return Double.valueOf(getAttribute(key));
	}

	public double getAttributeAsDouble(String key, double def) {
		try {
			return getAttributeAsDouble(key);
		} catch (NumberFormatException ex){
			ex.printStackTrace();
		} catch (XMLException ex) {
			FWConsole.printWarning(this, ex.getMessage());
		}
		return def;
	}

	public Color getAttributeAsColor(String key) throws XMLException {
		return new Color(Integer.valueOf(getAttribute(key)));
	}

	public Color getAttributeAsColor(String key, Color def) {
		try {
			return getAttributeAsColor(key);
		} catch (NumberFormatException ex){
			ex.printStackTrace();
		} catch (XMLException ex) {
			FWConsole.printWarning(this, ex.getMessage());
		}
		return def;
	}

}