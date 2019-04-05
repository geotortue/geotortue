/**
 * 
 */
package type;

import org.nfunk.jep.addon.JEPException;
import org.nfunk.jep.type.Complex;

import jep2.JEP2.JEP2Trouble;

/**
 * @author Salvatore Tummarello
 *
 */
public class JNumberFactory implements JNumberFactoryI {

	private JInteger ZERO = new JInteger(0);
	private JInteger ONE = new JInteger(1);
	private JInteger TWO = new JInteger(2);
	private JInteger MINUSONE = new JInteger(-1);

	@Override
	public JNumber<?> createNumber(String value) throws JEPException {
		try {
			return createNumber((int) Integer.valueOf(value));
		} catch (NumberFormatException ex) {
		}
		try {
			return createNumber(Double.valueOf(value));
		} catch (NumberFormatException ex) {
			throw new JEPException(JEP2Trouble.JEP2_NOT_A_NUMBER, "\""+value.toString()+"\"");
		}
	}

	@Override
	public JNumber<?> createNumber(double value) {
		return new JDouble(new Double(value));
	}

	@Override
	public JNumber<?> createNumber(Number value) {
		return createNumber(value.doubleValue());
	}

	@Override
	public JInteger createNumber(boolean value) {
		return (value ? getOne() : getZero());
	}

	@Override
	public JNumber<?> createNumber(float value) {
		return createNumber(new Double(value));
	}
	
	public JNumber<?> createNumber(long value) {
		return new JInteger(value);
	}

	@Override
	public JInteger createNumber(int value) {
		return new JInteger(value);
	}

	@Override
	public JInteger createNumber(short value) {
		return new JInteger(value);
	}

	@Override @Deprecated
	public JObjectI<?> createNumber(Complex value) {
		System.err.println("JNumberFactory.createNumber()");
		return JNullObject.NULL_OBJECT;
	}

	@Override
	public JInteger getMinusOne() {
		return MINUSONE;
	}

	@Override
	public JInteger getOne() {
		return ONE;
	}

	@Override
	public JInteger getTwo() {
		return TWO;
	}

	@Override
	public JInteger getZero() {
		return ZERO;
	}

	@Override
	public JList createList(JObjectsVector v) {
		return new JList(v);
	}

	@Override
	public JDict createDict(JHashTable t) {
		return new JDict(t);
	}

	@Override
	public JString createString(String s) {
		return new JString(s);
	}

	@Override
	public JBoolean createBoolean(boolean b) {
		return new JBoolean(b);
	}
}