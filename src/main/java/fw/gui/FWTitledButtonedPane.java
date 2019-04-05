package fw.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;

import fw.app.Translator.TKey;
import fw.gui.layout.BasicLayoutAdapter;


public class FWTitledButtonedPane extends FWTitledPane {
	private static final long serialVersionUID = -4365097225491761431L;
	private int buttonW = 32;
	private int buttonH = headHeight;
	private final JButton button ;
	
	public FWTitledButtonedPane(TKey title, JComponent c, JButton b) {
		super(title, c);
		this.headRightInset += buttonW ;
		this.button = b;
		button.setText("");
		button.setFocusable(false);
		FWButton.removeBackground(button);
		button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
		this.head = new Head2();
		relayout();
	}

	protected class Head2 extends Head {
		private static final long serialVersionUID = 9045756884572643494L;

		protected Head2() {
			super();
			add(button);
			setLayout(new HeadLayout());
		}

		private class HeadLayout extends BasicLayoutAdapter {
			
			@Override
			public void layoutComponent(Component c, int idx) {
				if (idx==0)
					c.setBounds(parentW-buttonW-2, (parentH-buttonH)/2+1, buttonW, buttonH);
			}
		}
	}
}
