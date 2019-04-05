/**
 * 
 */
package sound;

import javax.sound.midi.Track;

import type.JObjectI;
import type.JObjectsVector;

/**
 * @author Salvatore Tummarello
 *
 */
public class Chord extends MusicEvent {

	public Chord(JObjectsVector v) {
		super(v);
	}

	@Override
	public long putIn(Track t, int channel, long tick) {
		long newTick = tick;
		for (JObjectI<?> note : getValue()) 
			newTick = ((MusicEvent) note).putIn(t, channel, tick);
		return newTick;
	}

	@Override
	public MusicEventType getMusicEventType() {
		return MusicEventType.CHORD;
	}

	@Override
	public String format() {
		String str = "(";
		for (JObjectI<?> o : getValue()) {
			MusicEvent ev = (MusicEvent) o;
			str += ev.format()+" ";
		}
		str += ")";
		return str;
	}

	
	
}
