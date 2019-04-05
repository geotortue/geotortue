/**
 * 
 */
package function;

import java.util.Stack;
import java.util.Vector;

import org.nfunk.jep.addon.JEPException;

import jep2.JEP2;
import jep2.JEP2Exception;
import jep2.JEP2.JEP2Trouble;
import jep2.JKey;
import type.JIterable;
import type.JObjectI;
import type.JObjectsVector;

/**
 * @author Salvatore Tummarello
 *
 */
public class JSpecialFunctionFactory {

	private static final JKey RANGE = new JKey(JSpecialFunctionFactory.class, "range");

	public Vector<JFunction> getFunctions() {
		Vector<JFunction> v = new Vector<>();
		v.add(new RangeFunction(RANGE));
		return v;
	}

	private class RangeFunction extends JFunction {

		public RangeFunction(JKey key) {
			super(key, -1);
		}

		@Override
		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			if (curNumberOfParameters < 1 || curNumberOfParameters > 3)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_RANGE, getName());

			long min = 0;
			long max = 0;
			long step = 1;
			
			switch (curNumberOfParameters) {
			case 1:
				max = popLong(inStack);
				break;
			case 2:
				max = popLong(inStack);
				min = popLong(inStack);
				break;
			case 3:
				step = popLong(inStack);
				max = popLong(inStack);
				min = popLong(inStack);
			default:
				break;
			}

			JObjectsVector v = new JObjectsVector();
			// check that range is not too big
			JIterable.Checker.checkLength(Math.abs((max - min)/step));
			
			if (step>0 && min < max) {
				long idx = min;
				while (idx < max) {
					v.add(JEP2.createNumber(idx));
					idx += step;
				}
			} else if (step<0 && max < min) {
				long idx = min;
				while (idx > max) {
					v.add(JEP2.createNumber(idx));
					idx += step;
				}
			} 
			return JEP2.createList(v);
		}
	}
}
