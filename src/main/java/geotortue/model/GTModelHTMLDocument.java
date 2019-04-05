/**
 * 
 */
package geotortue.model;

import java.awt.Color;

import javax.swing.text.Element;

import fw.text.FWHTMLCommentScopeParser;
import fw.text.FWStylesManager;
import fw.text.FWSyntaxDocument;
import fw.text.TextStyle;
import geotortue.core.KeywordManager;

/**
 *
 */
public class GTModelHTMLDocument extends FWSyntaxDocument {
	
	private static final long serialVersionUID = -4475677434135069815L;
	
	private static final String[] HTML_TAGS = new String[]{
			"html", "head", "body", "p", "b", "br", "i", "h1", "h2", "h3", "h4", "h5", "h6", "span", "table", "td", "tr", "li", 
			"center", "strike", "small", "small", "sub", "sup", "ol", "ul", "a"};
	
	private static final TextStyle HTML_STYLE = new TextStyle(new Color(0x990011));
	
	private static final  FWStylesManager STYLES = new FWStylesManager(
			KeywordManager.DEFAULT_STYLE,
			KeywordManager.COMMENTS_STYLE,
			KeywordManager.STRINGS_STYLE) {

				@Override
				public void updateFont() {
					super.updateFont();
					HTML_STYLE.setFontFamily(KeywordManager.DEFAULT_STYLE.getFontFamily());
					HTML_STYLE.setFontSize(KeywordManager.DEFAULT_STYLE.getFontSize());
				}
		
	};

	public GTModelHTMLDocument() {
		super(STYLES, new FWHTMLCommentScopeParser());
		HTML_STYLE.setBold(true);
		STYLES.updateFont();
	}
	
	protected void customHighlight(String content, int startOffset, int endOffset) {
		Element root = getDefaultRootElement();
		int startLine = root.getElementIndex(startOffset);
		int start = root.getElement(startLine).getStartOffset();
		int endLine = root.getElementIndex(endOffset);
		int end = root.getElement(endLine).getEndOffset();
		highlightHtmlTags(content, start, end);
	}


	private void highlightHtmlTags(String content, int startOffset, int endOffset) {
		TextStyle style = HTML_STYLE;
		for (String key : HTML_TAGS) {
			int idx = content.indexOf("<"+key, startOffset);
			while (idx >= 0) {
				int idx1 = content.indexOf("<"+key+">", idx);
				int tokenLength = key.length();
				int newIdx = content.indexOf("<"+key, idx + tokenLength);
				if (idx==idx1) { // opening tag
					tokenLength += 2;
					setCharacterAttributes(idx, tokenLength, style, false);
				} else {
					tokenLength += 1;
					setCharacterAttributes(idx, tokenLength, style, false);
					int cidx = content.indexOf(">", idx);
					if (cidx>=0) {
						setCharacterAttributes(cidx, 1, style, false);
						newIdx = content.indexOf(key, cidx);
					}
				}
				idx = newIdx; 
				
			}
			
			idx = content.indexOf("</"+key+">", startOffset); // closing tag
			while (idx >= 0) {
				int tokenLength = key.length()+3;
				setCharacterAttributes(idx, tokenLength, style, false);
				idx = content.indexOf("</"+key+">", idx+tokenLength); 
			}
		}
		
	}
}
