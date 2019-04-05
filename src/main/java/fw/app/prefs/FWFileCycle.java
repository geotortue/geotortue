/**
 * 
 */
package fw.app.prefs;

import java.io.File;
import java.util.Vector;

import fw.xml.XMLCapabilities;

public class FWFileCycle {
	
	private final FWFileEntry[] files;
	private final int len = 8;
	
	public FWFileCycle(XMLCapabilities parent) {
		this.files = new FWFileEntry[len];
		for (int idx = 0; idx < len; idx++) 
			files[idx] = new FWFileEntry(parent, "last."+(idx+1));
	}

	public void put(File f) {
		int match = len-1;
		for (int idx = 0; idx < len; idx++) {
			if (files[idx].getValue() == f) {
				match = idx;
				break;
			}
		}
		
		for (int idx = match; idx > 0; idx--) 
			files[idx].setValue(files[idx-1].getValue());
		files[0].setValue(f);
	}

	public Vector<File> getFiles() {
		Vector<File> fv = new Vector<>();
		
		for (int idx = 0; idx < len; idx++) {
			File f = files[idx].getValue();
			if (f==null || !f.exists() || !f.isFile())
				return fv;
			fv.add(f);
		}
		return fv;
	}
}
