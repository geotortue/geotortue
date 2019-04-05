package geotortue.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultEditorKit.CopyAction;
import javax.swing.text.TextAction;

import fw.app.FWToolKit;
import fw.app.Translator.TKey;
import fw.gui.FWButton;
import fw.gui.FWButton.FWButtonListener;
import fw.text.FWEnhancedDocument;
import fw.text.FWScope;
import fw.text.FWScopes;
import geotortue.core.GTCodeDocument;
import geotortue.core.GTTextPaneWithButton;
import geotortue.core.KeywordManager;

public class GTBoard {

	private static final TKey FLUSH = new TKey(GTBoard.class, "flush.tooltip");
	private final static Color BG_COLOR = new Color(248, 248, 255);
	private static final TKey COPY = new TKey(GTBoard.class, "copy");

	private final KeywordManager keywordManager;
	private FWScopes scopes = new FWScopes();
	private final BoardDocument boardDoc;
	private final JTextPane boardPane;
	

	public GTBoard(KeywordManager km) {
		this.keywordManager = km;
		this.boardDoc = new BoardDocument(km);
		this.boardPane = new BoardPane(boardDoc);

		boardPane.setEditable(false);
		boardPane.setFocusable(false);
		boardPane.setBackground(BG_COLOR);
	}
	/**
	 * add text to board.
	 * if procArguments or loopVarNames is null, add text verbatim
	 * @param text
	 * @param procArguments
	 * @param loopVarNames
	 */
	public void add(String text, String[] procArguments, String[] loopVarNames) {
		int scopesSize = scopes.getScopes().size();
		if (scopesSize<500)
			boardDoc.appendString(text, procArguments, loopVarNames);
		else if (scopesSize==500)
			boardDoc.appendString("[...]", procArguments, loopVarNames);
		else 
			return;
		
		boardPane.setCaretPosition(boardDoc.getLength());
	}
	
	public void clear() {
		boardDoc.removeAll();
		scopes = new FWScopes();
	}

	private class BoardDocument extends GTCodeDocument {
		private static final long serialVersionUID = 4400188722068286975L;

		public BoardDocument(KeywordManager keywordManager) {
			super(keywordManager);
			setCancelWorker(false);
		}

		private synchronized void appendString(final String str, String[] procArguments, String[] loopVarNames) {
			try {
				int start = getLength();
				int end = start + str.length();
				if (procArguments == null || loopVarNames == null)
					scopes.addScope(new BoardScope(start, end));
				else
					scopes.addScope(new BoardScope(start, end, procArguments, loopVarNames));
				insertString(start, str+"\n", defaultStyle);
			} catch (BadLocationException ex) {
				ex.printStackTrace(); // should not occur
			}
		}

		protected void paintToken(String content, String token, int startOffset, int tokenLength) {
			BoardScope scope = (BoardScope) scopes.getScope(startOffset);
			if (scope == null ||scope.isVerbatim())
				return;

			super.paintToken(content, token, startOffset, tokenLength);
			
			paintVars(token, startOffset, tokenLength, scope);
		}

		private void paintVars(String token, int startOffset, int tokenLength, BoardScope scope) {
			for (String varName : scope.loopVarNames)
				if (varName.equals(token)) {
					setCharacterAttributes(startOffset, tokenLength, keywordManager.getLoopVariablesAttributeSet(), true);
					return;
				}
			
			for (String varName : scope.procArguments)
				if (varName.equals(token)) {
					setCharacterAttributes(startOffset, tokenLength, keywordManager.getLocalVariablesAttributeSet(), true);
					return;
				}
		}
		
		protected void paintComments() {}
	}

	private class BoardScope extends FWScope {
		private final String[] procArguments;
		private final String[] loopVarNames;
		private final boolean isVerbatim;

		public boolean isVerbatim() {
			return isVerbatim;
		}

		public BoardScope(int start, int end, String[] procArguments, String[] loopVarNames) {
			super(start, end);
			this.procArguments = procArguments;
			this.loopVarNames = loopVarNames;
			this.isVerbatim = false;
		}
		
		public BoardScope(int start, int end) {
			super(start, end);
			this.procArguments = null;
			this.loopVarNames = null;
			this.isVerbatim = true;
		}
	}
	

	private class BoardPane extends JTextPane {

		private static final long serialVersionUID = -8363369555422959147L;

		public BoardPane(FWEnhancedDocument doc) {
			super(doc);
			setFocusable(false);
			setEditable(false);
			doc.setOwner(this);
			setMargin(new Insets(5, 10, 5, 10));
		}

		// no wrap
		public boolean getScrollableTracksViewportWidth() {
			Component parent = getParent();
			return parent != null ? (getUI().getPreferredSize(this).width <= parent.getSize().width) : true;
		}

		public JPopupMenu getComponentPopupMenu() {
			requestFocus();
			JPopupMenu popup = new JPopupMenu();

			JMenuItem copy = new JMenuItem(new TextAction("copyAll") {
				private static final long serialVersionUID = 4276832507146186653L;

				@Override
				public void actionPerformed(ActionEvent e) {
					select(0, getDocument().getLength());
					CopyAction copyAction = new DefaultEditorKit.CopyAction();
					copyAction.actionPerformed(new ActionEvent(BoardPane.this, 0, "copy-all"));
				}
			});
			copy.setIcon(FWToolKit.getIcon("copy.png"));

			copy.setText(COPY.translate());
			popup.add(copy);

			return popup;
		}
	}

	// TODO : (done) commande « aff » sans argument efface le tableau d'affichage

	public JComponent getPane() {
		JButton flushButton = FWButton.createIconButton(FLUSH, "edit-clear.png", new FWButtonListener() {
			@Override
			public void actionPerformed(ActionEvent e, JButton source) {
				clear();
			}
		});
		return new GTTextPaneWithButton(boardPane, flushButton);
	}
}