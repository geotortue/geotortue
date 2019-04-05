package fw.geometry.util;


public class MathException extends Exception {
	private static final long serialVersionUID = -2023279816493490733L;

	public MathException(String message) {
		super(message);
	}
	
	public static class ZeroVectorException extends MathException {
		private static final long serialVersionUID = -7656759043628026971L;

		public ZeroVectorException() {
			super("zeroVectorException");
		}
		
	}
}