/**
 * 
 */
package fw.app;

import java.util.Locale;

public class LanguageNotSupportedException extends Exception {

	private static final long serialVersionUID = 6191971419267728960L;

	public LanguageNotSupportedException(Locale l) {
		super(l.toString());
	}
}
