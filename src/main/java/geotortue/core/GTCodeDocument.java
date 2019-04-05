package geotortue.core;

import java.awt.Color;
import java.awt.Window;
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;

import fw.text.FWParsingTools;
import fw.text.FWScope;
import fw.text.FWScopes;
import fw.text.FWSyntaxDocument;
import fw.text.TextStyle;
import geotortue.core.GTCommandFactory.GTCommandKey;
import geotortue.gui.GTTextPane;

public class GTCodeDocument extends FWSyntaxDocument implements SourceProvider {
	
	private static final long serialVersionUID = -7758047160379440222L;
	

	private TextStyle badStyle = new TextStyle();
	private String[] badStrings = new String[]{";=", ": =", "=:", "0[", "1[", "2[", "3[", "4[", "5[", "6[", "7[", "8[", "9["};
	
	private final KeywordManager keywordManager;
	private final String forEachToken = GTCommandDescTable.getName(GTCommandKey.FOR_EACH);
	private final String fromToken = KeywordManager.FROM_KEY.translate(); // de : pour_chaque i de 1 à 10
	private final String toToken = KeywordManager.TO_KEY.translate(); 
	private final String inListToken = KeywordManager.IN_LIST_KEY.translate(); // dans : pour_chaque el dans L

	
	private final HashMap<String, FWScopes> loopScopesTable = new HashMap<>();
	private final Stack<GTException> warnings = new Stack<GTException>();
	
	public GTCodeDocument(KeywordManager km) {
		super(km, new GTCommentsScopeParser(), km.getKeySets());
		this.keywordManager = km;
		keywordManager.getDefaultStyle().register(this);
		setCompletionKeys(keywordManager.getCompletionKeys());
		badStyle.setBackground(Color.PINK);
	}
	
	@Override
	public void refresh() {
		super.refresh();
		badStyle.setFontFamily(getFontFamily());
		badStyle.setFontSize(getFontSize());
	}
	
	
	
	@Override
	protected void prepareUpdate(String content) {
		super.prepareUpdate(content);
		loopScopesTable.clear();
		parseLoopScopes(new SourceLocalization(this, 0, getLength()));
	}
	
	protected void customHighlight(String content, int _startOffset, int endOffset) {
		int startOffset = _startOffset;
		if (_startOffset>0)
			startOffset--;
		int idx = content.indexOf(":=", startOffset);
		while (idx >= startOffset && idx < endOffset) {
			AttributeSet set = getCharacterElement(idx).getAttributes();
			if (!StyleConstants.isBold(set))
				setCharacterAttributes(idx, 2, KeywordManager.BOLD_STYLE, true);
			idx = content.indexOf(":=", idx+2);
		}
		paintBadStrings(content, startOffset, endOffset, getCommentScopes(), getStringScopes());
	}
	
	private void paintBadStrings(String content, int startOffset, int endOffset, FWScopes commentScopes, FWScopes stringScopes) {
		for (String str : badStrings) {
			int len = str.length();
			int offset = content.indexOf(str, startOffset);
			while (offset>=startOffset && offset<=endOffset) {
				String token = FWParsingTools.getToken(content, offset);
				FWScope cScope = commentScopes.getScope(offset);
				FWScope sScope = stringScopes.getScope(offset);
				if (cScope !=null) 
					offset = cScope.getEnd();
				else if (sScope !=null) 
					offset = sScope.getEnd(); 
				else {
					FWParsingTools.getToken(content, offset);
					if (!token.contains("$")) // do not paint list
						setCharacterAttributes(offset, len, badStyle, false);
					offset += len+1;
				}
				offset = content.indexOf(str, offset);
			}
		}
	}
	
    @Override
	protected void paintToken(String content, String token, int startOffset, int tokenLength) {
		super.paintToken(content, token, startOffset, tokenLength);
		FWScopes loopScopes = loopScopesTable.get(token);
		if (loopScopes !=null && loopScopes.getScope(startOffset) != null)
			setCharacterAttributes(startOffset, tokenLength, keywordManager.getLoopVariablesAttributeSet(), false);
	}
    
	@Override
	public boolean highlight(int offset, int len, boolean err) {
		GTTextPane textPane = (GTTextPane) getTextPane();
		textPane.highlight(offset, len, err);
		if (!textPane.isFocusOwner())
			textPane.autoScrollTo(offset, len);
		return true;
	}
	
	@Override
	public Window getTopLevelAncestor() {
		try {
			return  (JFrame) getTextPane().getTopLevelAncestor();
		} catch (ClassCastException ex) {
			return null;
		}
	}
	
	private void parseLoopScopes(SourceLocalization loc) {
		String content = loc.getRawText();

		int offset = FWParsingTools.indexOfToken(content, forEachToken, 0);
		if (offset<0)
			return;
		
		int len = content.length();
		while (offset>= 0 && offset < len) {
			try {
				offset = parseLoopScope(loc.getSubLocalization(offset));
			} catch (GTException ex) {
				warnings.push(ex);
				if (warnings.size()>1) {
					ex.displayTransientWindow();
					warnings.clear();
				}
				return;
			} catch (StringIndexOutOfBoundsException ex) {
				ex.printStackTrace();
				offset++;
			}
			offset = FWParsingTools.indexOfToken(content, forEachToken, offset);
		}
	}

	private int parseLoopScope(SourceLocalization loc) throws GTException {
		Vector<SourceLocalization> subLocs = GTCommandParser.parse7FirstScopes(loc);
		if (subLocs.size()<2)
			return -1;

		SourceLocalization varLoc = subLocs.get(1); // variable name
		if (varLoc.getText().equals("?")) // avoid error on "for_each ?"
			return varLoc.getOffset();
		
		String content = loc.getText();
		int endLineIdx = content.indexOf("\n");
		if (endLineIdx>0 && varLoc.getOffset()-loc.getOffset()>endLineIdx)
			return loc.getOffset()+endLineIdx;
		
		
		keywordManager.testValidity(varLoc);
		
		String varName = varLoc.getText();
		addLoopScope(varName, varLoc.getOffset(), varName.length()); // add variable scope
		
		if (subLocs.size()<5)
			return varLoc.getOffset();

		SourceLocalization loc2 = subLocs.get(2);
		String token = loc2.getText();
		boolean inList = token.equals(inListToken); // in : for_each el ***in*** L bundleAt4
		boolean inRange = token.equals(fromToken); // from : for_each i ***from*** 1 to 10 bundleAt6
		if ( !(inList || inRange))
			return loc2.getOffset();
		
		SourceLocalization loc4 = subLocs.get(4);
		SourceLocalization bundle;
		
		if (inRange && loc4.getText().equals(toToken)) { // from : for_each i from 1 ***to*** 10 bundleAt6
			if (subLocs.size()<7)
				return loc2.getOffset();
			bundle = subLocs.get(6);
		} else if (inList)
			bundle = loc4;
		else
			return loc2.getOffset();

		int bundleStart = bundle.getOffset();
		int bundleLen = bundle.getLength();
		addLoopScope(varName, bundleStart, bundleLen);
		parseLoopScopes(bundle); // parse internal loops

		return bundleStart+bundleLen;
	}
	
	private void addLoopScope(String token, int start, int len) {
		FWScopes scopes = loopScopesTable.get(token);
		if (scopes == null) {
			scopes = new FWScopes();
			loopScopesTable.put(token, scopes);
		}
		scopes.addScope(new FWScope(start, start+len));
	}
	

	public void replace(String target, String replacement, boolean smart) {
		JTextPane textPane = getTextPane();
		int caretLocation = textPane.getCaretPosition();
		if (smart)
			setText(FWParsingTools.replaceTokens(getText(),
					new String[] { target }, new String[] { replacement }));
		else
			setText(getText().replace(target, replacement));
		if (caretLocation > getLength())
			caretLocation = getLength();
		textPane.setCaretPosition(caretLocation);
	}

	@Override
	public String getText(int offset, int length) {
		try {
			return super.getText(offset, length);
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
		return "";
	}

}