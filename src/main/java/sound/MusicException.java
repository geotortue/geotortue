/**
 * 
 */
package sound;

/**
 * @author Salvatore Tummarello
 *
 */
public class MusicException extends Exception {
	
	private static final long serialVersionUID = -1954303952442542434L;
	
	/**
	 * @param value
	 */
	public MusicException(String value) {
		super(value);
	}

	public static class InvalidNoteException extends MusicException {

		private static final long serialVersionUID = 2535646596495406715L;
		
		public InvalidNoteException(String value) {
			super(value);
		}
	}

	public static class InvalidVelocityException extends MusicException {

		private static final long serialVersionUID = 6960728142873000712L;

		public InvalidVelocityException(String value) {
			super(value);
		}
	}

	public static class InvalidDurationException extends MusicException {

		private static final long serialVersionUID = 1955661143763597977L;

		public InvalidDurationException(String value) {
			super(value);
		}
	}
	
	public static class InvalidInstrumentException extends MusicException {

		private static final long serialVersionUID = -2492726119935492679L;

		public InvalidInstrumentException(String value) {
			super(value);
		}
	}
	
	public static class InvalidTempoException extends MusicException {

		private static final long serialVersionUID = -4713999176783914842L;

		public InvalidTempoException(String value) {
			super(value);
		}
	}


}
