/**
 * 
 */
package type;

import java.math.BigInteger;

import org.nfunk.jep.addon.JEPException;

import jep2.JEP2;
import jep2.JEP2.JEP2Trouble;

/**
 * @author Salvatore Tummarello
 *
 */
public class JInteger extends JNumber<Long> {

	JInteger(long v) {
		super(v);
	}

	@Override
	public JEP2Type getType() {
		return JEP2Type.LONG;
	}

	@Override
	public JObjectI<?> add(JObjectI<?> o) throws JEPException {
		if (o.isANumber()) {
			JNumber<?> b = (JNumber<?>) o;
			if (b.getType() == JEP2Type.LONG)
				try {
					return addAndCheck(getValue(), ((JInteger) b).getValue());
				} catch (ArithmeticException ex) {}
			
			return JEP2.createNumber(getValue() + b.doubleValue());
		}
		throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_SUM, toString(), o.toString());
	}

	@Override
	public JObjectI<?> mul(JObjectI<?> o) throws JEPException {
		if (o.isANumber()) {
			JNumber<?> b = (JNumber<?>) o;
			if (b.getType() == JEP2Type.LONG)
				try {
					return mulAndCheck(getValue(), ((JInteger) b).getValue());
				} catch (ArithmeticException ex) {}
			return JEP2.createNumber(getValue() * b.doubleValue());
		}

		if (o.getType() == JEP2Type.LIST)
			return ((JList) o).mul(this);

		if (o.getType() == JEP2Type.STRING)
			return ((JString) o).mul(this);

		throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_PROD, toString(), o.toString());
	}

	@Override
	public JObjectI<?> opp() {
		return JEP2.createNumber(-getValue());
	}

	@Override
	public JNumber<?> sub(JNumber<?> b) {
		if (b.getType() == JEP2Type.LONG)
			return JEP2.createNumber(getValue() - ((JInteger) b).getValue());
		else
			return JEP2.createNumber(getValue() - b.doubleValue());
	}

	@Override
	public JNumber<?> div(JNumber<?> b) {
		if (b.getType() == JEP2Type.LONG) {
			long p = getValue();
			long q = ((JInteger) b).getValue();
			if (p%q==0)
				return JEP2.createNumber(p/q);
			else
				return JEP2.createNumber(p/(double) q);
		} else
			return JEP2.createNumber(getValue() / b.doubleValue());
	}

	@Override
	public JNumber<?> mod(JNumber<?> b) {
		if (b.getType() == JEP2Type.LONG)
			return JEP2.createNumber(getValue() % ((JInteger) b).getValue());
		else
			return JEP2.createNumber(getValue() % b.doubleValue());
	}

	@Override
	public JNumber<?> pow(JNumber<?> b) {
		if (b.getType() == JEP2Type.LONG) {
			long exp = ((JInteger) b).getValue();
			if (exp < 63)
			try {
				BigInteger a = new BigInteger(getValue().toString());
				long l = (a.pow((int) exp)).longValue();
				return JEP2.createNumber(l);
			} catch (ArithmeticException ex) {}
		}
		return JEP2.createNumber(Math.pow(getValue(), b.doubleValue()));
	}
	
	

	@Override
	public boolean isZero() {
		return getValue()==0;
	}

	@Override
	public boolean isOne() {
		return getValue()==1;
	}


	/**
	 * @return
	 */
	public int intValue() {
		return getValue().intValue();
	}
	
	private static JNumber<?> addAndCheck(long a, long b) {
		long ret;
		if (a > b) // use symmetry to reduce boundry cases
			return addAndCheck(b, a);
		else { // assert a <= b
			if (a < 0) {
				if (b < 0) {// check for negative overflow
					if (Long.MIN_VALUE - b <= a)
						ret = a + b;
					else
						throw new ArithmeticException();
				} else // opposite sign addition is always safe
					ret = a + b;
			} else { // check for positive overflow
				if (a <= Long.MAX_VALUE - b)
					ret = a + b;
				else
					throw new ArithmeticException();
			}
		}
		return JEP2.createNumber(ret);
	}

	public static JNumber<?> mulAndCheck(long a, long b) throws ArithmeticException {
		long ret;
		if (a > b) // use symmetry to reduce boundry cases
			return mulAndCheck(b, a);
		else {
			if (a < 0) {
				if (b < 0) { // check for positive overflow with negative a, negative b
					if (a >= Long.MAX_VALUE / b)
						ret = a * b;
					else
						throw new ArithmeticException();

				} else if (b > 0) { // check for negative overflow with negative a, positive b
					if (Long.MIN_VALUE / b <= a)
						ret = a * b;
					else
						throw new ArithmeticException();
				} else // assert b == 0
					ret = 0;
			} else if (a > 0) { // check for positive overflow with positive a, positive b
				if (a <= Long.MAX_VALUE / b)
					ret = a * b;
				else
					throw new ArithmeticException();
			} else // assert a == 0
				ret = 0;
		}
		return JEP2.createNumber(ret);
	}
}