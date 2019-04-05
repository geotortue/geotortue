/**
 * 
 */
package sound;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;

import jep2.JEP2;
import sound.MusicException.InvalidTempoException;
import type.JObjectsVector;

/**
 * @author Salvatore Tummarello
 *
 */
public class Tempo extends MusicEvent {

	private int tempo;

	private Tempo(JObjectsVector v, int tempo) throws InvalidTempoException {
		super(v);
		this.tempo = tempo;
		if (tempo<0 )
			throw new InvalidTempoException(tempo+"");
	}
	
	public static Tempo create(int inst) throws InvalidTempoException {
		JObjectsVector vec = new JObjectsVector();
		vec.add(JEP2.createNumber(inst));
		return new Tempo(vec, inst);
	}

	public long putIn(Track t, int channel, long tick) {
		try {

			int mpqTempo = 60000000 / tempo; //calculate MPQ from BPM: mpq = 60000000 / bpm.
			
			byte[] byteTempo = new byte[3]; //The tempo value is a 3 bytes BIG_ENDIAN
	        byteTempo[0] = (byte) ((mpqTempo >> 16) & 0xFF);
	        byteTempo[1] = (byte) ((mpqTempo >> 8) & 0xFF);
	        byteTempo[2] = (byte) (mpqTempo & 0xFF);

	        MetaMessage tempoChange  = new MetaMessage(0x51, byteTempo, 3);
			t.add(new MidiEvent(tempoChange, tick-10));
		} catch (InvalidMidiDataException ex) {
			ex.printStackTrace();
		}
		return tick;
	}

	@Override
	public MusicEventType getMusicEventType() {
		return MusicEventType.TEMPO;
	}

	@Override
	public String format() {
		return "♩="+tempo;
	}
	


}
