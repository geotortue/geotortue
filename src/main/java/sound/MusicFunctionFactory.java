/**
 * 
 */
package sound;

import java.util.Stack;
import java.util.Vector;

import org.nfunk.jep.addon.JEPException;
import org.nfunk.jep.addon.JEPTroubleI;

import function.JFunction;
import jep2.JEP2Exception;
import jep2.JKey;
import sound.MusicException.InvalidDurationException;
import sound.MusicException.InvalidInstrumentException;
import sound.MusicException.InvalidNoteException;
import sound.MusicException.InvalidTempoException;
import sound.MusicException.InvalidVelocityException;
import type.JObjectI;
import type.JObjectsVector;

/**
 * @author Salvatore Tummarello
 *
 */
public class MusicFunctionFactory {

	private static final JKey NOTE = new JKey(MusicFunctionFactory.class, "note");
	private static final JKey CHORD = new JKey(MusicFunctionFactory.class, "chord");
	private static final JKey REST = new JKey(MusicFunctionFactory.class, "rest");
	private static final JKey TEMPO = new JKey(MusicFunctionFactory.class, "tempo");
	private static final JKey INSTRUMENT = new JKey(MusicFunctionFactory.class, "instrument");
	
	public enum MusicTrouble implements JEPTroubleI {
		MUSIC_NOTE_ERROR, MUSIC_INVALID_NOTE, MUSIC_INVALID_VELOCITY, MUSIC_INVALID_DURATION, 
		MUSIC_CHORD_ERROR, MUSIC_INVALID_INSTRUMENT, MUSIC_INVALID_TEMPO
	}
	
	public static Vector<JFunction> getFunctions() {
		Vector<JFunction> table = new Vector<>();
		table.add(new NoteFunction());
		table.add(new ChordFunction());
		table.add(new RestFunction());
		table.add(new InstrumentFunction());
		table.add(new TempoFunction());
		return table;
	}
	
	
	private static class NoteFunction extends JFunction {
		public NoteFunction() {
			super(NOTE, -1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			int n = curNumberOfParameters;
			if (n==0 || n>3)
				throw new JEP2Exception(this, MusicTrouble.MUSIC_NOTE_ERROR);
			
			JObjectsVector vec = new JObjectsVector();
			for (int idx = 0; idx < n; idx++) {
				JObjectI<?> o = popJEPObjectI(inStack);
				vec.add(0, o);
			}

			try {
				return Note.create(vec);
			} catch (InvalidNoteException e) {
				throw new JEP2Exception(this, MusicTrouble.MUSIC_INVALID_NOTE, e.getMessage());
			} catch (InvalidVelocityException e) {
				throw new JEP2Exception(this, MusicTrouble.MUSIC_INVALID_VELOCITY, e.getMessage());
			} catch (InvalidDurationException e) {
				throw new JEP2Exception(this, MusicTrouble.MUSIC_INVALID_DURATION, e.getMessage());
			} catch (MusicException ex) {
				ex.printStackTrace();
				throw new JEP2Exception(this, MusicTrouble.MUSIC_NOTE_ERROR, "unknown");
			}
		}
	}
	
	private static class ChordFunction extends JFunction {
		public ChordFunction() {
			super(CHORD, -1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			int n = curNumberOfParameters;
			
			JObjectsVector vec = new JObjectsVector();
			for (int idx = 0; idx < n; idx++) {
				JObjectI<?> o = popJEPObjectI(inStack);
				if (o instanceof MusicEvent)
					vec.add(0, o);
				else
					throw new JEP2Exception(this, MusicTrouble.MUSIC_CHORD_ERROR, o.toString());
			}
			
			return new Chord(vec);
		}
	}
	
	private static class RestFunction extends JFunction {
		public RestFunction() {
			super(REST, 1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			double duration = popDouble(inStack);
			return Rest.create(duration);
		}
	}

	private static class InstrumentFunction extends JFunction {
		public InstrumentFunction() {
			super(INSTRUMENT, 1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			long instrument = popLong(inStack);
			try {
				return Instrument.create((int) instrument);
			} catch (InvalidInstrumentException e) {
				throw new JEP2Exception(this, MusicTrouble.MUSIC_INVALID_INSTRUMENT, instrument+"");
			}
		}
	}
	
	private static class TempoFunction extends JFunction {
		public TempoFunction() {
			super(TEMPO, 1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			long tempo = popLong(inStack);
			try {
				return Tempo.create((int) tempo);
			} catch (InvalidTempoException e) {
				throw new JEP2Exception(this, MusicTrouble.MUSIC_INVALID_TEMPO, tempo+"");
			}
		}
	}
}
