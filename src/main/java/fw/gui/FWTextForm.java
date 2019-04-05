package fw.gui;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import fw.app.Translator.TKey;
import fw.gui.layout.VerticalPairingLayout;
import fw.text.TextStyle;


public class FWTextForm extends JPanel implements FWAccessible {
	private static final long serialVersionUID = 3935097600883324353L;
	
	private JTextField[] textFields;

	public void setText(String str, int idx) {
		textFields[idx].setText(str);
	}

	public FWTextForm(int hgap, int vgap, int nCols, TKey... keys) {
		super(new VerticalPairingLayout(hgap, vgap));
		this.textFields = new JTextField[keys.length];
		
		for (int idx = 0; idx < keys.length; idx++) {
			add(new FWLabel(keys[idx], SwingConstants.RIGHT));
			textFields[idx]= new JTextField(nCols);
			add(textFields[idx]);
		}
		FWAccessibilityManager.register(this);	}
	
	public FWTextForm(int nCols, TKey... keys) {
		this(10, 10, nCols, keys);
	}
	
	public FWTextForm(TKey... keys) {
		this(30, keys);
	}

	public String[] getStrings() {
		String[] strings = new String[textFields.length];
		for (int idx = 0; idx < strings.length; idx++)
			strings[idx]=textFields[idx].getText();
		return strings;
	}
	
	public boolean requestFocusInWindow() {
		super.requestFocusInWindow();
		return textFields[0].requestFocusInWindow();
	}
	
	@Override
	public void setFont(TextStyle s) {
		for (JTextField tf : textFields) 
			tf.setFont(s.getFont());
	}
}