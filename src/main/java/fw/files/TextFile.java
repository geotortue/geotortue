package fw.files;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import fw.files.FileUtilities.HTTPException;
import fw.text.FWParsingTools;


public class TextFile {
	
	protected String content="";
	
	protected TextFile(File f) throws IOException {
		open(f.toURI().toURL());
	}
	
	public TextFile(URL url) throws HTTPException, IOException {
		if (url.getProtocol().equals("http"))
			openHTTP(url);
		else
			open(url);
	}
	
	private void open(URL url) throws IOException {
		InputStream is = null;
		InputStreamReader reader = null;
		try {
			is = url.openStream();
	        reader = new InputStreamReader(is, "UTF-8");
	        
	        int c;
	        while ((c = reader.read()) != -1)
	        	content += (char) c;
		} finally {
			if (reader != null)
				reader.close();
		}
    }
	
	/*
	 * Open a text file from a http url.
	 */
	private void openHTTP(URL url) throws HTTPException, IOException {
		InputStreamReader reader = FileUtilities.getInputStream(url);
		
		try {
	        int c;
	        while ((c = reader.read()) != -1)
	        	content += (char) c;
		} finally {
			reader.close();
		}
    }
    
    public TextFile(String text){
    	this.content = text;
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
        content=FWParsingTools.removeJavaComments(content);
    }
    
    protected void removeHTMLComments() {
        content=FWParsingTools.removeHTMLComments(content);
    }

    public String getText() {
		return ""+content;
	}

    public void write(File file) throws IOException {
		FileWriter writer = new FileWriter(file);
		writer.write(content);
		writer.close();
	}
}