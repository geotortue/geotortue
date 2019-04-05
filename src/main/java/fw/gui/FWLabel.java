package fw.gui;

import java.awt.Dimension;

import javax.swing.JLabel;

import fw.app.Translator.TKey;
import fw.text.TextStyle;


public class FWLabel extends JLabel implements FWAccessible {
	private static final long serialVersionUID = 4585615291082576659L;

	public FWLabel(TKey key){
		this(key, LEFT);
		FWAccessibilityManager.register(this);
		}

	public FWLabel(TKey key, int horizontalAlignment) {
		super("<html>"+key.translate()+"</html>", horizontalAlignment);
		FWAccessibilityManager.register(this);
	}
	
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		return new Dimension(d.width+12, d.height);
	}
	
	@Override
	public void setFont(TextStyle s) {
		setFont(s.getFont());
	}
}