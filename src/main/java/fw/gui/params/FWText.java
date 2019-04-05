package fw.gui.params;

import javax.swing.event.DocumentEvent;

import fw.gui.FWTextField;
import fw.gui.FWTextField.FWTextFieldListener;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;

public class FWText extends FWParameter<String, FWTextField> {

	private final FWTextField textField; 
	
	public FWText(String tag, String value, int cols) {
		super(tag, value);
		this.textField = new FWTextField(value, cols, new FWTextFieldListener() {
			@Override
			public void textChanged(String text, DocumentEvent e) {
				FWText.super.setValue(text);
			}
		});
	}
	
	@Override
	protected boolean setValue(String v) {
		boolean changed = super.setValue(v);
		if (changed)
			textField.setText(getValue());
		return changed;
	}

	@Override
	public void fetchValue(XMLReader child, String def) {
		setValue(child.getAttribute(getXMLTag(), def));
	}

	@Override
	public void storeValue(XMLWriter e) {
		e.setAttribute(getXMLTag(), getValue());
	}

	@Override 
	public FWTextField getComponent() {
		return textField;
	}




}