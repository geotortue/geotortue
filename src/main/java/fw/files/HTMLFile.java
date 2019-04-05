package fw.files;

import java.io.File;
import java.io.IOException;



public class HTMLFile extends TextFile {
	
	public HTMLFile(String title, String cssPath, String description, String iconPath){
		super("");
		content += "<head><link href=\""+cssPath+"\" rel=\"stylesheet\" type=\"text/css\">\n";
		content += "<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\">\n";
		content += "<meta name=\"Description\" content=\""+description+"\">\n";
		content += "<title>" + title + "</title>\n";
		content += "<link rel=\"icon\" href=\""+ iconPath +"\">\n";
		content += "</head>\n\n<body>\n";
	}
	
	@Override
	public void write(File file) throws IOException {
		content+="\n</body>\n</html>";
		super.write(file);
	}

	public void append(String text){
		if (text.length()==0)
			return;
		content+=text+"\n";
	}
	
	public void appendBlock(String divClass, String text){
		if (text.length()==0)
			return;
		content+="\n<div class=\""+divClass+"\">";
		content+="\n"+text+"\n";
		content+="</div>\n";
	}
	
	public void appendParagraph(String pClass, String text) {
		if (text.length()==0)
			return;
		content+="\n<p class=\""+pClass+"\">";
		content+="\n"+text+"\n";
		content+="</p>\n";
	}
}
