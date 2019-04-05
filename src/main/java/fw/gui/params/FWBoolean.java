package fw.gui.params;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class FWBoolean extends FWParameter<Boolean, JCheckBox> {
	
	private final JCheckBox checkBox;

	public FWBoolean(String xmlTag, boolean v) {
		super(xmlTag, v);
		
		checkBox = new JCheckBox("", getValue());
		checkBox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (checkBox.isSelected() != getValue()) {
					setValue(checkBox.isSelected());
				}
			}
		});
	}
	
	protected boolean setValue(Boolean v) { 
		boolean changed = super.setValue(v);
		checkBox.setSelected(getValue());
		return changed;
	}
	
	public JCheckBox getComponent() {
		return checkBox;
	}
	
	public void fetchValue(XMLReader child, Boolean def) {
		setValue(child.getAttributeAsBoolean(getXMLTag(), def));
	}
	
	public void setEnabled(boolean b) {
		checkBox.setEnabled(b);
	}

	@Override
	public void storeValue(XMLWriter e) {
		e.setAttribute(getXMLTag(), getValue());
	}
}
