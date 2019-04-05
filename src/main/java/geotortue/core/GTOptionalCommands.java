/**
 * 
 */
package geotortue.core;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import fw.app.Translator.TKey;
import fw.gui.FWLabel;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWBoolean;
import fw.gui.params.FWParameterListener;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;

/**
 * @author Salvatore Tummarello
 *
 */
public abstract class GTOptionalCommands implements XMLCapabilities, FWSettings {

	
	private static final TKey OPTIONS = new TKey(GTOptionalCommands.class, "title");
	private static final TKey CIRCLE = new TKey(GTOptionalCommands.class, "circle");
	private static final TKey MUSIC = new TKey(GTOptionalCommands.class, "music");
	private final FWBoolean circleCommands = new FWBoolean("circle", false);
	private final FWBoolean musicCommands = new FWBoolean("music", false);
	
	private final boolean midiAvailable;
	
	public GTOptionalCommands(boolean midiAvailable) {
		this.midiAvailable = midiAvailable;
	}

	@Override
	public String getXMLTag() {
		return "GTOptionalCommands";
	}


	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		circleCommands.storeValue(e);
		musicCommands.storeValue(e);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		circleCommands.fetchValue(child, false);
		musicCommands.fetchValue(child, false);
		update();
		return child;
	}


	@Override
	public TKey getTitle() {
		return OPTIONS;
	}
	
	@Override
	public JPanel getSettingsPane(FWSettingsActionPuller actions) {
		JCheckBox circleCB =  circleCommands.getComponent(new FWParameterListener<Boolean>() {
			@Override
			public void settingsChanged(Boolean b) {
				setCircleEnabled(b);
			}
		});
		
		JCheckBox musicCB =  musicCommands.getComponent(new FWParameterListener<Boolean>() {
			@Override
			public void settingsChanged(Boolean b) {
				setMusicEnabled(b);
			}
		});
		musicCB.setEnabled(midiAvailable);
		
		return VerticalPairingLayout.createPanel(
				new FWLabel(CIRCLE), circleCB,
				new FWLabel(MUSIC), musicCB);
	}
	
	public abstract void setCircleEnabled(boolean value);
	
	public abstract void setMusicEnabled(boolean b);
	
	public void update() {
		setCircleEnabled(circleCommands.getValue());
		setMusicEnabled(musicCommands.getValue());
	}
}
