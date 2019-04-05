package fw.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import fw.app.Translator.TKey;
import fw.gui.FWButton;



public class FWAction extends AbstractAction {

	private static final long serialVersionUID = -5466628506553995217L;

	private final ActionListener actionListener;
	
	public FWAction(ActionKey key, ActionListener l){
		super();
		this.actionListener = l;
		putValue(ACTION_COMMAND_KEY, key.key);
		putValue(NAME, key.translate());
		putValue(SHORT_DESCRIPTION, key.tooltip.translate());
		String mnemo = key.mnemonic.translate();
		if (mnemo.length() == 1)
			putValue(MNEMONIC_KEY, (int) mnemo.charAt(0));
	}
	
	public FWAction(ActionKey key, int mask, int keyChar, ActionListener l){
		this(key, l);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyChar, mask));
	}
	
	public FWAction(ActionKey key, int mask, int keyChar, String iconFile, ActionListener l){
		this(key, mask, keyChar, l);
		Icon icon = FWToolKit.getIcon(iconFile);
		if (icon!=null)
			putValue(SMALL_ICON, icon);
	}

	public FWAction(ActionKey key, String iconFile, ActionListener l){
		this(key, l);
		Icon icon = FWToolKit.getIcon(iconFile);
		if (icon!=null)
			putValue(SMALL_ICON, icon);
	}
	
	// for use in FWAbstractApplication.getLoadOldFileAction()
	FWAction(String commandKey, AbstractAction a){
		this.actionListener = a;
		putValue(ACTION_COMMAND_KEY, commandKey);
		putValue(ACCELERATOR_KEY, a.getValue(ACCELERATOR_KEY));
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, a.getValue(DISPLAYED_MNEMONIC_INDEX_KEY));
		putValue(LARGE_ICON_KEY, a.getValue(LARGE_ICON_KEY));
		putValue(LONG_DESCRIPTION, a.getValue(LONG_DESCRIPTION));
		putValue(MNEMONIC_KEY, a.getValue(MNEMONIC_KEY));
		putValue(NAME, a.getValue(NAME));
		putValue(SELECTED_KEY, a.getValue(SELECTED_KEY));
		putValue(SHORT_DESCRIPTION, a.getValue(SHORT_DESCRIPTION));
		putValue(SMALL_ICON, a.getValue(SMALL_ICON));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		actionListener.actionPerformed(e);
	}
	
	public static class ActionKey extends TKey {

		private final TKey mnemonic, tooltip;
		private final String key;
		
		public ActionKey(Class<?> c, String key) {
			super(c, key+".action");
			this.key = key;
			this.mnemonic = new TKey(c, key+".mnemonic");
			this.tooltip = new TKey(c, key+".tooltip");
		}
	}

	/**
	 * @return
	 */
	public FWButton getButton() {
		return new FWButton(this);
	}
}