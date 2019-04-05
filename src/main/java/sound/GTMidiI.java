/**
 * 
 */
package sound;

import java.util.List;

import javax.swing.JPanel;

import fw.app.Translator.TKey;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.xml.XMLTagged;
import sound.GTMidi.MidiChannelException;

/**
 * @author Salvatore Tummarello
 *
 */
public interface GTMidiI extends FWSettings, XMLTagged {

	public void putIn(List<MusicEvent> events, int channel) throws MidiChannelException;

	public void play();

	public void init();

	public 	void pause();

	public void resume();

	public void interrupt();

	public boolean isOpen();

	public void write(String fileName);

	public String getXMLTag();

	public TKey getTitle();

	public JPanel getSettingsPane(FWSettingsActionPuller actions);

}