/**
 * 
 */
package sound;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import jep2.JEP2;
import sound.MusicException.InvalidInstrumentException;
import type.JObjectsVector;

/**
 * @author Salvatore Tummarello
 *
 */
public class Instrument extends MusicEvent {

	private int instrument;

	private Instrument(JObjectsVector v, int instrument) throws InvalidInstrumentException {
		super(v);
		this.instrument = instrument;
		if (instrument>127 || instrument<0 )
			throw new InvalidInstrumentException(instrument+"");
	}
	
	public static Instrument create(int inst) throws InvalidInstrumentException {
		JObjectsVector vec = new JObjectsVector();
		vec.add(JEP2.createNumber(inst));
		return new Instrument(vec, inst);
	}

	public long putIn(Track t, int channel, long tick) {
		try {
			ShortMessage instrumentChange = new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, instrument, 0);
			t.add(new MidiEvent(instrumentChange, tick-10));
		} catch (InvalidMidiDataException ex) {
			ex.printStackTrace();
		}
		return tick;
	}

	@Override
	public MusicEventType getMusicEventType() {
		return MusicEventType.INSTRUMENT;
	}

	@Override
	public String format() {
		return "🎹="+instrument;
	}
	


}
