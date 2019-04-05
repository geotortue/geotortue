/**
 * 
 */
package fw.app;

import java.awt.Cursor;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import fw.app.Translator.TKey;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.layout.HorizontalFlowLayout;

public abstract class FWWorker extends SwingWorker<Exception, Void> {

	private final WKey key ;
	private final JFrame waitingFrame;
	private final JFrame owner;
	
	
	private static FWWorkerWaitingFrameSupplier WF_SUPPLIER = new FWWorkerWaitingFrameSupplier() {

		@Override
		public JPanel getContentPane(String title) {
			JProgressBar progressBar = new JProgressBar();
			progressBar.setIndeterminate(true);
			progressBar.setStringPainted(true);
			progressBar.setString(title);
			return HorizontalFlowLayout.createPanel(5, progressBar);
		}
	};
	
	public FWWorker(WKey key, JFrame owner) {
		this.owner = owner;
		this.key = key;

		waitingFrame = new JFrame(); 
		waitingFrame.setAlwaysOnTop(true);
		waitingFrame.setUndecorated(true);
		waitingFrame.setIconImage(FWLauncher.ICON);
		waitingFrame.setContentPane(WF_SUPPLIER.getContentPane(this.key.translate()));

		waitingFrame.pack();

		waitingFrame.setSize(waitingFrame.getPreferredSize());
		waitingFrame.setLocationRelativeTo(owner);
		
		waitingFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		waitingFrame.setVisible(true);

		execute();
	}
	
	public static void setWaitingFrameSupplier(FWWorkerWaitingFrameSupplier supplier) {
		WF_SUPPLIER = supplier;
	}
	
	
	
	@Override
	protected final Exception doInBackground() {
		owner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			runInBackground();
		} catch (Exception ex) {
			return ex;
		}
		return null;
	}

	@Override
	protected void done() {
		waitingFrame.dispose();
		owner.setCursor(Cursor.getDefaultCursor());
		
		try {
			handleOutcome(get());
		} catch (InterruptedException | ExecutionException ex) {
			ex.printStackTrace();
		}
	}

	public abstract void runInBackground() throws Exception;
	
	public void handleOutcome(Exception ex) {
		if (ex == null && key.success != null) {
			FWOptionPane.showInformationMessage(owner, key.success); 
		}
		if (ex != null) {
			FWOptionPane.showErrorMessage(owner, key.failure);
			ex.printStackTrace();
		}
	}
	
	public interface FWWorkerWaitingFrameSupplier {
		public JPanel getContentPane(String title);
	}
	
	public static class WKey extends TKey {
		private final OPTKey success, failure ;
		
		public WKey(Class<?> c, String key, boolean showOnSuccess) {
			super(c, key+".worker");
			this.failure = new OPTKey(c, key+".failure");
			if (showOnSuccess)
				this.success = new OPTKey(c, key+".success");
			else
				this.success = null;
		}
	}
}