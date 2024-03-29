package fw.text;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.TextAction;
import javax.swing.undo.UndoManager;

/**
 * Singleton to register done actions
 */
public abstract class FWUndoSupport {

	private static final Hashtable<JTextComponent, UndoManager> table = new Hashtable<>();
	
	private FWUndoSupport() {}

	public static void register(final JTextComponent jtc) {
		final Keymap keymap = jtc.getKeymap();
		keymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), UNDO_ACTION);
		keymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), REDO_ACTION);
		
		final UndoManager m = new UndoManager();
		m.setLimit(-1);
		table.put(jtc, m);
		jtc.getDocument().addUndoableEditListener(m);
	}
	
	public static final TextAction UNDO_ACTION = new TextAction("undo") {
		private static final long serialVersionUID = 7113904388581707309L;

		public void actionPerformed(ActionEvent e) {
			JTextComponent target = getTextComponent(e);
			final UndoManager m = table.get(target);
			if (m != null && m.canUndo()) {
				m.undo();
			}
		}
	};
	

	public static final TextAction REDO_ACTION = new TextAction("redo") {
		private static final long serialVersionUID = -5445003267281151718L;

		public void actionPerformed(ActionEvent e) {
			JTextComponent target = getTextComponent(e);
			UndoManager m = table.get(target);
			if (m != null && m.canRedo()) {
				m.redo();
			}
		}
	};
	
	static UndoManager getUndoManager(final JTextComponent jtc){
		return table.get(jtc);
	}
}