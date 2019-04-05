/**
 * 
 */
package sound;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.nfunk.jep.addon.JEPException;

import jep2.JEP2;
import sound.MusicException.InvalidDurationException;
import sound.MusicException.InvalidNoteException;
import sound.MusicException.InvalidVelocityException;
import type.JInteger;
import type.JObjectI;
import type.JObjectsVector;
import type.JString;

/**
 * @author Salvatore Tummarello
 *
 */
public class Note extends MusicEvent {

	private short value, velocity;
	private long duration;
	
	private Note(JObjectsVector vec, long val, double duration, long velocity) throws MusicException {
		super(vec);
		if (val>127 || val<0 )
			throw new InvalidNoteException(val+"");
		if (duration<0)
			throw new InvalidDurationException(duration+"");
		if (velocity>127 || velocity<0 )
			throw new InvalidVelocityException(velocity+"");

		this.value = (short) val;
		this.duration = (long) (duration*GTMidi.RESOLUTION);
		this.velocity = (short) velocity;
	}
	
	static Note create(JObjectsVector vec) throws MusicException, JEPException {
		int n = vec.size();
		
		long val;
		JObjectI<?> o = vec.elementAt(0);
		if (o.getType() == JEP2Type.STRING)
			val = valueToInt(((JString) o).getValue());
		else if (o.getType() == JEP2Type.LONG)
			val = ((JInteger) o).getValue();
		else
			throw new InvalidNoteException(o.toString());
		
		double duration = (n>1)? JEP2.getDouble(vec.elementAt(1)) : 1;
		int vel = (n>2)? (int) JEP2.getLong(vec.elementAt(2)) : 127;
		
		return new Note(vec, val, duration, vel);
	}


	/**
	 * @param value2
	 * @return
	 * @throws InvalidNoteException 
	 */
	private static short valueToInt(String value) throws InvalidNoteException {
		int len = value.length();
		if (len==0)
			throw new InvalidNoteException(value);
		int idx = 0;
		int val = getNote(value.charAt(idx));
		
		idx++;

		char c = value.charAt(idx);
		if (c=='#') {
			val++;
			idx++;
		} else if (c=='b') {
			val--;
			idx++;
		} 
		
		int octave = Integer.valueOf(value.substring(idx));
		val += 12*octave;
		if (val>127 || val<0 )
			throw new InvalidNoteException(value);
		return (short) val;
	}

	private static short getNote(char c) throws InvalidNoteException {
		switch (c) {
		case 'C':
			return 0;
		case 'D':
			return 2;
		case 'E':
			return 4;
		case 'F':
			return 5;
		case 'G':
			return 7;
		case 'A':
			return 9;
		case 'B':
			return 11;
		default:
			throw new InvalidNoteException(c+"");
		}
	}
	
	public long putIn(Track t, int channel, long tick) {
		long newTick = tick + duration;
		try {
			ShortMessage m1 = new ShortMessage(ShortMessage.NOTE_ON, channel, value, velocity);
			t.add(new MidiEvent(m1, tick));
			ShortMessage m2 = new ShortMessage(ShortMessage.NOTE_OFF, channel, value, velocity);
			t.add(new MidiEvent(m2, newTick));
		} catch (InvalidMidiDataException ex) {
			ex.printStackTrace();
		}
		return newTick;
	}
	
	@Override
	public MusicEventType getMusicEventType() {
		return MusicEventType.NOTE;
	}


	@Override
	public String format() {
		return "♫"+getNote(value);
	}
	
	private static String getNote(short v) {
		switch (v%12) {
		case 0:
			return "C";
		case 1:
			return "C♯";
		case 2:
			return "D";
		case 3:
			return "D♯";
		case 4:
			return "E";
		case 5:
			return "F";
		case 6:
			return "F♯";
		case 7:
			return "G";
		case 8:
			return "G♯";
		case 9:
			return "A";
		case 10:
			return "A♯";
		case 11:
			return "B";
		default:
			return "???";
		}
	}
}