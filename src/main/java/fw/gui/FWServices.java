package fw.gui;

import java.awt.Container;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import fw.gui.FWOptionPane.OPTKey;


public class FWServices {
	
	private final static OPTKey URL_ERROR = new OPTKey(FWServices.class, "urlError");
	
	private static void showUrlErrorOptionPane(Container owner) {
		FWOptionPane.showErrorMessage(owner, URL_ERROR);
	}
	

	public static void openBrowser(Container owner, String url) {
		try {
			openBrowser(owner, new URL(url));
		} catch (MalformedURLException ex) {
			showUrlErrorOptionPane(owner);
		}
	}
	
	public static void openBrowser(final Container owner, final URL url) {
		new Thread() {
			public void run() {
				try {
					Desktop.getDesktop().browse(url.toURI());
				} catch (IOException | URISyntaxException ex) {
					showUrlErrorOptionPane(owner);
				}
			}
		}.start();
	}
}
