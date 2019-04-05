package geotortue.gallery;

import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import fw.app.FWConsole;
import fw.app.Translator.TKey;
import fw.app.prefs.FWWritableDirectoryEntry;
import fw.app.prefs.FWWritableDirectoryEntry.UninitializedDirectoryException;
import fw.files.FWFileReader;
import fw.files.FWFileWriter;
import fw.files.FileUtilities.HTTPException;
import fw.files.NoMoreEntryAvailableException;
import fw.gui.FWLabel;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalFlowLayout;
import fw.gui.params.FWParameterListener;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLFile;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.gallery.Drawing.DrawingIOException;


public class Gallery implements XMLCapabilities, FWSettings {

	/**
	 * 
	 */

	private static final TKey NAME = new TKey(Gallery.class, "settings");

	@Override
	public TKey getTitle() {
		return NAME;
	}


	private static final TKey DIRECTORY = new TKey(Gallery.class, "directory");

	private final FWWritableDirectoryEntry galleryPath;
	
	private File xmlIndexFile;

	private HTMLFactory htmlFactory;

	private final Vector<Drawing> drawings = new Vector<Drawing>();
	
	private static final OPTKey CHOOSE_DIR_KEY = new OPTKey(Gallery.class, "chooseDirectory");
	
	
	public Gallery(Window owner) {
		this.galleryPath = new FWWritableDirectoryEntry(owner, "Gallery", CHOOSE_DIR_KEY);
		try {
			setPath(galleryPath.getValueSafely());
		} catch (UninitializedDirectoryException ex) {
		}
	}
	
	private File getPath() throws UninitializedDirectoryException {
		setPath(galleryPath.askForValue());
		
		return galleryPath.getValueSafely();
	}
	
	private boolean setPath(File path) {
		if (path != null)
			path.mkdirs();
		
		xmlIndexFile = new File(path, "index.xml");

		drawings.clear();
		
		if (xmlIndexFile.exists())
			try {
				loadXMLProperties(new XMLFile(xmlIndexFile).parse());
			} catch (XMLException | IOException ex) {
				ex.printStackTrace();
			}
		else {
			loadFiles(path);
			updateXMLIndex();
		}
		return true;
	}
	
	private void loadFiles(final File path) {
		new Thread() {

			@Override
			public void run() {
				File[] files = path.listFiles(new FileFilter() {
					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".png");
					}
				});
				for (int idx = 0; idx < files.length; idx++)
					try {
						drawings.add(new Drawing(files[idx], path));
					} catch (DrawingIOException ex) {
						FWConsole.printWarning(this, ex.toString());
					}
			}
			
		}.start();
	
	}
	
	void updateXMLIndex() {
		try {
			new XMLFile(getXMLProperties()).write(xmlIndexFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public boolean isEmpty() {
		return drawings.isEmpty();
	}
	
	/*
	 * I/O
	 */
	public void openArchive(File f) throws IOException, NoMoreEntryAvailableException, XMLException, UninitializedDirectoryException {
		// TODO : les fichiers portent le même nom
		FWFileReader reader = new FWFileReader(f);
		XMLReader e =  new XMLFile(reader.readText("gallery.xml")).parse();
		reader.unzipNextEntries(getPath());
		
		loadXMLProperties(e);
		updateXMLIndex();
	}
	
	public void writeArchive(File f) throws IOException {
		FWFileWriter writer = new FWFileWriter(f);
		try {
			writer.writeXML(this, "gallery.xml");
		} catch (XMLException ex) {
			ex.printStackTrace();
		}
		
		for (Drawing d : drawings)
			d.writeImage(writer);

		writer.close();
	}
	
	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return "Gallery";
	}

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		for (Drawing d : drawings)
			e.put(d);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		while (child.hasChild(Drawing.XML_TAG))
			try {
				drawings.add(new Drawing(child, galleryPath.getValueSafely()));
			} catch (DrawingIOException | UninitializedDirectoryException ex) {
				FWConsole.printWarning(this, ex.toString());
				return child;
			}
		updateXMLIndex();
		return child;
	}
	
	/*
	 * FWS
	 */


	@Override
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		FWParameterListener<File> l= new FWParameterListener<File>() {
			@Override
			public void settingsChanged(File value) {
				setPath(value);
			}
		};
		return VerticalFlowLayout.createPanel(new FWLabel(DIRECTORY, SwingConstants.LEFT), galleryPath.getComponent(l));
	}
	
	/*
	 * Drawings Manipulation (delegate to drawings vector)
	 */
	
	public void add(BufferedImage img) throws DrawingIOException, UninitializedDirectoryException {
		drawings.add(new Drawing(img, getPath()));
		updateXMLIndex();
	}
	
	Drawing getDrawingAt(int idx) {
		return drawings.elementAt(idx);
	}

	public int getSize() {
		return drawings.size();
	}
	
	void remove(Drawing d) {
		drawings.remove(d);
		d.delete();
		updateXMLIndex();
	}
	
	void moveDrawingAt(Drawing d, int index) {
		drawings.remove(d);
		drawings.insertElementAt(d, index);
		updateXMLIndex();
	}
	
	public void createCells() {
		for (Drawing d : drawings)
			d.createCell();
	}

	/**
	 * @return
	 * @throws HTTPException 
	 * @throws IOException 
	 * @throws UninitializedDirectoryException 
	 */
	public URL compileHTMLFiles() throws IOException, HTTPException, UninitializedDirectoryException {
		this.htmlFactory = new HTMLFactory(getPath(), drawings);
		return htmlFactory.compileHTMLFiles();
	}
}