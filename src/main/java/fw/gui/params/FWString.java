/**
 * 
 */
package fw.gui.params;

import fw.gui.FWComboBox;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;

public abstract class FWString extends FWParameter<String, FWComboBox> {

	
	public FWString(String tag, String value) {
		super(tag, value);
	}

	@Override
	public void fetchValue(XMLReader child, String def) {
		setValue(child.getAttribute(getXMLTag(), def));
	}

	@Override
	public void storeValue(XMLWriter e) {
		e.setAttribute(getXMLTag(), getValue());
	}

	@Override @Deprecated
	public FWComboBox getComponent() {
		return null;
	}
}
