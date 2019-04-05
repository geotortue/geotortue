package fw.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;


public abstract  class FWSyntaxDocument extends FWCompletionDocument {
	private static final long serialVersionUID = 3852230541181480672L;

	private FWStyledKeySet<?>[] keySets;

	private final FWScopeParserI commentScopeParser;
	private final FWScopeParserI stringScopeParser = new FWStringScopeParser();
	private FWScopes commentScopes, stringScopes;
	private final FWStylesManager styles;
	
	private UpdatesWorker worker = new UpdatesWorker();
	private boolean cancelWorker = true;
	
	public FWSyntaxDocument(FWStylesManager styles, FWScopeParserI commentParser, FWStyledKeySet<?>... keywordSets) {
		super(styles.getDefaultStyle());
		this.styles = styles;
		this.commentScopeParser = commentParser;
		this.keySets = keywordSets;
		putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
		setParagraphAttributes(0, 0, defaultStyle, true);
		setCharacterAttributes(0, 1, defaultStyle, true);
	}

	public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
		super.insertString(offset, str, defaultStyle);
		launchUpdating(offset, str, true);
	}
	
	public void remove(int offset, int length) throws BadLocationException {
		String str = getText(offset, length);
		super.remove(offset, length);
		launchUpdating(offset, str, false);
	}

	private void launchUpdating(final int offset, final String str, final boolean insert) {
		if (getLength()==0)
			return;
		if (cancelWorker)
			worker.cancel(true);
		worker = new UpdatesWorker() {
			@Override
			protected UpdatesCollector doInBackground() throws Exception {
//				prepareUpdate(content);
				if (isCancelled()) 
					return null;
				return collectUpdates(offset, str);
			}

			@Override
			protected void done() {
				if (isCancelled()) 
					return;
				try {
					update(getText(), get());
				} catch (InterruptedException | ExecutionException ex) {
					ex.printStackTrace();
				}
			}
		};
		worker.execute();
	}

	protected void prepareUpdate(String content)  {
		stringScopes = stringScopeParser.parse(content);
		commentScopes = commentScopeParser.parse(content, stringScopes);
	}
	
	protected UpdatesCollector collectUpdates(int offset, String str) {
		UpdatesCollector collector = new UpdatesCollector();
		boolean hasComment = (str.indexOf('\"')>=0) || (str.indexOf('*')>=0) || (str.indexOf('/')>=0);
		if (hasComment) {
			collector.updateAll();
			return collector;
		}
		collector.addLines(offset, str.length());
		return collector;
	}
	
	private void update(String content, UpdatesCollector collector) {
		prepareUpdate(content);
		if (collector.updateAll)
			processDoc(content);
		else {
			for (Update update : collector.updates) 
				processContent(content, update.start, update.end);
			
			for (String token : collector.tokens.keySet()) 
				updateToken(content, token, collector.tokens.get(token));
		}
	}

	public void refresh(){
		final String content = getText();
		if (!SwingUtilities.isEventDispatchThread())
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					prepareUpdate(content);
					processDoc(content);
				}
			});
		else {
			prepareUpdate(content);
			processDoc(content);
		}
	}
		
	private void processDoc(String content) {
		processContent(content, 0, getLength()-1);
	}
	
	private void processContent(String content, int startOffset, int end) {
		// writeLock();
		int endOffset = (end>getLength()) ? getLength() : end; 
		int len = endOffset - startOffset; // + 1;
		
		// set normal attributes 
		setCharacterAttributes(startOffset, len, defaultStyle, true);
		
		// check for tokens
		highlightTokens(content, startOffset, endOffset);
		
		// custom
		customHighlight(content, startOffset, endOffset);
		
		// paint comments
		paintComments();
		
		// paint strings
		paintStrings();

		//writeUnlock();
	}
	
	protected void paintStrings() {
		TextStyle style = styles.getStringStyle();
		for (FWScope scope : stringScopes.getScopes()) {
			int start = scope.getStart();
			if (commentScopes.getScope(start) == null)
				setCharacterAttributes(start, scope.getLength(), style, true);
		}
	}
	
	protected void paintComments() {
		TextStyle style = styles.getCommentsStyle();
		for (FWScope scope : commentScopes.getScopes())
			setCharacterAttributes(scope.getStart(), scope.getLength(), style, true);
	}
	
	protected void customHighlight(String content, int startOffset, int endOffset) {
	}
	
	private void highlightTokens(String content, int startOffset, int endOffset) {
		int index = startOffset;
		while (index <= endOffset) {
			String token = FWParsingTools.getToken(content, index);
			int tokenLength = token.length();
			if (tokenLength!=0) {
				lookForToken(content, token, index, tokenLength);
				index += tokenLength;
			}
			index++;
		}
	}
	
	private void lookForToken(String content, String token, int startOffset, int tokenLength){
		if (commentScopes.getScope(startOffset) != null || stringScopes.getScope(startOffset) != null)
			return;
		paintToken(content, token, startOffset, tokenLength);
	}
	
	protected void paintToken(String content, String token, int startOffset, int tokenLength){
		for (FWStyledKeySet<?> keyset : keySets) {
			if (keyset.getKeys().contains(token)) {
				setCharacterAttributes(startOffset, tokenLength, keyset.getStyle(), true);
				return;
			}
		}
	}
	
	private void updateToken(String content, String token, FWStyledKeySet<?> set){
		int tokenLength = token.length();
		int offset = FWParsingTools.indexOfToken(content, token, 0);
		while (offset>=0) {
			updateToken(token, offset, tokenLength, set);
			offset = FWParsingTools.indexOfToken(content, token, offset+tokenLength);
		}
	}
	
	private void updateToken(String token, int offset, int tokenLength, FWStyledKeySet<?> set){
		if (commentScopes.getScope(offset) != null || stringScopes.getScope(offset) != null)
			return;
		if (set==null) {
			setCharacterAttributes(offset, tokenLength, defaultStyle, true);
			return;
		}
		if (set.getKeys().contains(token)) {
			setCharacterAttributes(offset, tokenLength, set.getStyle(), true);
			return;
		} 
		setCharacterAttributes(offset, tokenLength, defaultStyle, true);
	}
	
	
	protected FWScopes getCommentScopes() {
		return commentScopes;
	}

	protected FWScopes getStringScopes() {
		return stringScopes;
	}
	
	
	/*
	 * 
	 */
	
	protected class UpdatesWorker extends SwingWorker<UpdatesCollector, UpdatesCollector> {
		@Override
		protected UpdatesCollector doInBackground() throws Exception {
			return null;
		}
	}
	
	public class UpdatesCollector {
		private boolean updateAll = false;
		private ArrayList<Update> updates = new ArrayList<>();
		private HashMap<String, FWStyledKeySet<?>> tokens = new HashMap<>();
		
		public void addLines(int offset, int len) {
			Element root = getDefaultRootElement();
			int startLine = root.getElementIndex(offset);
			final int start = root.getElement(startLine).getStartOffset();
			int endLine = root.getElementIndex(offset + len);
			final int end = root.getElement(endLine).getEndOffset();
			updates.add(new Update(start, end));
		}
		
		
		public void addUpdate(int start, int end) {
			updates.add(new Update(start, end));
		}
		
		public void updateAll() {
			updateAll = true;
		}
		
		public boolean shouldUpdateAll() {
			return updateAll;
		}

		/**
		 * paint this token according to the given styledKeySet
		 */
		public void addToken(String token, FWStyledKeySet<?> set) {
			tokens.put(token, set);
		}
		
		/**
		 * paint this token with default style
		 * 
		 */
		public void removeToken(String token) {
			tokens.put(token, null);
		}
	}
	
	private class Update {
		private final int start, end;

		public Update(int start, int end) {
			this.start = start;
			this.end = end;
		}
	}
	
	
	public void setCancelWorker(boolean cancelWorker) {
		this.cancelWorker = cancelWorker;
	}
}