/**
 * 
 */
package fw.gui.params;

import java.awt.Window;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import fw.app.FWManager;
import fw.app.Translator.TKey;
import fw.gui.FWButton.BKey;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;

public class FWDirectory extends FWParameter<File, JPanel> {
	
	private static final TKey SELECT = new TKey(FWDirectory.class, "select");
	protected static final BKey SELECT_FOLDER = new BKey(FWDirectory.class, "selectFolder");


	public FWDirectory(String xmlTag) {
		super(xmlTag, FWManager.getUserDirectory());
	}

	@Override
	public void fetchValue(XMLReader child, File def) {
		String path = child.getAttribute(getXMLTag(), def.getAbsolutePath());
		setValue(new File(path));
	}

	@Override
	public void storeValue(XMLWriter e) {
		e.setAttribute(getXMLTag(), getValue().getAbsolutePath());
	}

	public File openFileChooser(Window owner)  {
		JFileChooser chooser = new JFileChooser(getValue());
		chooser.setDialogTitle(SELECT_FOLDER.translate());
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showDialog(owner, SELECT.translate()) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file!=null && setValue(file))
				return file;
		}
		return null;
	}
	
	@Override
	protected boolean setValue(File v) {
		File f;
		if (v.isDirectory()) 
			f = v;
		else if (v.isFile()) 
			f = v.getParentFile(); 
		else 
			return false;
			
		return super.setValue(f);
	}

	@Override @Deprecated
	public JPanel getComponent() {
		// don't call this
		return null;
	}
	
	
}