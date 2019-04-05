/**
 * 
 */
package fw.app;

public class FWRestrictedAccessException extends Exception {
	private static final long serialVersionUID = 6691599265060059891L;

	public FWRestrictedAccessException() {
		super("--restrict flag enabled");
	}
}