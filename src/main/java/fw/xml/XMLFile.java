package fw.xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import fw.files.FileUtilities.HTTPException;
import fw.files.TextFile;

public class XMLFile extends TextFile {

	public XMLFile(URL url) throws HTTPException, IOException {
		super(url);
	}
	
	public XMLFile(File f) throws IOException {
		super(f);
	}
	
	public XMLFile(String text){
		super(text);
	}
	
	
	public XMLFile(XMLCapabilities c) {
		super(c.getXMLProperties().getXMLCode());
	}
	
	public XMLFile(XMLWriter e) {
		super(e.getXMLCode());
	}
	
	public XMLReader parse() throws XMLException {
		removePrologue();
		removeHTMLComments();
		return XMLEntry.parse(content);
	}
	
	private void removePrologue() throws XMLException {
		if (!content.startsWith("<?"))
				throw new XMLException("Prologue missing");
		int idx = content.indexOf("?>");
		content = content.substring(idx+2);
	}
}
