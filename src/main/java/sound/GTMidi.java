/**
 * 
 */
package sound;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.sun.media.sound.StandardMidiFileWriter;

import fw.app.FWManager;
import fw.app.FWRestrictedAccessException;
import fw.app.Translator.TKey;
import fw.app.prefs.FWDirectoryEntry;
import fw.app.prefs.FWWritableDirectoryEntry;
import fw.app.prefs.FWWritableDirectoryEntry.UninitializedDirectoryException;
import fw.files.FileUtilities;
import fw.gui.FWLabel;
import fw.gui.FWOptionPane;
import fw.gui.FWSettingsActionPuller;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.layout.VerticalFlowLayout;
import geotortue.gallery.Gallery;

/**
 * @author Salvatore Tummarello
 *
 */
public class GTMidi implements GTMidiI {

	private static final TKey NAME = new TKey(GTMidi.class, "settings");
	private static final TKey DIR = new TKey(GTMidi.class, "dir");
	
//	private static final TKey SOUND_BANK = new TKey(GTMidi.class, "soundBank");
//	private static final FKey SB_FILE = new FKey(GTMidi.class, "sf2");
//	private static final TKey DEFAULT_SB = new TKey(GTMidi.class, "defaultSoundBank");
//	private static final TKey OPEN_SB = new TKey(GTMidi.class, "openSoundBank.tooltip");
//	private static final TKey DELETE_SB = new TKey(GTMidi.class, "deleteSoundBank.tooltip");
//	private static final OPTKey INVALID_SB = new OPTKey(GTMidi.class, "invalidSoundBank");
	
	final static int RESOLUTION = 960;
	private static final OPTKey MIDI_DIR_UNINITIALIZED = new OPTKey(GTMidi.class, "midiDirUninitialized");
	
	private final Sequencer sequencer;
//	private final Synthesizer synth = MidiSystem.getSynthesizer();
	private final Sequence sequence; 
	
	private final FWWritableDirectoryEntry directory; 
//	private final FWFileEntry soundBankFile = new FWFileEntry(this, "soundBank");
	
	private final Object monitor = new Object();
	
	private Track track;
	private static OPTKey CHOOSE_DIR_KEY = new OPTKey(GTMidi.class, "chooseDirectory");
	
	
	public GTMidi(Window owner) throws MidiUnavailableException, InvalidMidiDataException {
		this.sequencer = MidiSystem.getSequencer();
		this.sequence = new Sequence(Sequence.PPQ, RESOLUTION);
		File f;
		try {
			f = new File(FWManager.getConfigDirectory(), "midi");
		} catch (FWRestrictedAccessException e) {
			f = null;
		}
		this.directory = new FWWritableDirectoryEntry(owner, "GTMidi", CHOOSE_DIR_KEY);
		this.track = sequence.createTrack();
	}
	
	@Override
	public void putIn(List<MusicEvent> events, int channel) throws MidiChannelException {
		if (channel>15)
			throw new MidiChannelException();
		long tick = 120;
		for (MusicEvent event : events) 
			tick = event.putIn(track, channel, tick);
	}


	/**
	 * @throws InvalidMidiDataException 
	 * @throws MidiUnavailableException 
	 * 
	 */
	@Override
	public void play() {
		try {
			ShortMessage instrumentChange = new ShortMessage(ShortMessage.PROGRAM_CHANGE, 0, 0, 0);
	        track.add(new MidiEvent(instrumentChange, 60));
	        
		} catch (InvalidMidiDataException ex) {
			ex.printStackTrace();
		}
		sequencer.addMetaEventListener(new MetaEventListener() {
            @Override
            public void meta(MetaMessage metaMsg) {
                if (metaMsg.getType() == 0x2F) 
                    close();
            }
        });
		
		try {
			sequencer.setSequence(sequence);
		} catch (InvalidMidiDataException ex) {
			ex.printStackTrace();
		}
		
		
		
		try {
			sequencer.open();
			sequencer.start();
			
			
			synchronized (monitor) {
				while (sequencer.isRunning()) {
					try {
						monitor.wait();
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		} catch (MidiUnavailableException ex) {
			ex.printStackTrace();
		} finally {
			 close();
		}
	}
	
	private void close() {
		sequencer.close();
		synchronized (monitor) {
			monitor.notify();
		}
	}
	
	/**
	 * 
	 */
	@Override
	public void init() {
		sequence.deleteTrack(track);
		this.track = sequence.createTrack();		
	}

	/**
	 * 
	 */
	@Override
	public void pause() {
		if (isOpen())
			sequencer.stop();
	}
	
	@Override
	public void resume() {
		if (isOpen())
			sequencer.start();
	}

	/**
	 * 
	 */
	@Override
	public void interrupt() {
		if (isOpen()) {
			sequencer.stop();
			close();
		}
	}
	
	@Override
	public boolean isOpen() {
		return sequencer.isOpen();
	}
	
	@Override
	public void write(final String fileName) {
		
		try {
			directory.getValueSafely();
		} catch (UninitializedDirectoryException ex) {
			try {
				directory.askForValue();
			} catch (UninitializedDirectoryException e) {
			}
		}
		
		
		new Thread(){
			@Override
			public void run() {
				try {
					File out = FileUtilities.getNewFile(directory.getValueSafely(), fileName+"-%%%.mid");
					StandardMidiFileWriter w = new StandardMidiFileWriter(); 
					w.write(sequence, w.getMidiFileTypes()[1], out);
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (UninitializedDirectoryException e) {
					FWOptionPane.showInformationMessage(null, MIDI_DIR_UNINITIALIZED);
				}
			}
		}.start();
	}
	
//	private void loadSoundBank() throws InvalidMidiDataException, IOException, MidiUnavailableException {
//		synth.open();
//		try {
//		File file = soundBankFile.getValue();
//		if (file == null)
//			loadDefaultSoundBank();
//		else {
//
//			Soundbank sbDefault = synth.getDefaultSoundbank();
//			synth.unloadAllInstruments( sbDefault );
//
//			Soundbank soundbank = MidiSystem.getSoundbank(file);
//				synth.loadAllInstruments(soundbank);
//		}
//		} finally {
//			synth.close();
//		}
//	}
//	
//	private void loadDefaultSoundBank() throws MidiUnavailableException {
//		synth.open();
//		try {
//		Soundbank soundbank = synth.getDefaultSoundbank();
//		System.out.println("GTMidi.loadDefaultSoundBank() "+soundbank);
//		synth.loadAllInstruments(soundbank);
//		} finally {
//			synth.close();
//		}
//	}
	
	@Override
	public String getXMLTag() {
		return "GTMidi";
	}

	
	@Override
	public TKey getTitle() {
		return NAME;
	}
	
	@Override
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
//		final FWLabel sbLabel = new FWLabel(DEFAULT_SB, SwingConstants.LEFT);
//		File f = soundBankFile.getValue();
//		if (f != null && f.getPath().length() == 0)
//			sbLabel.setText(f.getName());
		
		
//		JPanel sbButtonsPane = new GTFileLoaderWidget(sbLabel, OPEN_SB, "document-open.png", DELETE_SB, "edit-clear.png") {
//
//			private static final long serialVersionUID = -52552539672327542L;
//
//			@Override
//			protected void open() {
//				Window owner = (Window) sbLabel.getTopLevelAncestor();
//				FWFileAssistant a = new FWFileAssistant(owner, SB_FILE, false);
//				File file = a.getFileForLoading();
//				if (file == null)
//					return;
//				
//				try {
//					soundBankFile.setValue(file);
//					loadSoundBank();
//					sbLabel.setText(file.getName());
//				} catch (InvalidMidiDataException | IOException | MidiUnavailableException ex) {
//					FWOptionPane.showErrorMessage(owner, INVALID_SB);
//					sbLabel.setText(DEFAULT_SB.translate());
//				}
//			}
//			
//			@Override
//			protected void delete() {
//				soundBankFile.setValue(new File(""));
//				sbLabel.setText(DEFAULT_SB.translate());
//				try {
//					loadDefaultSoundBank();
//				} catch (MidiUnavailableException ex) {
//					ex.printStackTrace();
//				}
//			}
//		};
		
		return VerticalFlowLayout.createPanel(new FWLabel(DIR, SwingConstants.LEFT), directory.getComponent());
//				VerticalPairingLayout.createPanel(new FWLabel(SOUND_BANK), sbButtonsPane));
	}
	
	public static class MidiChannelException extends Exception {

		private static final long serialVersionUID = 3116320953086066894L;
		
	}
}