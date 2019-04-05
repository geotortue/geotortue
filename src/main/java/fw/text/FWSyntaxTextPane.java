package fw.text;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit.DefaultKeyTypedAction;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.Keymap;

import fw.text.FWParsingTools.ParsingException;


public class FWSyntaxTextPane extends FWEnhancedTextPane {

	private static final long serialVersionUID = -847733977643340435L;

	private final HighlightPainter greenPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN);
	private final HighlightPainter redPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.PINK);
	
	private int openingBracket= -1, closingBracket= -1;
	private Object openingHL, closingHL;
	private boolean areBracketsMatching = true;

	public FWSyntaxTextPane(FWSyntaxDocument doc) {
		super(doc);
		setTabulationLength(4);
		
		addCaretListener(new CaretListener() {
			public void caretUpdate(final CaretEvent e) {
				final boolean isValid = ((JComponent) e.getSource()).isValid();
				SwingUtilities.invokeLater(new Runnable() {
					/* (non-Javadoc)
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run() {
						highlightBrackets(e.getDot(), isValid);
					}
				});						
			}
		});
		
		getKeymap().setDefaultAction(new BracketTypedAction());
		Keymap map = addKeymap("BracketMap", getKeymap());
		setKeymap(map);
	}
	
	/*
	 * Brackets highlighting
	 */
	
	private void highlightBrackets(int dot, boolean isValid) {
		areBracketsMatching = true;
		openingBracket = -1;
		closingBracket = -1;
		
		String content = getText();
		int len = content.length();
		
		if (dot<1 || len<=0 || dot>len) 
			return;
		
		FWSyntaxDocument doc = (FWSyntaxDocument) getDocument();
		if (!isValid)
			doc.prepareUpdate(doc.getText());
		
		FWScopes commentScopes = doc.getCommentScopes();
		FWScopes stringScopes = doc.getStringScopes();
		
		if (commentScopes.getScope(dot-1) == null && stringScopes.getScope(dot-1) == null)
			try {
				char c = getText(dot-1, 1).charAt(0);
				if (FWParsingTools.isOpeningBracket(c)){
					openingBracket = dot-1;
					try {
						closingBracket = FWParsingTools.getClosingBracketIdx(content, openingBracket, stringScopes, commentScopes);
					} catch (ParsingException ex) {
						areBracketsMatching = false;
						closingBracket = ex.getIndex();
					}
				}
				
				if (FWParsingTools.isClosingBracket(c)){
					closingBracket = dot-1;
					try {
						openingBracket = FWParsingTools.getOpeningBracketIdx(content, closingBracket, stringScopes, commentScopes);
					}  catch (ParsingException ex) {
						areBracketsMatching = false;
						openingBracket = ex.getIndex();
					}				
				}
				
				highlightBrackets();
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			} 
	}
	
	private void highlightBrackets() {
		if (openingBracket<0 && closingBracket<0) 
			if (openingHL == null && closingHL == null)
				return;
		
		if (openingHL != null)
			getHighlighter().removeHighlight(openingHL);
		if (closingHL != null)
				getHighlighter().removeHighlight(closingHL);

		if (openingBracket>=0 && closingBracket>=0) {
			HighlightPainter painter = (areBracketsMatching)? greenPainter : redPainter;
			openingHL = highlight(openingBracket, painter);
			closingHL = highlight(closingBracket, painter);
		} else if (openingBracket>=0) {
			openingHL = highlight(openingBracket, redPainter);
			closingHL = null;
		} else if (closingBracket>=0) {
			openingHL = null;
			closingHL = highlight(closingBracket, redPainter);
		}
		
	}
	
	protected Object highlight(int start, int end, HighlightPainter painter) {
		try {
			return getHighlighter().addHighlight(start, end, painter);
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private Object highlight(int offset, HighlightPainter painter) {
		return highlight(offset, offset+1, painter);
	}
	
	
	/*
	 * 
	 */
	public void autoScrollTo(final int offset, final int len) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Rectangle rect = modelToView(offset);
					scrollRectToVisible(rect);
					//int c = getCaretPosition();
					//if (c < offset || c > offset + len)
					setCaretPosition(offset);
				} catch (BadLocationException ex) { 
					ex.printStackTrace();
				}
			}
		});
	}
	
	private boolean handleOpeningBrackets(char openMark) {
		char closeMark = 0;
		switch (openMark) {
			case '(':
				closeMark = ')';
				break;
			case '[' :
				closeMark = ']';
				break;
			case '{' :
				closeMark = '}';
				break;
			default:
				return false;
		}
		
		String selectedText = getSelectedText();
		if (selectedText != null) {
			replaceSelection(openMark+selectedText +closeMark);
		} else {
			replaceSelection(openMark+""+closeMark);
			moveCaret(-1);
		}
		return true;
	}
	
	private boolean handleClosingBracket(char c) {
		if (!FWParsingTools.isClosingBracket(c))
			return false;
		int dot = getCaretPosition();
		String text = getText();
		if (dot>=text.length()) 
			return false;
		if (text.charAt(dot) == c) {
			moveCaret(1);
			return true;
		}
			
		return false;
	}
	
	private class BracketTypedAction extends DefaultKeyTypedAction  {
		private static final long serialVersionUID = 52489561826227024L;

		public void actionPerformed(ActionEvent e) {
			char c = e.getActionCommand().charAt(0);
			if (handleOpeningBrackets(c))
				return;
			
			if (handleClosingBracket(c))
				return;

			super.actionPerformed(e);
        }
    }
}
