package fw.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fw.app.FWAction;
import fw.app.FWToolKit;
import fw.app.Translator.TKey;
import fw.text.TextStyle;


public class FWButton extends JButton implements ActionListener, FWAccessible {
	private static final long serialVersionUID = -385404440511731768L;

	private final FWButtonListener listener;
	
	public FWButton(BKey key, FWButtonListener l){
		super(key.translate());
		this.listener = l;
		addActionListener(this);
		setToolTipText(key.tooltip.translate());
		FWAccessibilityManager.register(this);
	}
	
	public FWButton(final FWAction a) {
		super(a);
		this.listener = new FWButtonListener() {
			
			@Override
			public void actionPerformed(ActionEvent e, JButton source) {
				a.actionPerformed(e);
			}
		};
		FWAccessibilityManager.register(this);
	}

	public void actionPerformed(ActionEvent e) {
		listener.actionPerformed(e, this);
	}

	public interface FWButtonListener {
		public void actionPerformed(ActionEvent e, JButton source);
	}
	
	public static void removeBackground(final AbstractButton b) {
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.getModel().addChangeListener(new ChangeListener() {
	        @Override
	        public void stateChanged(ChangeEvent e) {
	            ButtonModel model = (ButtonModel) e.getSource();
	            boolean ir = model.isRollover();
	            b.setContentAreaFilled(ir);
	            b.setBorderPainted(ir);
	        }
	    });
	}
	
	public static JButton createIconButton(TKey tooltip, String iconName, final FWButtonListener listener) {
		final JButton b = new JButton();
		b.setAction(new AbstractAction() {
			private static final long serialVersionUID = 8793358345877464535L;

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.actionPerformed(e, b);
			}
		});
		b.setToolTipText(tooltip.translate());
		b.setIcon(FWToolKit.getIcon(iconName));
		return b;
	}
	
	public static class BKey extends TKey {
		private final TKey tooltip;
		
		public BKey(Class<?> c, String key) {
			super(c, key+".text");
			this.tooltip = new TKey(c, key+".tooltip");
		}
	}
	
	@Override
	public void setFont(TextStyle s) {
		setFont(s.getFont());
	}
}

