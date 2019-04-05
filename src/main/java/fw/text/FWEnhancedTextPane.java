package fw.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.Keymap;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.text.TextAction;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.undo.UndoManager;

import fw.app.FWToolKit;
import fw.app.Translator.TKey;



public class FWEnhancedTextPane extends JTextPane {
	private static final TKey SYMBOLS = new TKey(FWEnhancedTextPane.class, "specialChars");
	private static final TKey GREEKS = new TKey(FWEnhancedTextPane.class, "greekChars");
	private static final TKey COPY = new TKey(FWEnhancedTextPane.class, "copy");
	private static final TKey CUT = new TKey(FWEnhancedTextPane.class, "cut");
	private static final TKey PASTE = new TKey(FWEnhancedTextPane.class, "paste");
	private static final TKey UNDO = new TKey(FWEnhancedTextPane.class, "undo");
	private static final TKey REDO = new TKey(FWEnhancedTextPane.class, "redo");

	private static final long serialVersionUID = -4419459534337649057L;
	
	public static String JAGGED_UDERLINE_ATTRIBUTE_NAME="jagged-underline";
	private boolean showNumbers = true;
	private String promptString = " > ";

	public FWEnhancedTextPane(FWEnhancedDocument doc) {
		super();
		setEditorKit(new EnhancedEditorKit());
		setDocument(doc);
		doc.setOwner(this);
		FWUndoSupport.register(this);
		addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				FWEnhancedTextPane.this.repaint();
			}
		});
		
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_DELETE && e.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD)
					e.setKeyCode(KeyEvent.VK_PERIOD);
			}
		});
		
		Keymap fwMap = addKeymap("FWMap", getKeymap());
		fwMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK), doc.new DeleteLineAction());
		setKeymap(fwMap);
	}
	
	public void setPrompt(String str) {
		promptString = str;
	}

	public void showNumbers(boolean b) {
		showNumbers = b;
	}

	public void moveCaret(int offset) {
		try {
			setCaretPosition(getCaretPosition() + offset);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	  public void replaceSelection(String content) {
		    getInputAttributes().removeAttribute(StyleConstants.Foreground);
		    super.replaceSelection(content);
		  }
	
	public void setTabulationLength(int l) {
		int tabWidth = l * getFontMetrics(getFont()).charWidth(' ');
		TabStop[] tabs = new TabStop[20];

		for (int j = 0; j < tabs.length; j++)
			tabs[j] = new TabStop((j + 1) * tabWidth);

		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setTabSet(attributes, tabSet);
		int length = getDocument().getLength();
		getStyledDocument().setParagraphAttributes(0, length, attributes, true);
	}

	
	public JPopupMenu getComponentPopupMenu() {
		requestFocus();
		JPopupMenu popup = new JPopupMenu();
		
		JMenuItem copy = new JMenuItem(new DefaultEditorKit.CopyAction());
		JMenuItem cut = new JMenuItem(new DefaultEditorKit.CutAction());
		JMenuItem paste = new JMenuItem(new DefaultEditorKit.PasteAction());
		JMenu specialCharsMenu = new JMenu(SYMBOLS.translate());
		String[] specialChars = new String[]{"π", "√()", "²", "³",  "≤", "≥", "≠"};
		for (int idx = 0; idx < specialChars.length; idx++) 
			specialCharsMenu.add(new JMenuItem(getInsertSpecialCharacterAction(specialChars[idx])));

		String[] greekChars = new String[]{"α", "β", "γ", "δ", "ε", "ζ", "η", "θ", "ι", "κ", "λ", "μ", "ν", "ξ", "ο", "π", "ρ", "σ", "τ", "υ", "φ", "χ", "ψ", "ω"};
		JMenu greekCharsMenu = new JMenu(GREEKS.translate());
		for (int idx = 0; idx < greekChars.length; idx++) 
			greekCharsMenu.add(new JMenuItem(getInsertSpecialCharacterAction(greekChars[idx])));
		
		specialCharsMenu.addSeparator();
		
		specialCharsMenu.add(greekCharsMenu);
		
		
		if (getSelectedText() == null) {
			copy.setEnabled(false);
			cut.setEnabled(false);
		}

		copy.setAccelerator(KeyStroke.getKeyStroke('C', KeyEvent.CTRL_MASK));
		cut.setAccelerator(KeyStroke.getKeyStroke('X', KeyEvent.CTRL_MASK));
		paste.setAccelerator(KeyStroke.getKeyStroke('V', KeyEvent.CTRL_MASK));

		copy.setIcon(FWToolKit.getIcon("copy.png"));
		cut.setIcon(FWToolKit.getIcon("cut.png"));
		paste.setIcon(FWToolKit.getIcon("paste.png"));

		copy.setText(COPY.translate());
		cut.setText(CUT.translate());
		paste.setText(PASTE.translate());
		
		UndoManager m = FWUndoSupport.getUndoManager(this);
		JMenuItem undo = new JMenuItem(FWUndoSupport.UNDO_ACTION);
		JMenuItem redo = new JMenuItem(FWUndoSupport.REDO_ACTION);

		undo.setEnabled(m.canUndo());
		redo.setEnabled(m.canRedo());

		undo.setAccelerator(KeyStroke.getKeyStroke('Z',	KeyEvent.CTRL_DOWN_MASK));
		redo.setAccelerator(KeyStroke.getKeyStroke('Y',	KeyEvent.CTRL_DOWN_MASK));

		undo.setIcon(FWToolKit.getIcon("edit-undo.png"));
		redo.setIcon(FWToolKit.getIcon("edit-redo.png"));

		undo.setText(UNDO.translate());
		redo.setText(REDO.translate());

		popup.add(specialCharsMenu);
		
		popup.addSeparator();
		
		popup.add(copy);
		popup.add(cut);
		popup.add(paste);
		
		popup.addSeparator();
		
		popup.add(undo);
		popup.add(redo);
		
		return popup;
	}
	
	private TextAction getInsertSpecialCharacterAction(final String str) {
		return new TextAction(str) {
			private static final long serialVersionUID = 6538101064231233913L;

			@Override
			public void actionPerformed(ActionEvent e) {
				FWEnhancedTextPane pane = FWEnhancedTextPane.this;
				int offset = pane.getCaretPosition();
				try {
					pane.getDocument().insertString(offset, str, null);
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
			}
		};
	}
	
	class EnhancedEditorKit extends StyledEditorKit {
		private static final long serialVersionUID = -194084512589826515L;
		
		public ViewFactory getViewFactory() {
			return new ViewFactory() {
				public View create(Element elem) {
					String kind = elem.getName();
					if (kind != null) {
						if (kind.equals(AbstractDocument.ContentElementName))
							return new LabelView(elem);
						else if (kind.equals(AbstractDocument.ParagraphElementName))
							return new HighlightView(elem); // the ad hoc view
						else if (kind.equals(AbstractDocument.SectionElementName))
							return new BoxView(elem, View.Y_AXIS);
						else if (kind.equals(StyleConstants.ComponentElementName))
							return new ComponentView(elem);
						else if (kind.equals(StyleConstants.IconElementName))
							return new IconView(elem);
					}
					return new LabelView(elem);
				}
			};
		}
		

		private class HighlightView extends ParagraphView {
			private short marginWidth = 0;
		    
			private HighlightView(Element elem) {
				super(elem);
	        }
			
			public void paint(Graphics g, Shape allocation) {
				Rectangle alloc = allocation.getBounds();
				Graphics2D g2 = (Graphics2D) g;
				
				// highlight
				int caretLocation = getCaretPosition();
				for (int idx = 0; idx < getViewCount(); idx++) {
					View v = getView(idx);
					if (v.getStartOffset() <= caretLocation
							&& caretLocation < v.getEndOffset()) {
						g2.setColor(UIManager.getColor("EnhancedTextPane.highlight"));
						g2.fillRect(alloc.x, alloc.y, alloc.width-1, alloc.height-1);
						g2.setColor(UIManager.getColor("EnhancedTextPane.highlightBorder"));
						g2.drawRect(alloc.x, alloc.y, alloc.width-1, alloc.height-1);
					}
				}

				// margin
				int lc = getLineCount();
				String t = (lc<10) ? "  "+lc : (lc<100) ? " "+lc : ""+lc;
				String marginText = showNumbers ? t+promptString : promptString;
				

				FWEnhancedDocument doc = ((FWEnhancedDocument) FWEnhancedTextPane.this.getDocument());
				Font font = doc.getFont();
				FontMetrics metrics = g2.getFontMetrics(font);
				Font promptFont = doc.getPromptFont();
				FontMetrics promptMetrics = g2.getFontMetrics(promptFont);

				// prompt
				g2.setColor(Color.GRAY);
				g2.setFont(promptFont);
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.drawString(marginText, alloc.x+2, alloc.y + metrics.getMaxAscent()+metrics.getLeading()-1);
				
				int adv = promptMetrics.stringWidth(marginText);
				marginWidth = (short) (adv);
				
				try {
					super.paint(g, allocation);
				} catch (NullPointerException ex) {
					System.out.println("FWEnhancedTextPane.EnhancedEditorKit.HighlightView.paint() "+ex);
					// FIXME : Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException
//					at javax.swing.text.GlyphView.paint(GlyphView.java:423)
//					at javax.swing.text.BoxView.paintChild(BoxView.java:161)
//					at javax.swing.text.BoxView.paint(BoxView.java:433)
//					at javax.swing.text.BoxView.paintChild(BoxView.java:161)
//					at javax.swing.text.BoxView.paint(BoxView.java:433)
//					at javax.swing.text.ParagraphView.paint(ParagraphView.java:580)
//					at fw.text.FWEnhancedTextPane$EnhancedEditorKit$HighlightView.paint(FWEnhancedTextPane.java:283)
					//arrive souvent avec la commande attends ...
				}
			}
			
		    protected short getLeftInset() {
		    	return (short) (super.getLeftInset()+marginWidth);
		    }
		    
			private int getLineCount() {
				int lineCount = 0;
				View parent = this.getParent();
				if (parent == null)
					return 0;
				for (int idx = 0; idx < parent.getViewCount(); idx++) {
					if (parent.getView(idx) == this)
						break;
					else
						lineCount++;
				}
				return lineCount + 1;
			}
		}
	}

}