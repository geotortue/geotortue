package fw.files;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.swing.text.html.StyleSheet;

import fw.files.FileUtilities.HTTPException;


public final class CSSFile extends TextFile {

	public CSSFile(URL url) throws IOException, HTTPException  {
		super(url);
	}
	
	public StyleSheet getStyleSheet(){
		StringReader reader = new StringReader(content);
		StyleSheet styles = new StyleSheet();
		 
		 try {
			styles.loadRules(reader, null);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return styles;
	}

}
