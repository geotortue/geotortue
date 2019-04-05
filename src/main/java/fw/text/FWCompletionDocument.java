package fw.text;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;


public class FWCompletionDocument extends FWEnhancedDocument {
	/**
	 * @param style
	 */
	public FWCompletionDocument(TextStyle style) {
		super(style);
	}

	private static final long serialVersionUID = 4634526308076952807L;
	
	private Set<String> completionKeys = Collections.synchronizedSet(new HashSet<String>());
	
	public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
		super.insertString(offset, str, a);
		if (str.length()==1)
			tryCompletion(offset);
	}
	
	public void tryCompletion(int offset) {
		String token=FWParsingTools.getToken(getText(), offset);
		Vector<String> hits = match(token);
		switch (hits.size()) {
			case 0:
				JTextPane pane = getTextPane();
				if (pane != null)
					pane.requestFocusInWindow();
				return;
			default:
				showPopup(offset, hits);
				break;
		}
	}
	
	private int matchLength = 3; 
	
	private Vector<String> match(String token){
		Vector<String> hits = new Vector<String>();
		if (token.length()< matchLength || token.startsWith("_"))
			return hits;
		synchronized (completionKeys) {
			for (String word : completionKeys) {
				if (word.startsWith(token) && !word.equals(token))
					hits.add(word);
			}
			return hits;
		}
	}
	
	protected void commitCompletion(int offset, String word) {
		String content = getText();
		String token=FWParsingTools.getToken(content, offset);

		int endIdx = FWParsingTools.getEndOfToken(content, offset);

		try {
			insertString(endIdx, word.substring(token.length()), null);
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
		getTextPane().requestFocusInWindow();
	}
	
	private void showPopup(int offset, Vector<String> hits) {
		final JTextPane textPane = getTextPane();
		final JList<String> list = new JList<String>(hits);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);
		list.setFont(getDefaultStyle().deriveFont(0, -2));

		int x = 0, y = 0, rH = 0, caretY = 0;
		try {
			Rectangle r = textPane.modelToView(textPane.getCaretPosition());
			Point p = textPane.getLocationOnScreen();
			x = p.x+r.x;
			y = p.y+r.y+r.height;
			rH = r.height;
			caretY = r.y;
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}

		PopupFactory factory = PopupFactory.getSharedInstance();
		
		// adjust if popup is too low
		int h = textPane.getParent().getHeight();
		int listH = list.getPreferredSize().height;
		
		if (caretY+listH > h-rH)
			y -= listH + rH;

		final Popup popup = factory.getPopup(textPane, list, x, y);
		popup.show();
		
		if (!list.requestFocusInWindow())
			popup.hide();
			
		
		final int offs=offset;
		list.addKeyListener(new KeyAdapter(){
			public void keyTyped(KeyEvent e){
				char ch=e.getKeyChar();
				switch (ch) {
				case KeyEvent.VK_ENTER :
					commitCompletion(offs, (String) list.getSelectedValue());
					return;
				case KeyEvent.VK_ESCAPE :
					textPane.requestFocusInWindow();
					return;
				case KeyEvent.VK_BACK_SPACE :
					textPane.requestFocusInWindow();
					try {
							remove(offs, 1);
						} catch (BadLocationException ex) {
							ex.printStackTrace();
						}
					tryCompletion(offs-1);
					return;
				case KeyEvent.VK_DELETE :
					textPane.requestFocusInWindow();
					return;
				default:
					textPane.dispatchEvent(new KeyEvent(textPane,
						    KeyEvent.KEY_TYPED, System.currentTimeMillis(),
						    e.getModifiers(), KeyEvent.VK_UNDEFINED, ch));
					tryCompletion(offs+1);
					break;
				}
			}
		});
		
		list.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				commitCompletion(offs, (String) list.getSelectedValue());
			}
		});
		
		list.addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e) {
				popup.hide();
			}
		});
	}

	public void setCompletionKeys(Set<String> keys) {
		completionKeys = keys;
	}
	
	public void setCompletionKeys(String... keys) {
		clearCompletionKeys();
		synchronized (completionKeys) {
			for (String key : keys) 
				completionKeys.add(key);	
		}
	}
	
	public void addCompletionKeys(String... keys) {
		for (String key : keys) 
			synchronized (completionKeys) {
				completionKeys.add(key);
			}
	}
	
	public void removeCompletionKeys(String... keys) {
		for (String key : keys) 
			synchronized (completionKeys) {
				completionKeys.remove(key);
			}
	}
	
	public void clearCompletionKeys(){
		synchronized (completionKeys) {
			completionKeys.clear();
		}
	}
}