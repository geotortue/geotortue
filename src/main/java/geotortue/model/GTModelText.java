/**
 * 
 */
package geotortue.model;

import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;

public class GTModelText implements XMLCapabilities {

	private String text;

	public GTModelText(final String msg) {
		setText(msg);
	}
	

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getXMLTag() {
		return "GTModelText"; 
	}

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.setContent(getText());
		return e;
	}

	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		try {  
			setText(child.getContent());
		} catch (XMLException ex) {
			setText("");
			ex.printStackTrace();
		}
		return child;
	}
}