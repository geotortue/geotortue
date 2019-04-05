/**
 * 
 */
package fw.files;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import fw.app.Translator.TKey;
import fw.app.prefs.FWBooleanEntry;
import fw.app.prefs.FWIntegerEntry;
import fw.app.prefs.FWTextEntry;
import fw.gui.FWLabel;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.FWTextField;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWParameterListener;
import fw.xml.XMLTagged;

/**
 *
 */
public class ProxyConfigurator implements XMLTagged, FWSettings {

	private static final TKey TITLE = new TKey(ProxyConfigurator.class, "title");
	private static final TKey HOST = new TKey(ProxyConfigurator.class, "host");
	private static final TKey PORT = new TKey(ProxyConfigurator.class, "port");
	private static final TKey CUSTOM_CONFIG = new TKey(ProxyConfigurator.class, "customConfig");
	
	private final static String PROXY_HOST = System.getProperty("http.proxyHost");
	private final static String PROXY_PORT = System.getProperty("http.proxyPort");

	private final FWBooleanEntry useCustomConfig = new FWBooleanEntry(this, "customConfig", false);
	private final FWTextEntry host = new FWTextEntry(this, "host", "", 16);
	private final FWIntegerEntry port = new FWIntegerEntry(this, "port", 80, 0, 65535);
	
	public ProxyConfigurator() {
		boolean customConfig = useCustomConfig.getValue();
		if (customConfig && PROXY_HOST == null) {
			System.setProperty("http.proxyHost", host.getValue());
			System.setProperty("http.proxyPort", port.getEntryValue());
		}
	}
	
	private void updateProxy(boolean customConfig) {
		if (customConfig) {
			System.setProperty("http.proxyHost", host.getValue());
			System.setProperty("http.proxyPort", port.getEntryValue());
		} else {
			System.setProperty("http.proxyHost", "");
			System.setProperty("http.proxyPort", "80");
		}
	}

	@Override
	public String getXMLTag() {
		return "ProxyConfigurator";
	}
	
	@Override
	public TKey getTitle() {
		return TITLE;
	}

	@Override
	public JPanel getSettingsPane(FWSettingsActionPuller actions) {
		final FWTextField tf ;
		final JSpinner sp;
		if (PROXY_HOST == null) {
			tf = host.getComponent(new FWParameterListener<String>() {
				@Override
				public void settingsChanged(String value) {
					System.setProperty("http.proxyHost", value);
				}
			});

			sp = port.getComponent(new FWParameterListener<Integer>() {
				@Override
				public void settingsChanged(Integer value) {
					System.setProperty("http.proxyPort", String.valueOf(value));
				}
			});
		} else {
			tf = new FWTextField(PROXY_HOST, 18);
			int value = (PROXY_PORT == null) ? 80 : Integer.parseInt(PROXY_PORT);
			sp = new JSpinner(new SpinnerNumberModel(value, 0, 65535, 1));
		}

		JCheckBox cb = useCustomConfig.getComponent(new FWParameterListener<Boolean>() {
			@Override
			public void settingsChanged(Boolean value) {
				updateProxy(value);
				tf.setEnabled(value);
				sp.setEnabled(value);
			}
		}); 
		
		boolean value = useCustomConfig.getValue();
		tf.setEnabled(value && PROXY_HOST == null);
		sp.setEnabled(value && PROXY_HOST == null);
		
		cb.setEnabled(PROXY_HOST == null);
		
		return VerticalPairingLayout.createPanel(10, 10, 
				new FWLabel(CUSTOM_CONFIG, SwingConstants.RIGHT), cb,
				new FWLabel(HOST, SwingConstants.RIGHT), tf,
				new FWLabel(PORT, SwingConstants.RIGHT), sp);
	}
}