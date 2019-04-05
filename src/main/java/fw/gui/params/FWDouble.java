package fw.gui.params;

import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fw.gui.FWSpinner;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class FWDouble extends FWParameter<Double, JSpinner> {

	private final JSpinner spinner;
	
	public FWDouble(String xmlTag, double v, double min, double max, double step) {
		super(xmlTag, v);
		
		spinner = new FWSpinner(v, min, max, step);
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double v = ((SpinnerNumberModel) spinner.getModel()).getNumber().doubleValue(); 
				setValue(v);
			}
		});
		spinner.setPreferredSize(new Dimension(60, 20));
	}
	
	public JSpinner getComponent() {
		return spinner;
	}


	protected boolean setValue(Double v) {
		boolean changed = super.setValue(v);
		spinner.setValue(getValue());
		return changed;
	}
	
	public void storeValue(XMLWriter e) {
		e.setAttribute(getXMLTag(), getValue());
	}
	
	public void fetchValue(XMLReader child, Double def) {
		setValue(child.getAttributeAsDouble(getXMLTag(), def));
	}
}