package fw.gui.params;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fw.gui.FWSpinner;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class FWAngle extends FWParameter<Double, JSpinner> {

	private final JSpinner spinner;
	private final static double PI_OVER_180 = Math.PI/180;
	
	public FWAngle(String xmlTag, double v, double min, double max) {
		super(xmlTag, v);
		spinner = new FWSpinner(getValue() / PI_OVER_180, min, max, 1);
		spinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				double v = ((SpinnerNumberModel) spinner.getModel()).getNumber().doubleValue() * PI_OVER_180;
				setValue(v);
			}
		});
	}
	
	public FWAngle(String xmlTag, double v) {
		this(xmlTag, v, -360, 360);
	}
	
	public JSpinner getComponent() {
		return spinner;
	}
	
	protected boolean setValue(Double v) {
		boolean changed = super.setValue(v);
		spinner.setValue(getValue() / PI_OVER_180);
		return changed;
	}
	
	public void storeValue(XMLWriter e) {
		e.setAttribute(getXMLTag(), getValue());
	}
	
	public void fetchValue(XMLReader child, Double def) {
		setValue(child.getAttributeAsDouble(getXMLTag(), def));
	}

	public void setEnabled(boolean b) {
		spinner.setEnabled(b);
	}
}