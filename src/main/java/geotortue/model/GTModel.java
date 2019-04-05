/**
 * 
 */
package geotortue.model;

import javax.swing.text.Document;

import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;
import geotortue.core.GTCodeDocument;
import geotortue.core.KeywordManager;
import geotortue.gui.GTTextPane;
import geotortue.gui.HTMLTextPane;

public class GTModel implements XMLCapabilities {

	public static final XMLTagged XML_TAG = XMLTagged.Factory.create("GTModel");
	private final KeywordManager keywordManager;
	private GTModelText htmlEditor = new GTModelText("<h1>Titre</h1>\n<p>Énoncé</p>"
			+ "<p><a href=\"http://geotortue.free.fr\">GéoTortue</a></p>");
	private final HTMLTextPane htmlPane = new HTMLTextPane(htmlEditor.getText());
	private final GTTextPane commandTP;
	
	public GTModel(KeywordManager km) {
		this.keywordManager = km;
		
		GTCodeDocument doc = new GTCodeDocument(keywordManager);
		this.commandTP = new GTTextPane(doc);
		commandTP.showNumbers(false);
		commandTP.setDocument(doc);
		doc.refresh();
	}
	
	public GTModel(KeywordManager km, XMLReader e) {
		this(km);
		loadXMLProperties(e);
	}
	
	public GTModel(GTModel m) {
		this(m.keywordManager);
		commandTP.setText(m.getCommand());
		htmlEditor.setText(m.getHTMLCode());
	}
	
	public String getHTMLCode() {
		return htmlEditor.getText();
	}
	
	public void setText(String text) {
		htmlEditor.setText(text);
		htmlPane.setText(text);
	}
	
	/**
	 */
	public HTMLTextPane getHTMLPane() {
		Document document = htmlPane.getDocument();
		document.putProperty(Document.StreamDescriptionProperty, null);
		htmlPane.setText(htmlEditor.getText());
		return htmlPane;
	}

	public GTTextPane getCommandTextPane() {
		return commandTP;
	}
	
	public String getCommand() {
		return commandTP.getText();
	}
	
	@Override
	public String getXMLTag() {
		return XML_TAG.getXMLTag();
	}
	

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.setAttribute("command", commandTP.getText().trim());
		e.put(htmlEditor);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		commandTP.setText(child.getAttribute("command", ""));
		htmlEditor.loadXMLProperties(child);
		return child;
	}

	/**
	 */
	public String getTitle() {
		return getCommand();
	}

	@Override
	public String toString() {
		return super.toString()+" "+getCommand();
	}
}