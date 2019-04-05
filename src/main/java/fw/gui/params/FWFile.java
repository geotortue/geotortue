/**
 * 
 */
package fw.gui.params;

import java.io.File;

import javax.swing.JPanel;

import fw.app.FWManager;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;

public class FWFile extends FWParameter<File, JPanel> {

	public FWFile(String tag) {
		super(tag, null);
		setValue(FWManager.getUserDirectory());
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

	@Deprecated
	public JPanel getComponent() {
		new Exception("do not invoke this method").printStackTrace();
		return null;
	}

	@Override
	protected boolean setValue(File v) {
		boolean b = super.setValue(v);
		if (b)
			v.mkdirs();
		return b;
	}
	
	
}
