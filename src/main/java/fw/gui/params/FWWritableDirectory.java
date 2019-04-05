/**
 * 
 */
package fw.gui.params;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;

import fw.gui.FWButton;
import fw.gui.FWButton.FWButtonListener;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.OPTKey;

/**
 * @author Salvatore Tummarello
 *
 */
public class FWWritableDirectory extends FWDirectory {
	

	private static final OPTKey INVALID_DIRECTORY  = new OPTKey(FWWritableDirectory.class, "invalidDirectory");

	protected final Window owner;
	private FWButton dirButton;
	
	public FWWritableDirectory(final Window owner, String xmlTag) {
		super(xmlTag);
		this.owner = owner;
		this.dirButton = new FWButton(SELECT_FOLDER, new FWButtonListener() {
			@Override
			public void actionPerformed(ActionEvent e, JButton source) {
				File f = openFileChooser(owner);
				if (f!= null) {
					source.setText(f.getAbsolutePath());
					source.revalidate();
					source.repaint();
					source.validate();
				}
			}
		});
	}
	
	public JPanel getComponent() {
		File f = getValue();
		if (f!=null)
			dirButton.setText(f.getAbsolutePath());

		JPanel p = new JPanel();
		p.add(dirButton);
		return p;
	}
	
	@Override
	protected boolean setValue(File v) {
		if (!v.isDirectory()) 
			if (!v.exists()) { 
				v.mkdirs();
			} else {
				return false;
			}
			
		if (!v.canWrite()) {
			showErrorMessage();
			return false;
		}
		
		boolean b = super.setValue(v);
		dirButton.setText(v.getAbsolutePath());
		
		return b;
	}
	
	private void showErrorMessage() {
		FWOptionPane.showErrorMessage(owner, INVALID_DIRECTORY);
	}



}
