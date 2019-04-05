package fw.text;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.undo.UndoableEdit;

public class FWEnhancedDocument extends DefaultStyledDocument {
	private static final long serialVersionUID = 7878259653812416081L;

	protected final TextStyle defaultStyle;
	protected Font promptFont;
	
	private final static String OWNER_PROPERTY = "documentOwner"; 
	
	public FWEnhancedDocument(TextStyle style){
		defaultStyle = style;
		promptFont = defaultStyle.deriveFont(TextStyle.PLAIN, -6);
		setParagraphAttributes(0, 0, defaultStyle, true);
	}
	
	public JTextPane getTextPane() {
		return (JTextPane) getProperty(OWNER_PROPERTY);
	}

	protected void fireUndoableEditUpdate(UndoableEditEvent e) {
		super.fireUndoableEditUpdate(getEvent(e));
	}
	
	private UndoableEditEvent getEvent(UndoableEditEvent e) {
		UndoableEdit edit = e.getEdit();
		if (edit instanceof AbstractDocument.DefaultDocumentEvent) {
			DocumentEvent.EventType type = ((AbstractDocument.DefaultDocumentEvent) edit).getType();
			if (type == DocumentEvent.EventType.CHANGE) {
				return new UndoableEditEvent(e.getSource(), new FWUndoableEdit(edit));
			}
		} 
		return e;
	}

	public String getText(){
		try {
			return getText(0, getLength());
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
		return "";
	}
	
	public void setText(final String text) {
		removeAll();
		try {
			insertString(0, text, null);
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				getTextPane().scrollRectToVisible(new Rectangle(0, 0, 1, 1));				
			}
		});
	}

	public void removeAll() {
		try {
			remove(0, getLength());
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
	}
	
	public class DeleteLineAction extends TextAction {
		private static final long serialVersionUID = -385023266866485171L;

		public DeleteLineAction() {
			super("delete-line");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JTextComponent jtc = (JTextComponent) e.getSource();
			int pos = jtc.getCaretPosition();
			Element rootElement = getDefaultRootElement();
			int offset = rootElement.getElementIndex(pos);
			Element line = rootElement.getElement(offset);
			int start = line.getStartOffset();
			int end = line.getEndOffset();
			int len = end-start;
			if (start+len>getLength())
				len--;
			try {
				remove(start, len);
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
	public void refresh() {
		promptFont = defaultStyle.deriveFont(TextStyle.PLAIN, -6);
		setCharacterAttributes(0, getLength(), defaultStyle, true);		
	}

	/*
	 * STYLE
	 */
	
	protected TextStyle getDefaultStyle(){
		return defaultStyle;
	}
	
	public Font getFont(){
		return getDefaultStyle().getFont();
	}
	
	public String getFontFamily(){
		return getDefaultStyle().getFontFamily();
	}
	
	public int getFontSize(){
		return getDefaultStyle().getFontSize();
	}
	
	public Font getPromptFont() {
		return promptFont;
	}

	public void setOwner(JTextPane pane) {
		putProperty(OWNER_PROPERTY, pane);
	}
}