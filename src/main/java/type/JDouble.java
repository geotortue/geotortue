/**
 * 
 */
package type;

import org.nfunk.jep.addon.JEPException;

import jep2.JEP2;
import jep2.JEP2.JEP2Trouble;

/**
 * @author Salvatore Tummarello
 *
 */
public class JDouble extends JNumber<Double> {
	
	JDouble(double v) {
		super(v);
	}

	@Override
	public JEP2Type getType() {
		return JEP2Type.DOUBLE;
	}

	@Override
	public JObjectI<?> add(JObjectI<?> o) throws JEPException {
		if (o.isANumber()) { 
			JNumber<?> b = (JNumber<?>) o;
			return JEP2.createNumber(getValue() + b.doubleValue());
		}
		throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_SUM, toString(), o.toString());
	}

	@Override
	public JObjectI<?> mul(JObjectI<?> o) throws JEPException {
		if (o.isANumber()) { 
			JNumber<?> b = (JNumber<?>) o;
			return JEP2.createNumber(getValue() * b.doubleValue());
		}		
		throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_PROD, toString(), o.toString());
	}


	@Override
	public JObjectI<?> opp() {
		return new JDouble(-getValue());
	}

	@Override
	public JNumber<?> sub(JNumber<?> b) {
		return new JDouble(getValue()-b.doubleValue());
	}

	@Override
	public JNumber<?> div(JNumber<?> b) {
		return new JDouble(getValue()/b.doubleValue());
	}

	@Override
	public JNumber<?> mod(JNumber<?> b) {
		return new JDouble(getValue() % b.doubleValue());
	}

	@Override
	public JNumber<?> pow(JNumber<?> b) {
		return new JDouble(Math.pow(getValue(), b.doubleValue()));
	}
	
	@Override
	public boolean isZero() {
		return getValue()==0;
	}

	@Override
	public boolean isOne() {
		return getValue()==1;
	}
}
