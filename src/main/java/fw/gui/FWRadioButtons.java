package fw.gui;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import fw.app.Translator.TKey;
import fw.text.TextStyle;


public class FWRadioButtons extends JPanel implements ItemListener {
	private static final long serialVersionUID = -2231242118023800508L;
	
	private final ButtonGroup group = new ButtonGroup();
	private final FWRadioButtonsListener listener;
	
	public FWRadioButtons(FWRadioButtonsListener l, FWRadioButtonKey... keys) {
		super(new GridLayout(0, 1));
		this.listener = l;
		for (FWRadioButtonKey key : keys) {
			 JRadioButton rb = new FWRadioButton(key);
			 rb.setActionCommand(key.getCode());
			 rb.addItemListener(this);
			 group.add(rb);
			 add(rb);
		}
	}

	
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			JRadioButton b = (JRadioButton) e.getItem();
			listener.selectionChanged(b.getActionCommand());
		}
		
	}
	
	public void setSelected(String key){
		Enumeration<AbstractButton> e = group.getElements();
		while (e.hasMoreElements()){
			AbstractButton button = e.nextElement();
			if (button.getActionCommand().equals(key)){
				button.setSelected(true);
				return;
			}
		}
	}

	public interface FWRadioButtonsListener {
		public abstract void selectionChanged(String key);
	}
	
	public static class FWRadioButtonKey extends TKey {

		private final String code;
		
		public FWRadioButtonKey(Class<?> c, String key) {
			super(c, key);
			this.code = key;
		}
		
		private String getCode() {
			return code;
		}
	}

	private class FWRadioButton extends JRadioButton implements FWAccessible {

		private static final long serialVersionUID = 1213143837465012710L;

		public FWRadioButton(FWRadioButtonKey key) {
			super(key.translate());
			FWAccessibilityManager.register(this);
		}

		@Override
		public void setFont(TextStyle s) {
			setFont(s.getFont());
		}
	}
}