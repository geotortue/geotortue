/**
 * 
 */
package type;

/**
 * @author Salvatore Tummarello
 *
 */
public abstract class JAbstractObject<T extends Object> implements JObjectI<T> {
	
	private final T value;

	public JAbstractObject(T value) {
		this.value = value;
	}

	@Override
	public T getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}
	
	@Override
	public boolean isHashable() {
		return false;
	}

	@Override
	public boolean isMutable() {
		return false;
	}
}
