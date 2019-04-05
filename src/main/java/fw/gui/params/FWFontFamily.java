/**
 * 
 */
package fw.gui.params;

import java.awt.GraphicsEnvironment;

import fw.gui.FWComboBox;
import fw.gui.FWComboBox.FWComboBoxListener;

public class FWFontFamily extends FWString {
	
	private final FWComboBox fontsCB;

	public FWFontFamily(String tag, String value) {
		super(tag, value);
		String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		this.fontsCB = new FWComboBox(fonts, getValue(), new FWComboBoxListener() {
			public void itemSelected(Object o) {
				setValue((String) o);
			}
		});
	}

	public FWComboBox getComponent() {
		fontsCB.getModel().setSelectedItem(getValue());
		return fontsCB;
	}

}
