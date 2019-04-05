package fw.gui.params;

import java.awt.Color;

import fw.gui.FWColorBox;
import fw.gui.FWColorBox.FWColorChooserListener;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class FWColor extends FWParameter<Color, FWColorBox> {

	private final FWColorBox colorBox;
	
	public FWColor(String xmlTag, Color c) {
		super(xmlTag, c);
		this.colorBox = new FWColorBox(c, new FWColorChooserListener() {
			
			@Override
			public void colorSelected(Color c) {
				setValue(c);
			}
		});
	}
	
	public FWColorBox getComponent() {
		return colorBox;
	}
	
	@Override
	protected boolean setValue(Color v) {
		colorBox.setColor(v);
		return super.setValue(v);
	}

	@Override
	public void fetchValue(XMLReader e, Color def) {
		try {
			setValue(e.getAttributeAsColor(getXMLTag()));
		} catch (XMLException ex) {
			setValue(def);
		}
	}

	@Override
	public void storeValue(XMLWriter e) {
		e.setAttribute(getXMLTag(), getValue());
	}
}