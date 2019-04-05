/**
 * 
 */
package sound;

import java.util.List;

import javax.swing.JPanel;

import fw.app.Translator.TKey;
import fw.gui.FWSettingsActionPuller;
import sound.GTMidi.MidiChannelException;

/**
 * @author Salvatore Tummarello
 *
 */
public class GTMidiNotAvailable implements GTMidiI {

	private static final TKey NAME = new TKey(GTMidi.class, "settings");

	@Override
	public void putIn(List<MusicEvent> events, int channel) throws MidiChannelException {
	}

	@Override
	public void play() {
	}

	@Override
	public void init() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void interrupt() {
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public void write(String fileName) {
	}

	@Override
	public String getXMLTag() {
		return "GTMidi";
	}

	
	@Override
	public TKey getTitle() {
		return NAME;
	}

	@Override
	public JPanel getSettingsPane(FWSettingsActionPuller actions) {
		return new JPanel();
	}
}
