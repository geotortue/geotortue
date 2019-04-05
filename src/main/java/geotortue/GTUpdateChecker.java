package geotortue;

import java.awt.Window;
import java.net.URL;

import fw.files.TextFile;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.ANSWER;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.FWServices;
import fw.text.FWParsingTools;

public class GTUpdateChecker {

	/**
	 * 
	 */
	private static final OPTKey UPDATE = new OPTKey(GTUpdateChecker.class, "update");
	private static final OPTKey CHECKING_FAILED = new OPTKey(GTUpdateChecker.class, "checkingFailed");
	private static final OPTKey NO_UPDATE = new OPTKey(GTUpdateChecker.class, "noUpdate");

	public enum STATE {UPDATE_AVAILABLE, NO_UPDATE, CHECKING_FAILED} ; 
	
	public static void checkUpdateAtStartUp(Window owner) {
		STATE state = checkUpdate();
		switch (state) {
		case UPDATE_AVAILABLE :
			showDialog(state, owner);
			break;
		default:
			break;
		}
	}
	
	public static void checkUpdateNow(Window owner) {
		STATE state = checkUpdate();
		showDialog(state, owner);
	}
	
	public static STATE checkUpdate() {
		try {
			if (GTLauncher.VERSION.isOlderThan(getRemoteVersion()))
				return STATE.UPDATE_AVAILABLE;
			else
				return STATE.NO_UPDATE;
		} catch (Exception e) {
			return STATE.CHECKING_FAILED;
		}
	}
	

	private static GTVersion getRemoteVersion() throws Exception {
		URL url = GTLauncher.IS_BETA ? new URL("http://geotortue.free.fr/deploy/version.beta") : new URL("http://geotortue.free.fr/deploy/version");
		TextFile f = new TextFile(url);
		return new GTVersion(f.getText().trim());
	}
	
	private static void showDialog(STATE state, Window owner) {
		switch (state) {
		case UPDATE_AVAILABLE :
			if (FWOptionPane.showConfirmDialog(owner, UPDATE) == ANSWER.YES)
				FWServices.openBrowser(owner, "http://geotortue.free.fr/index.php?page=telechargement");
			break;
		case NO_UPDATE :
			FWOptionPane.showInformationMessage(owner, NO_UPDATE);
			break;
		case CHECKING_FAILED :
			FWOptionPane.showErrorMessage(owner, CHECKING_FAILED); 
			break;
		default:
			break;
		}
	}
	

	public static class GTVersion {
		final int v, y, m, d;

		public GTVersion(String str) {
			String[] strs = FWParsingTools.split(str, "\\.");
			this.v = Integer.parseInt(strs[0]);
			this.y = Integer.parseInt(strs[1]);
			this.m = Integer.parseInt(strs[2]);
			this.d = Integer.parseInt(strs[3]);
		}

		public GTVersion(int v, int y, int m, int d) {
			this.v = v;
			this.y = y;
			this.m = m;
			this.d = d;
		}
		
		@Override
		public String toString() {
			return v+"."+y+"."+m+"."+d;
		}

		public boolean isOlderThan(GTVersion candidate) {
			if (v < candidate.v)
				return true;
			if (v > candidate.v)
				return false;
			
			if (y < candidate.y)
				return true;
			if (y > candidate.y)
				return false;
			
			if (m < candidate.m)
				return true;
			if (m > candidate.m)
				return false;
			
			if (d < candidate.d)
				return true;
			return false;
		}
	}
}