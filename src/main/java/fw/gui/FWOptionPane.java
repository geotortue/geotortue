package fw.gui;


import java.awt.Container;

import javax.swing.JOptionPane;

import fw.app.Translator.TKey;



public class FWOptionPane {
	
	public enum ANSWER {YES, NO, CANCEL};
	
	public static ANSWER showConfirmDialog(Container owner, OPTKey key, String... args) {
		int confirm = JOptionPane.showConfirmDialog(owner, 
				formatMessage(key, args), 
				key.title.translate(),
				JOptionPane.YES_NO_CANCEL_OPTION, 
				JOptionPane.QUESTION_MESSAGE);
		if (confirm==JOptionPane.YES_OPTION)
			return ANSWER.YES;
		if (confirm==JOptionPane.NO_OPTION)
			return ANSWER.NO;
		return ANSWER.CANCEL;
	}
	
	public static ANSWER showErrorDialog(Container owner, OPTKey key, String... args) {
		int confirm = JOptionPane.showConfirmDialog(owner, 
				formatMessage(key, args),
				key.title.translate(),
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.ERROR_MESSAGE);
		if (confirm==JOptionPane.YES_OPTION)
			return ANSWER.YES;
		return ANSWER.NO;
	}
	
	private static void showMessageDialog(Container owner, OPTKey key, int messageType, String... args) {
		JOptionPane.showMessageDialog(owner, 
				formatMessage(key, args),
				key.title.translate(),
				messageType);
	}
	
	public static void showInformationMessage(Container owner, OPTKey key, String... args) {
		showMessageDialog(owner, key, JOptionPane.INFORMATION_MESSAGE, args);
	}

	public static void showErrorMessage(Container owner, OPTKey key, String... args) {
		showMessageDialog(owner, key, JOptionPane.ERROR_MESSAGE, args);
	}
	
	public static class OPTKey extends TKey {
		private final TKey title;

		public OPTKey(Class<?> c, String key) {
			super(c, key+".msg");
			this.title = new TKey(c, key+".title");
		}
	}
	
	private static String formatMessage(OPTKey key, String... args) {
		return "<html>"+key.translate(args)+"</html>";
	}

}
