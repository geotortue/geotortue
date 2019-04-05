package fw.text;

import java.awt.Color;
import java.awt.Font;

import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;



public class TextStyle extends SimpleAttributeSet implements XMLCapabilities {

	private static final long serialVersionUID = 3766326646717187310L;

	public static final int PLAIN = Font.PLAIN; // 0
	public static final int BOLD = Font.BOLD; // 1
	public static final int ITALIC = Font.ITALIC; //2
	public static final int BOLD_ITALIC = 3;// 
	
	public TextStyle(Color c){
		Font f = UIManager.getFont("FWFont");
		StyleConstants.setFontFamily(this, f.getFamily());
		StyleConstants.setFontSize(this, f.getSize());
		StyleConstants.setForeground(this, c);
	}
	
	public TextStyle(){
		this(Color.BLACK);
	}
	
	public Color getForeground(){
		return StyleConstants.getForeground(this);
	}

	public void setBackground(Color c){
		StyleConstants.setBackground(this, c);
	}

	
	public Font getFont(){
		int style = 0;
		if (StyleConstants.isBold(this))
			style+=1;
		if (StyleConstants.isItalic(this))
			style+=2;

		return new Font(StyleConstants.getFontFamily(this), style,
				StyleConstants.getFontSize(this));
	}
	
	public Font deriveFont(int style, int dsize){
		return new Font(StyleConstants.getFontFamily(this),
				style,
				StyleConstants.getFontSize(this)+dsize);
	}

	public String getFontFamily(){
		return StyleConstants.getFontFamily(this);
	}
	
	public void setFontFamily(String f){
		StyleConstants.setFontFamily(this, f);
	}
	
	public int getFontSize(){
		return StyleConstants.getFontSize(this);
	}

	public void setFontSize(int size){
		StyleConstants.setFontSize(this, size);
	}

	public void setStyle(int style){
		boolean italic = false;
		boolean bold = false;
		switch (style) {
		case PLAIN:
			break;
		case ITALIC:
			italic = true;
			break;
		case BOLD:
			bold = true;
			break;
		case BOLD_ITALIC:
			italic = true;
			bold = true;
			break;
		default:
			break;
		}
		
		StyleConstants.setItalic(this, italic);
		StyleConstants.setBold(this, bold);
	}
	
	public boolean isItalic(){
		return StyleConstants.isItalic(this);
	}
	
	public boolean isBold(){
		return StyleConstants.isBold(this);
	}
	

	public void setItalic(boolean b){
		StyleConstants.setItalic(this, b);
	}

	public void setBold(boolean b){
		StyleConstants.setBold(this, b);
	}
	
	
	
	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return "TextStyle";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.setAttribute("family", getFontFamily());
		e.setAttribute("size", getFontSize());
		return e;
	}
	
	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		String fam = child.getAttribute("family", getFontFamily());
		setFontFamily(fam);
		int size = child.getAttributeAsInteger("size", getFontSize());
		setFontSize(size);
		return child;
	}
}