/**
 * 
 */
package sound;

import javax.sound.midi.Track;

import org.nfunk.jep.addon.JEPException;

import jep2.JEP2.JEP2Trouble;
import type.JAbstractObject;
import type.JObjectI;
import type.JObjectsVector;

/**
 * @author Salvatore Tummarello
 *
 */
public abstract class MusicEvent extends JAbstractObject<JObjectsVector> {
	
	public enum MusicEventType {NOTE, REST, INSTRUMENT, TEMPO, CHORD}
	
	public MusicEvent(JObjectsVector value) {
		super(value);
	}

	public abstract MusicEventType getMusicEventType();
	
	public abstract long putIn(Track t, int channel, long tick);
	
	public abstract String format();

	
	@Override
	public final JEP2Type getType() {
		return JEP2Type.MUSIC;
	}

	@Override
	public boolean isIterable() {
		return false;
	}

	@Override
	public boolean isANumber() {
		return false;
	}
	
	public JObjectI<?> add(JObjectI<?> o) throws JEPException {
		throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_SUM, toString(), o.toString());
	}

	@Override
	public JObjectI<?> mul(JObjectI<?> o) throws JEPException {
		throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_PROD, toString(), o.toString());
	}

	@Override
	public String toString() {
		return format();
	}
}
