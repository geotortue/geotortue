package fw.gui.params;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fw.gui.FWSpinner;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class FWInteger extends FWParameter<Integer, JSpinner> {

	private final JSpinner spinner;
	
	public FWInteger(String xmlTag, int v, int min, int max, int step) {
		super(xmlTag, v);
		
		spinner = new FWSpinner(getValue().intValue(), min, max, step);
		spinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				int v = ((SpinnerNumberModel) spinner.getModel()).getNumber().intValue();
				setValue(v);
			}
		});
	}
	
	public FWInteger(String xmlTag, int v, int min, int max) {
		this(xmlTag, v, min, max, 1);
	}
	
	public JSpinner getComponent() {
		return spinner;
	}
	
	public void setEnabled(Boolean b) {
		spinner.setEnabled(b);
	}

	protected boolean setValue(Integer v) {
		boolean changed = super.setValue(v);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				spinner.setValue(getValue());
			}
		});
		return changed;
	}
	
	public void storeValue(XMLWriter e) {
		e.setAttribute(getXMLTag(), getValue());
	}
	
	
	public void fetchValue(XMLReader child, Integer def) {
		setValue(child.getAttributeAsInteger(getXMLTag(), def));
	}
}