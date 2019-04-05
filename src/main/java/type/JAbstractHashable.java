/**
 * 
 */
package type;

import type.JHashTable.JHashable;

/**
 * @author Salvatore Tummarello
 *
 */
public abstract class JAbstractHashable<T extends Object> extends JAbstractObject<T> implements JHashable<T> {

	/**
	 * @param value
	 */
	public JAbstractHashable(T value) {
		super(value);
	}

	@Override
	public final boolean isHashable() {
		return true;
	}

	@Override
	public Object getHashValue() {
		return getValue();
	}
}
