/**
 * 
 */
package geotortue.gallery;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import fw.files.FileUtilities;
import fw.files.FileUtilities.HTTPException;
import fw.files.HTMLFile;
import geotortue.gallery.Drawing.HTMLDrawing;

/**
 * @author Salvatore Tummarello
 *
 */
public class HTMLFactory {
	
	private final File htmlPath, thumbsPath;
	private final Vector<HTMLDrawing> htmlDrawings = new Vector<HTMLDrawing>();

	HTMLFactory(File path, Vector<Drawing> drawings) throws IOException{
		this.htmlPath = new File(path, "html");
		this.thumbsPath = new File(htmlPath, "thumbs");
		
		htmlPath.mkdirs();
		thumbsPath.mkdirs();
		
		for (Drawing drawing : drawings) 
			htmlDrawings.add(drawing.getHTMLDrawing(htmlPath, thumbsPath));
	}
	

	URL compileHTMLFiles() throws IOException, HTTPException {
		FileUtilities.copy(getClass().getResource("/cfg/html/style.css"), new File(htmlPath, "style.css"));
		FileUtilities.copy(getClass().getResource("/cfg/icon.png"), new File(htmlPath, "icon.png"));
		FileUtilities.copy(getClass().getResource("/cfg/html/icon-forward.png"), new File(htmlPath, "icon-forward.png"));
		FileUtilities.copy(getClass().getResource("/cfg/html/icon-back.png"), new File(htmlPath, "icon-back.png"));
		FileUtilities.copy(getClass().getResource("/cfg/html/tortue-v4.png"), new File(htmlPath, "tortue.png"));
		FileUtilities.copy(getClass().getResource("/cfg/html/header.png"), new File(htmlPath, "header.png"));

		
		
		for (int idx = 0; idx < htmlDrawings.size(); idx++)
			writeHTMLFile(idx);
		
		File index = new File(htmlPath, "index.html");
	
		HTMLFile htmlWriter = getHTMLWriter(-1);	
		
		String thumbs = "";
		for (HTMLDrawing d : htmlDrawings)
			thumbs += "\t"+d.getHTMLCode()+"\n";
		
		htmlWriter.appendBlock("main", thumbs);
		appendFooter(htmlWriter);
		htmlWriter.write(index);
		
		return index.toURI().toURL();
	}

	private HTMLFile getHTMLWriter(int idx){
		HTMLFile htmlWriter = new HTMLFile("G&eacute;otortue",
				"style.css", 
				"GéoTortue est un logiciel libre pour enseigner les mathématiques et la programmation, de l'école maternelle à l'université", 
				"icon.png");
		
		
		htmlWriter.appendBlock("header", "<a href=\"http://geotortue.free.fr/index.php\"><img src=\"header.png\"/></a>");
		htmlWriter.append("<br clear=\"all\"/>\n");
		if (idx<0) {
			htmlWriter.appendBlock("splash", "<img src=\"tortue.png\"/>");
			return htmlWriter;
		}
		
		htmlWriter.append("<div class=\"splash\"><img src=\"tortue.png\"/><br/>");
		String navigation = "<table class=\"navigation\"><tr>\n";
		if (idx>0) {
			HTMLDrawing d0 = htmlDrawings.get(idx-1);
			navigation += "<td>\n<a href=\"" + d0.getHtmlFileName() + "\"><img src=\"./icon-back.png\"/></a></td>\n";
		} else
			navigation += "<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>";
		
		navigation += "<td>&nbsp;" + (idx+1) +"&nbsp;/&nbsp;" + htmlDrawings.size()+"&nbsp;</td>";
		if (idx<htmlDrawings.size()-1) {
			HTMLDrawing d1 = htmlDrawings.get(idx+1);
			navigation += "<td><a href=\"" + d1.getHtmlFileName() + "\"><img src=\"./icon-forward.png\"/></a></td>\n";
		}
		navigation += "</tr>";
		navigation += "<tr><td></td><td>&nbsp;<a href=\"index.html\">Index</td><td></td></a></tr>";
		navigation += "</table>";
		htmlWriter.append(navigation);
		htmlWriter.append("</div>");
		return htmlWriter;
	}
	
	private void appendFooter(HTMLFile htmlWriter) {
		htmlWriter.append("<br clear=\"all\"/><br/>");
		htmlWriter.appendBlock("footer" , "Pinacoth&egrave;que r&eacute;alis&eacute;e avec <a href=\"http://geotortue.free.fr\">G&eacute;oTortue</a>.");
	}
	
	private void writeHTMLFile(int idx) throws IOException {
		HTMLDrawing d = htmlDrawings.get(idx);
		HTMLFile htmlWriter = getHTMLWriter(idx);
		String content = "<img class=\"image\" src=\"../" + d.getFileName() + "\"/>";
		String comments = d.getHTMLComments();
		if (comments.length()>0)
			content +="<div class=\"comments\">"+comments+"</div>";
		htmlWriter.appendBlock("main",  content);
		appendFooter(htmlWriter);
		htmlWriter.write(d.getHtmlFile());
	}
}
