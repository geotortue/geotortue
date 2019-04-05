/**
 * 
 */
package fw.gui;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import fw.text.TextStyle;

/**
 * @author Salvatore Tummarello
 *
 */
public class FWSpinner extends JSpinner implements FWAccessible {

	private static final long serialVersionUID = 3536892516902747877L;

	public FWSpinner(Number value, Comparable<?> min, Comparable<?> max, Number step) {
		super(new SpinnerNumberModel(value, min, max, step));
		FWAccessibilityManager.register(this);
	}

	@Override
	public void setFont(TextStyle s) {
		getEditor().getComponent(0).setFont(s.getFont());
	}

}
