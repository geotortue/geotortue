/**
 * 
 */
package fw.xml;

import java.awt.Color;

public class XMLWriter {

	private final XMLEntry entry;


	public XMLWriter(XMLTagged c) {
		this.entry = new XMLEntry(c);
	}
	
	public String getXMLCode(){
		return entry.getXMLCode();
	}

	public void put(XMLCapabilities c) {
		XMLWriter w = c.getXMLProperties();
		entry.getChildren().add(w.entry);
	}
	
	public XMLReader toReader() {
		return XMLEntry.getReader(entry);
	}

	/*
	 * Setters
	 */

	public void setContent(String str) {
		entry.setContent(str);
	}

	public void setAttribute(String key, String value) {
		entry.getAttributes().put(key, value);
	}

	public void setAttribute(String key, String[] value) {
		String str = "[";
		for (int idx = 0; idx < value.length; idx++)
			str+= (idx != value.length-1) ? 
					value[idx]+", " : value[idx];
					str+="]";
					entry.getAttributes().put(key, str);
	}

	public void setAttribute(String key, boolean b) {
		setAttribute(key, String.valueOf(b));
	}

	public void setAttribute(String key, int n) {
		setAttribute(key, String.valueOf(n));
	}

	public void setAttribute(String key, long n) {
		setAttribute(key, String.valueOf(n));
	}
	
	public void setAttribute(String key, double x) {
		setAttribute(key, String.valueOf(x));
	}

	public void setAttribute(String key, Color color) {
		entry.getAttributes().put(key, String.valueOf(color.getRGB()));
	}

	

}