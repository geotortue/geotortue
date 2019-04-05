package geotortue.gui;

import java.awt.Window;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import fw.app.Translator.TKey;
import fw.gui.FWDialog;
import fw.gui.layout.VerticalFlowLayout;


public class GTDialog extends FWDialog {
	
	private static final long serialVersionUID = -430972992943999718L;

	public GTDialog(Window owner, TKey title, JPanel textPane, boolean addCancelButton){
		super(owner, title, new GTDecoratedPane(textPane), true, addCancelButton);
	}
	
	public static void show(Window owner, TKey title, String msg, boolean addCancelButton, JButton... extraButtons){
		new FWDialog(owner, title, new GTDecoratedPane(msg), true, addCancelButton, extraButtons).setVisible(true);
	}
	
	public static void show(Window owner, TKey key, String msg, final StringHandler handler){
		HTMLTextPane htmlPane = new HTMLTextPane(msg);
		final JTextField field = new JTextField();
		field.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "default");
		
		JPanel pane = VerticalFlowLayout.createPanel(htmlPane, field);
		
		GTDialog dial = new GTDialog(owner, key, pane, true) {

			private static final long serialVersionUID = -6798120510636762049L;

			@Override
			protected void validationPerformed() {
				handler.handle(field.getText());
				super.validationPerformed();
			}

			@Override
			protected void close() {
				super.close();
				handler.giveUp();
			}
		};
		field.requestFocusInWindow();
		dial.setVisible(true);
	}
	
	public static interface StringHandler {
		public void handle(String str);
		
		public void giveUp();
	}
}