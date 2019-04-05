/**
 * 
 */
package type;

/**
 * @author Salvatore Tummarello
 *
 */
public abstract class JNumber<T extends Number> extends JAbstractHashable<T> {

	
	public JNumber(T value) {
		super(value);
	}
	
	@Override
	public T getValue() {
		return super.getValue();
	}

	@Override
	public final boolean isIterable() {
		return false;
	}

	@Override
	public final boolean isANumber() {
		return true;
	}
	
	public final Double doubleValue() {
		return getValue().doubleValue();
	}
	
	public final Long longValue() {
		return getValue().longValue();
	}
	
	
	public abstract JNumber<?> sub(JNumber<?> b);
	
	public abstract JNumber<?> div(JNumber<?> b);
	
	public abstract JNumber<?> mod(JNumber<?> b);
	
	public abstract JNumber<?> pow(JNumber<?> b);

	public abstract JObjectI<?> opp();

	public abstract boolean isZero();
	
	public abstract boolean isOne();

}
