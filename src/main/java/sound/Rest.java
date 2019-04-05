/**
 * 
 */
package sound;

import javax.sound.midi.Track;

import jep2.JEP2;
import type.JObjectsVector;

/**
 * @author Salvatore Tummarello
 *
 */
public class Rest extends MusicEvent {

	private long duration;

	private Rest(JObjectsVector v, double duration) {
		super(v);
		this.duration = (long) (duration*GTMidi.RESOLUTION);
	}
	
	public static Rest create(double duration) {
		JObjectsVector vec = new JObjectsVector();
		vec.add(JEP2.createNumber(duration));
		return new Rest(vec, duration);
		
	}

	public long putIn(Track t, int channel, long tick) {
		return tick + duration;
	}

	@Override
	public MusicEventType getMusicEventType() {
		return MusicEventType.REST;
	}

	@Override
	public String format() {
		return " ";
	}
	
	

}
