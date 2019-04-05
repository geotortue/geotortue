/**
 * 
 */
package fw.text;

/**
 * @author Salvatore Tummarello
 *
 */
public class FWStylesManager {
	
	private final TextStyle DEFAULT_STYLE;
	private final TextStyle COMMENTS_STYLE; 
	private final TextStyle STRINGS_STYLE; 
	private TextStyle[] styles;
	
	public FWStylesManager(TextStyle def, TextStyle comments, TextStyle strings, TextStyle... styles) {
		this.DEFAULT_STYLE = def;
		this.COMMENTS_STYLE = comments;
		this.STRINGS_STYLE = strings;
		this.styles = styles;

		COMMENTS_STYLE.setStyle(TextStyle.ITALIC);
	}

	public TextStyle getDefaultStyle() {
		return DEFAULT_STYLE;
	}

	public TextStyle getCommentsStyle() {
		return COMMENTS_STYLE;
	}
	
	public TextStyle getStringStyle() {
		return STRINGS_STYLE;
	}


	public void updateFont() {
		String family = DEFAULT_STYLE.getFontFamily();
		int size = DEFAULT_STYLE.getFontSize();

		COMMENTS_STYLE.setFontFamily(family);
		COMMENTS_STYLE.setFontSize(size);
		
		STRINGS_STYLE.setFontFamily(family);
		STRINGS_STYLE.setFontSize(size);
		
		for (TextStyle s : styles) {
			s.setFontFamily(family);
			s.setFontSize(size);
		}

	}

	private void setFontSize(int size) {
		if (size<6 || size>60)
			return;
		DEFAULT_STYLE.setFontSize(size);
		COMMENTS_STYLE.setFontSize(size);
		STRINGS_STYLE.setFontSize(size);
		for (TextStyle s : styles)
			s.setFontSize(size);
	}
	
	public void increaseFontSize() {
		int s = DEFAULT_STYLE.getFontSize()+1;
		setFontSize(s);
	}

	public void decreaseFontSize() {
		int s = DEFAULT_STYLE.getFontSize()-1;
		setFontSize(s);
	}
}
