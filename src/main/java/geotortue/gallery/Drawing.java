package geotortue.gallery;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import fw.app.FWConsole;
import fw.files.FWFileWriter;
import fw.files.FileUtilities;
import fw.gui.FWImagePane;
import fw.gui.layout.HorizontalFlowLayout;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;

public class Drawing implements XMLCapabilities {

	static final XMLTagged XML_TAG = XMLTagged.Factory.create("Drawing");
	private final File path;
	private File file;
	private BufferedImage img;
	private final JTextArea commentsEditor = new JTextArea("");
	private String fileName;
	
	Drawing(BufferedImage im, File path) throws DrawingIOException {
		this.path = path;
		this.img = new BufferedImage(im.getWidth(), im.getHeight(), im.getType()); 
		img.setData(im.getRaster()); // work with a copy
		this.file = FileUtilities.getNewFile(path, "img-%%%%.png");
		try {
			ImageIO.write(img, "png", file);
		} catch (IOException ex) {
			throw new DrawingIOException(ex, file);
		}
		this.fileName = file.getName();
	}
	
	Drawing(XMLReader e, File path) throws DrawingIOException {
		this.path = path;
		loadXMLProperties(e);
		if (file == null)
			throw new DrawingIOException("XML issue");
		
		try {
			this.img = ImageIO.read(file);
		} catch (IOException ex) {
			throw new DrawingIOException(ex, file);
		}
		this.fileName = file.getName();
	}
	
	Drawing(File file, File path) throws DrawingIOException {
		this.path = path;
		try {
			this.file = file;
			this.img = ImageIO.read(file);
		} catch (IOException ex) {
			throw new DrawingIOException(ex, file);
		}
		this.fileName = file.getName();
	}
	
	String getDate() {
		return new SimpleDateFormat("dd/MM/yy").format(new Date(file.lastModified()));
	}


	BufferedImage getImage(){
		return img;
	}

	
	private String getComments() {
		return commentsEditor.getText();
	}
	
	private String getHTMLComments() {
		return getComments().replace("\n", "<br/>");
	}
	
	public static class DrawingIOException extends IOException {
		private static final long serialVersionUID = -9142194987611245748L;

		public DrawingIOException(IOException ex, File f) {
			super(ex.getMessage()+" ("+f+")", ex);
			
		}
		
		public DrawingIOException(String msg) {
			super(msg);
		}
	}
	

	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return XML_TAG.getXMLTag();
	}

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.setAttribute("filename", fileName);
		e.setContent(getComments());
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		try {
			commentsEditor.setText(child.getContent());
		} catch (XMLException ex) {
			FWConsole.printWarning(this, ex.getMessage());
		}
		try {
			this.file = new File(path, child.getAttribute("filename"));
		} catch (XMLException ex) {
			this.file = null;
		}
		return child;
	}
	
	/*
	 * Cell
	 */
	
	private JPanel cell;
	
	void createCell() {
		if (cell != null)
			return;
		FWImagePane imgPane = new FWImagePane(getScaledInstance(img, 160, 120));
		JLabel label = new JLabel("<html>"+getDate()+"<br/>"+img.getWidth()+" x "+img.getHeight()+" pixels</html>");
		label.setBackground(Color.WHITE);
		cell = HorizontalFlowLayout.createPanel(10, imgPane, label);
	}
	
	JPanel getCell(){
		if (cell == null)
			createCell();
		String htmlComments = getHTMLComments();
		if (htmlComments.length()>30)
			htmlComments = htmlComments.substring(0, 30)+" (...)";
		cell.setToolTipText("<html>"+fileName+"<br/>"+htmlComments+"</html>");
		return cell;
	}
	
	JTextArea getCommentsEditor() {
		return commentsEditor;
	}
	
	/**
	 * 
	 */
	void delete() {
		file.delete();
		if (htmlDrawing != null)
			htmlDrawing.delete();
	}
	
	static BufferedImage getScaledInstance(BufferedImage img, int fw, int fh) {
		int w = img.getWidth();
		int h = img.getHeight();
		int tw, th, dx, dy;
		if (3*w >= 4*h) {
			tw = fw;
			th = (h * fw) / w;
			dx = 0;
			dy = (fh - th)/2;
		} else  {
			tw = (w * fh) / h;
			th = fh;
			dx = (fw - tw)/2;
			dy = 0;
		}
		
		BufferedImage fImg = new BufferedImage(fw, fh, BufferedImage.TYPE_INT_RGB);
		Graphics g = fImg.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, fw, fh);
		g.drawImage(img.getScaledInstance(tw, th, Image.SCALE_SMOOTH), dx, dy, null);
		return fImg;
	}
	
	class HTMLDrawing {
		
		private final String thumbFileName, htmlFileName;
		private final File thumbFile, htmlFile;
		
		/**
		 * @param d
		 * @param thumbsPath
		 * @param htmlPath
		 * @throws IOException 
		 */
		HTMLDrawing(File thumbsPath, File htmlPath) throws IOException {
			this.thumbFileName = FileUtilities.getStem(fileName)+".thumb.png";
			this.htmlFileName = FileUtilities.getStem(fileName)+".html";
			this.thumbFile = new File(thumbsPath, thumbFileName);
			this.htmlFile = new File(htmlPath, htmlFileName);
			createThumbnail(img);
		}
		
		/**
		 * 
		 */
		private void delete() {
			thumbFile.delete();
			htmlFile.delete();
		}

		/**
		 * @return
		 */
		String getHTMLComments() {
			return Drawing.this.getHTMLComments();
		}

		/**
		 * @return
		 */
		String getHTMLCode() {
			return "<a href=\""+ htmlFileName + "\"><img class=\"thumbs\" src=\"./thumbs/" + thumbFileName + "\"/></a>";
		}

		private void createThumbnail(BufferedImage img) throws IOException {
			if (thumbFile.exists())
				return;
			BufferedImage thumb = new BufferedImage(240, 180, BufferedImage.TYPE_INT_RGB);
			int w = img.getWidth();
			int h = img.getHeight();
			int tw, th, dx, dy;
			if (3*w >= 4*h) {
				tw = 240;
				th = (h * 240) / w;
				dx = 0;
				dy = (180-th)/2;
			} else  {
				tw = (w * 180) / h;
				th = 180;
				dx = (240-tw)/2;
				dy = 0;
			}
			
			Graphics g = thumb.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, 240, 180);
			g.drawImage(img.getScaledInstance(tw, th, Image.SCALE_SMOOTH), dx, dy, null);
			ImageIO.write(Drawing.getScaledInstance(img, 240, 180), "png", thumbFile);
		}

		/**
		 * @return
		 */
		public String getFileName() {
			return fileName;
		}

		/**
		 * @return
		 */
		public String getHtmlFileName() {
			return htmlFileName;
		}

		/**
		 * @return
		 */
		public File getHtmlFile() {
			return htmlFile;
		}
	}

	private HTMLDrawing htmlDrawing;

	/**
	 * @param htmlPath
	 * @param thumbsPath
	 * @return
	 * @throws IOException 
	 */
	public HTMLDrawing getHTMLDrawing(File htmlPath, File thumbsPath) throws IOException {
		if (htmlDrawing == null)
			htmlDrawing = new HTMLDrawing(thumbsPath, htmlPath);
		return htmlDrawing;
	}

	/**
	 * @param writer
	 * @throws IOException 
	 */
	public void writeImage(FWFileWriter writer) throws IOException {
		writer.writeImage(img, fileName);		
	}

}