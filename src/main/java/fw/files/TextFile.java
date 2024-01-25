package fw.files;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import fw.files.FileUtilities.HTTPException;
import fw.text.FWParsingTools;


public class TextFile {
	
	protected String content = "";
	
	public TextFile(final String text){
    	this.content = text;
    }
	
	protected TextFile(final File f) throws IOException {
		open(f.toURI().toURL());
	}

	public TextFile(final URL url) throws HTTPException, IOException {
		if (url.getProtocol().equals("http")) {
			openHTTP(url);
		}
		else {
			open(url);
		}
	}
	
	private void open(final URL url) throws IOException {
		try (InputStream is = url.openStream();
		InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
	        int c;
	        while ((c = reader.read()) != -1)
	        	content += (char) c;
		}
    }
	
	/*
	 * Open a text file from a http url.
	 */
	private void openHTTP(URL url) throws HTTPException, IOException {
		try (InputStreamReader reader = FileUtilities.getInputStream(url)) {
	        int c;
	        while ((c = reader.read()) != -1)
	        	content += (char) c;
		}
    }
        
    /**
     * Splits the content of this instance according to regular expression regExp 
     * and removes empty pieces.  
     * 
     * @param str
     * @param regExp
     * @return
     */
    protected String[] splitContent(String regExp) {
    	return FWParsingTools.split(content, regExp);
    }

    /**
     * Remove java styled comments.
     *
     */
    protected void removeJavaComments() {
        content = FWParsingTools.removeJavaComments(content);
    }
    
    protected void removeHTMLComments() {
        content = FWParsingTools.removeHTMLComments(content);
    }

    public String getText() {
		return "" + content;
	}

    public void write(File file) throws IOException {
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(content);
		}
	}
}