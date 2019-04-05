/**
 * 
 */
package fw.gui.params;

import javax.swing.JComponent;

import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;

public interface FWParameterI<T, C extends JComponent> extends XMLTagged {
	
	public abstract T getValue();
	
	public abstract void fetchValue(XMLReader child, T def);
	
	public abstract void storeValue(XMLWriter e);
	
	public C getComponent();
	
	public void addParamaterListener(FWParameterListener<T> l);
}
