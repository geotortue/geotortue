package geotortue.core;

import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import fw.app.Translator.TKey;
import fw.text.FWEnhancedTextPane;
import fw.text.FWParsingTools;
import fw.text.FWScope;
import fw.text.FWScopes;
import geotortue.core.GTCommandFactory.GTCommandKey;
import geotortue.core.Procedure.ProcedureParsingException;
import geotortue.core.ProcedureUpdater.ProtoProcedure;
import geotortue.core.ThreadQueue.ThreadQueueListener;
import geotortue.gui.GTTextPane;


public class GTDocumentFactory  {
	
	private static final TKey NO_PROC = new TKey(GTDocumentFactory.class, "noProcedure");
	private final ProcedureDocument procedureDoc;
	private final CommandDocument commandDoc;

	private final GTProcessingContext processingContext;
	private final KeywordManager keywordManager;
	
	private final GTTextPane procedurePane, commandPane;
	private boolean locked = false;
	
	private final ProcedureManager procManager;  

	public GTDocumentFactory(KeywordManager km, GTProcessingContext pc){
		this.processingContext=pc;
		this.keywordManager = km;
		this.procedureDoc = new ProcedureDocument();
		this.commandDoc = new CommandDocument();
		this.procedurePane = new GTTextPane(procedureDoc);
		this.procManager = pc.getProcedureManager();
		this.commandPane = new GTTextPane(commandDoc) {
			private static final long serialVersionUID = 4357906404862828973L;

			@Override
			public void autoScrollTo(int offset, int len) {} // no auto scrolling in command document
		};
		commandPane.showNumbers(false);
		commandPane.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER && e.getModifiers() == KeyEvent.SHIFT_MASK) {
					int offset = commandPane.getCaretPosition();
					((CommandDocument) commandPane.getDocument()).insertLine(offset);
				} 
			}
		});
		processingContext.addThreadQueueListener(new ThreadQueueListener() {
			@Override
			public void started() {
				locked = true;
			}


			@Override
			public void stopped() {
				locked = false;
			}
		});
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	
	public FWEnhancedTextPane getProcedurePane() {
		return procedurePane;
	}


	public FWEnhancedTextPane getCommandPane() {
		return commandPane;
	}

	public void parseAndRefresh() {
		procedureDoc.refresh();
		commandDoc.refresh();
	}

	
	public void replace(String target, String replacement, boolean smart) {
		commandDoc.replace(target, replacement, smart);
		procedureDoc.replace(target, replacement,smart);
	}
	
	/*
	 * 
	 */
	
	public void appendAndProcessCommand(String command) {
		commandDoc.appendAndProcessCommand(command);
	}


	CommandDocument getCommandDocument(){
		return commandDoc;
	}
	
	public ProcedureDocument getProcedureDocument() {
		return procedureDoc;
	}
	
	/*
	 * HTML
	 */
	
	public String getHtmlText() { 
		String text= "";
		procedureDoc.refresh();
		if (procManager.isEmpty())
			return NO_PROC.translate()+"\t"; 
		
		for (String key : procManager.getSortedKeys())
			text += "<p>"+procManager.getProcedure(key).getHtmlText(keywordManager)+"</p>";
		return text;
	}
	
	public String getHtmlText(String command) {
		return Procedure.getHtmlText(keywordManager, command);
	}
	
	
	public void makeProcedure(Window owner, String key) throws GTException {
		keywordManager.testValidity(SourceLocalization.create(key, owner));
		if (isLocked())
			return;
		String content = commandPane.getSelectedText();
		if (content == null) {
			content = commandDoc.getText();
			commandDoc.removeAll();
			try {
				commandDoc.insertString(0, key, null);
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			};
		} else {
			int start = commandPane.getSelectionStart();
			int end = commandPane.getSelectionEnd();
			int length = end - start;
			try {
				commandDoc.remove(start, length);
				commandDoc.insertString(start, key, null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		procedureDoc.appendProcedure(key, content.trim());
		parseAndRefresh();
	}
	
	/**
	 * 
	 * Procedure Document
	 *
	 */
	public class ProcedureDocument extends GTCodeDocument {
		private static final long serialVersionUID = 2472459467415127240L;
		private Stack<GTException> warnings = new Stack<GTException>();
		
		
		private ProcedureDocument() {
			super(keywordManager);
		}

		public void insertString(int offset, String s, AttributeSet a) throws BadLocationException {
			if (!isLocked())
				super.insertString(offset, s, a);
		}
		
		public void remove(int offset, int length) throws BadLocationException {
			if (!isLocked())
				super.remove(offset, length);
		}
		
		private final ProcedureUpdater updater = new ProcedureUpdater(this);
		
		protected UpdatesCollector collectUpdates(int offset, String str) {
			UpdatesCollector collector = super.collectUpdates(offset, str);
			if (collector.shouldUpdateAll()) {
				updateProcedureManager();
				return collector;
			}
			
			boolean hasError = false;
			HashMap<String, ProtoProcedure> newProcs = null;
			try {
				readLock();
				
				newProcs = updater.parse(0, getLength(), getCommentScopes(), getStringScopes());
			} catch (GTException ex) {
				warnings.push(ex);
				if (warnings.size() > 2) {
					ex.displayTransientWindow();
					warnings.clear();
					hasError = true;
				}
			} finally {
				readUnlock();
			}
			
			if (newProcs == null)
				return collector;
			
			keywordManager.clearProcedures();
			
			ArrayList<String> removedKeys = new ArrayList<>();
			for (String key : procManager.getKeys()) 
				if (newProcs.get(key)==null) 
					removedKeys.add(key);
			
			for (String key  : removedKeys) { // update removed procedures keys
				procManager.remove(key);
				collector.removeToken(key);
			}
			
			for (String key : newProcs.keySet()) {
				ProtoProcedure p_ = newProcs.get(key);
				final int start = p_.getOffset();
				final int len = p_.getLength();
				final int end = start+len;
				Procedure old = procManager.getProcedure(key);
				if (old==null) { // procedure added
					try {
						Procedure p = new Procedure(keywordManager, p_);
						procManager.add(p, start, end);
						keywordManager.addProcedure(key, collector);
					} catch (GTException ex) {
						warnings.push(ex);
						if (warnings.size() > 2) {
							ex.displayTransientWindow();
							warnings.clear();
							hasError = true;
						}
					} catch (ProcedureParsingException e) {
					}
				} else {
					if (old.isSimilarTo(p_)) {	// unchanged
						Procedure p = new Procedure(p_, old);
						procManager.add(p, start, end);
						keywordManager.addProcedure(key);
					} else
						try { // changed
							Procedure p = new Procedure(keywordManager, p_);
							procManager.add(p, start, end);
							keywordManager.addProcedure(key);
							collector.addUpdate(start, end);
						} catch (GTException ex) {
							warnings.push(ex);
							if (warnings.size() > 2) {
								ex.displayTransientWindow();
								warnings.clear();
								hasError = true;
							}
						} catch (ProcedureParsingException e) {
						}
				}
			}
			
			keywordManager.updateCompletionKeys();
			
			if (!hasError)
				warnings.clear();
			
			return collector;
		}

		private boolean updateProcedureManager() {
			procManager.clear();
			keywordManager.clearProcedures();
			return updateProcedureManager(0, getLength());
		}
		
		private final String startKey = KeywordManager.START_KEY.translate();
		private final String endKey = KeywordManager.END_KEY.translate();
		
		private boolean updateProcedureManager(int start, int end) {
			ArrayList<String> procs = procManager.getKeys(start, end);
			for (String key : procs) {
				procManager.remove(key);
				keywordManager.removeProcedure(key);
			}
			
			boolean hasError = false;
			
			try {
				
				String content = getText();
				int offset = procManager.getStart(start);
				FWScopes commentScopes = getCommentScopes();
				FWScopes stringScopes = getStringScopes();
				
				while (offset>-1) {
					
					int startOffset = FWParsingTools.indexOfToken(content, startKey, offset);
					if (startOffset<0 || startOffset>=end)
						return hasError;
					
					FWScope cScope = commentScopes.getScope(startOffset);
					FWScope sScope = stringScopes.getScope(startOffset);
					if (cScope!=null) 
						offset = cScope.getEnd();
					else if (sScope!=null) 
						offset = sScope.getEnd();
					else {
						int nextStartOffset = FWParsingTools.indexOfToken(content, startKey, offset+1, commentScopes, stringScopes);
						int stopOffset = FWParsingTools.indexOfToken(content, endKey, offset, commentScopes, stringScopes);
			
						int endOffset;
						if (nextStartOffset > -1)
							endOffset = (stopOffset > -1) ? Math.min(stopOffset, nextStartOffset) : nextStartOffset;
						else
							endOffset = (stopOffset > -1) ? stopOffset : content.length();
							
						int len = endOffset-startOffset;
							
						if (startOffset>-1 && (len>0)){
							try {
								writeLock();
								SourceLocalization loc = new SourceLocalization(this, startOffset, len);
								Procedure proc = new Procedure(keywordManager, loc);
								procManager.add(proc, startOffset, endOffset);
								keywordManager.addProcedure(proc.getKey());
							} catch (ProcedureParsingException e) {
								// do nothing
							} finally {
								writeUnlock();
							}
						}
						offset = nextStartOffset;
					}
				}
				
				keywordManager.updateCompletionKeys();
				
				if (warnings.size()>0) 
					warnings.clear();
			} catch (GTException ex) {
				warnings.push(ex);
				if (warnings.size()>2) {
					ex.displayTransientWindow();
					warnings.clear();
				}
				hasError = true;
			}
			if (!hasError)
				warnings.clear();
			return hasError;
		}
		
		protected void customHighlight(String content, int startOffset, int endOffset) {
			paintLocalVariables(content, startOffset, endOffset);
			super.customHighlight(content, startOffset, endOffset);
		}
		
		private void paintLocalVariables(String content, int s, int e) {
			FWScopes commentScopes = getCommentScopes();
			FWScopes stringScopes = getStringScopes();
			AttributeSet set = keywordManager.getLocalVariablesAttributeSet();
			for (String key : procManager.getKeys(s, e)) {
				int start = procManager.getStart(key);
				int end = procManager.getEnd(key);
				Procedure p = procManager.getProcedure(key);
				for (String localVar : p.getLocalVariables()) {
					int len = localVar.length();
					int startOffset = FWParsingTools.indexOfToken(content, localVar, start);
					while (startOffset>start && startOffset<end) {
						FWScope scope = commentScopes.getScope(startOffset);
						if (scope == null)
							scope = stringScopes.getScope(startOffset);
						if (scope != null)
							startOffset = scope.getEnd();
						else  {
							setCharacterAttributes(startOffset, len, set, true);
							startOffset = FWParsingTools.indexOfToken(content, localVar, startOffset+len);
						}
					}
				}
			}
		}

		
		private void appendProcedure(String key, String content) {
			int len = getLength();
			String body = trimProcedure(content);
			
			String text = len>0 ? "\n\n" : "";
			text += KeywordManager.START_KEY.translate()+" "+key+"\n"+body+"\n"+KeywordManager.END_KEY.translate();
			try {
				super.insertString(len, text, null);
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			}
		}
		
		private String trimProcedure(String content) {
			String body = content.trim();
			String vg = GTCommandDescTable.getName(GTCommandKey.VG);
			if (body.startsWith(vg)) {
				body = body.substring(vg.length());
				return trimProcedure(body);
			}
			if (body.startsWith(";")) {
				body = body.substring(1);
				return trimProcedure(body);
			}
			return body;
		}

		public void append(Procedure p){
			int length = getLength();
			String text = getLength()>0 ? "\n\n" : "";
			text += p.getRawText().trim()+"\n"+KeywordManager.END_KEY.translate();
			try {
				insertString(length, text, defaultStyle);
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			}
			refresh();
		}
		
		public void refresh() {
			updateProcedureManager();
			super.refresh();
		}
		
		public void remove(Procedure p){
			String key = p.getKey();
			int s = procManager.getStart(key);
			int e = procManager.getEnd(key);
			
			String endTag = KeywordManager.END_KEY.translate();
			int endTagLen = endTag.length();
			if (e+endTagLen <= getLength() && getText(e, endTagLen).equals(endTag))
				e += endTagLen;
			
			try {
				super.remove(s, e-s);
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			}
			refresh();
		}
	}
	
	/**
	 * 
	 * Command Document
	 *
	 */
	class CommandDocument extends GTCodeDocument {
		
		private static final long serialVersionUID = 3275537626310119031L;
		private int lockIndex = -1; // prevent to change text while bundles are executed

		public CommandDocument() {
			super(keywordManager);
		}

		@Override
		public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
			if (str.length()!=1 || "\r\n".indexOf(str.charAt(0))<0) {
				if (!isLocked())
					lockIndex = -1;
				if (!isLocked() || offset>lockIndex) 
					super.insertString(offset, str, a);
				return;
			}

			String content = getText(0, getLength());
			int lineIdx = getDefaultRootElement().getElementIndex(offset);
			Element line = getDefaultRootElement().getElement(lineIdx);
			int start = line.getStartOffset();
			int end = line.getEndOffset()-1;
			
			if (end==content.length())
				super.insertString(end, str, a);
			
			getTextPane().setCaretPosition(end+1);
			
			launchExecution(this, start, end-start);
			if (end>lockIndex)
				lockIndex = end;
		}

		public void remove(int offset, int len) throws BadLocationException {
			if (!isLocked() || offset>lockIndex) 
				super.remove(offset, len);
		}
		
		private void launchExecution(GTCodeDocument doc, int offset, int len) {
			try {
				if (!isLocked()) {
					procedureDoc.refresh();
					if (!procedureDoc.warnings.isEmpty())  
						throw procedureDoc.warnings.pop();
				}
				
				SourceLocalization loc = new SourceLocalization(this, offset, len);
				GTCommandBundles bundles = GTCommandBundle.parse(loc);
				processingContext.launchExecution(bundles, new Runnable() {
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								commandPane.removeHighlight();
								procedurePane.removeHighlight();
							}
						});
						refresh();
					}
				});
			} catch (GTException ex) {
				ex.displayDialog();
			}
		}
		
		private void appendAndProcessCommand(String command) {
//			if (isLocked())
//				return;
			int offset = commandDoc.getLength();
			try {
				insertString(offset, command+"\n", null);
			} catch (BadLocationException ex) {
				ex.printStackTrace(); // should not occur
			}
			launchExecution(this, offset, command.length());
			getTextPane().setCaretPosition(getLength());
		}
		
		private void insertLine(int offset){
			try {
				super.insertString(offset, "\n", null);
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/*
	 * Find
	 */
	
	public boolean find(String str) {
		int pIdx = procedureDoc.getText().indexOf(str, procedurePane.getCaretPosition());
		int cIdx = commandDoc.getText().indexOf(str, commandPane.getCaretPosition());
		if (pIdx<0 && cIdx<0) {
			pIdx = procedureDoc.getText().indexOf(str, 0);
			cIdx = commandDoc.getText().indexOf(str, 0);
		}
		if (pIdx<0 && cIdx<0)
			return false;
		if (pIdx>-1) {
			procedurePane.select(pIdx, pIdx+str.length());
			procedurePane.requestFocusInWindow();
		} else {
			commandPane.select(cIdx, cIdx+str.length());
			commandPane.requestFocusInWindow();
		}
		return true;
	}
	
	public String getSelectedText() {
		if (commandPane.isFocusOwner())
			return commandPane.getSelectedText();
		if (procedurePane.isFocusOwner())
			return procedurePane.getSelectedText();
		return null;
	}


	/**
	 * 
	 */
	public void flush() {
		commandDoc.removeAll();
	}
	
}