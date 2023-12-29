package fw.gui;


import java.awt.Container;

import javax.swing.JOptionPane;

import fw.app.Translator.TKey;


/**
 * Singleton gathering static functions about configuration of IHM.
 */
public final class FWOptionPane {
	
	public enum ANSWER {YES, NO, CANCEL}

	public static final class OPTKey extends TKey {
		private final TKey title;

		public OPTKey(Class<?> c, String key) {
			super(c, key + ".msg");
			this.title = new TKey(c, key + ".title");
		}
	}
	
	private FWOptionPane() {}
	
	public static ANSWER showConfirmDialog(final Container owner, final OPTKey key, final String... args) {
		final int confirm = JOptionPane.showConfirmDialog(owner, 
				formatMessage(key, args), 
				key.title.translate(),
				JOptionPane.YES_NO_CANCEL_OPTION, 
				JOptionPane.QUESTION_MESSAGE);
		switch (confirm) {
		case JOptionPane.YES_OPTION:
			return ANSWER.YES;
		case JOptionPane.NO_OPTION :
			return ANSWER.NO;
		default:
			return ANSWER.CANCEL;
		}
	}
	
	public static ANSWER showErrorDialog(final Container owner, final OPTKey key, final String... args) {
		final int confirm = JOptionPane.showConfirmDialog(owner, 
				formatMessage(key, args),
				key.title.translate(),
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.ERROR_MESSAGE);
		return (confirm == JOptionPane.YES_OPTION) ? ANSWER.YES : ANSWER.NO;
	}
	
	private static void showMessageDialog(final Container owner, final OPTKey key, final int messageType, final String... args) {
		JOptionPane.showMessageDialog(owner, 
				formatMessage(key, args),
				key.title.translate(),
				messageType);
	}
	
	public static void showInformationMessage(final Container owner, final OPTKey key, final String... args) {
		showMessageDialog(owner, key, JOptionPane.INFORMATION_MESSAGE, args);
	}

	public static void showErrorMessage(final Container owner, final OPTKey key, final String... args) {
		showMessageDialog(owner, key, JOptionPane.ERROR_MESSAGE, args);
	}
	
	private static String formatMessage(final OPTKey key, final String... args) {
		return "<html>" + key.translate(args) + "</html>";
	}

}
