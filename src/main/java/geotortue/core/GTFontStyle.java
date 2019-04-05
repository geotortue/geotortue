/**
 * 
 */
package geotortue.core;

import java.awt.Color;
import java.util.Vector;

import javax.swing.UIManager;

import fw.text.FWEnhancedDocument;
import fw.text.TextStyleWFontColorP;
import fw.xml.XMLTagged;

/**
 *
 */
public class GTFontStyle extends TextStyleWFontColorP {

	private static final long serialVersionUID = -2303659862016299691L;
	
	private static Vector<FWEnhancedDocument> docs = new Vector<FWEnhancedDocument>();
	
	public GTFontStyle(XMLTagged parent, String key, Color c) {
		super(parent, key, UIManager.getFont("FWFont"), c);
	}

	public void register(FWEnhancedDocument doc) {
		docs.add(doc);
	}

	public void unregister(FWEnhancedDocument doc) {
		docs.remove(doc);
	}
	
	public void setFontFamily(String f){
		super.setFontFamily(f);
		refresh();
	}
	
	public void setFontSize(int s){
		super.setFontSize(s);
		refresh();
	}
	
	public void refresh() {
		for (FWEnhancedDocument doc : docs)
			doc.refresh();
	}
	
}
