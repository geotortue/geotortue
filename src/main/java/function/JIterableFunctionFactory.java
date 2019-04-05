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
import type.JObjectHelper;
import type.JObjectI;
import type.JObjectsVector;

/**
 * @author Salvatore Tummarello
 *
 */
public class JIterableFunctionFactory {
	
	private static final JKey LEN = new JKey(JIterableFunctionFactory.class, "len");
	private static final JKey MIN = new JKey(JIterableFunctionFactory.class, "min");
	private static final JKey MAX = new JKey(JIterableFunctionFactory.class, "max");
	private static final JKey SUM = new JKey(JIterableFunctionFactory.class, "sum");
	private static final JKey PROD = new JKey(JIterableFunctionFactory.class, "prod");

	private abstract class AbstractIterableFunction extends JFunction {

		public AbstractIterableFunction(JKey key) {
			super(key, 1);
		}

		public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
			JObjectI<?> o = popJEPObjectI(inStack);
			return getResult(JEP2.getIterable(o));
		}

		public abstract JObjectI<?> getResult(JIterable o) throws JEPException;
	}
	
	public Vector<JFunction> getFunctions() {
		Vector<JFunction> v = new Vector<>();
		v.add(new LengthFunction(LEN));
		v.add(new MinFunction(MIN));
		v.add(new MaxFunction(MAX));
		v.add(new SumFunction(SUM));
		v.add(new ProdFunction(PROD));
		return v;
	}

	private class LengthFunction extends AbstractIterableFunction {

		public LengthFunction(JKey key) {
			super(key);
		}

		@Override
		public JObjectI<?> getResult(JIterable o) throws JEPException {
			return JEP2.createNumber((long) o.len());
		}
	}
	
	private class MinFunction extends AbstractIterableFunction {

		public MinFunction(JKey key) {
			super(key);
		}

		@Override
		public JObjectI<?> getResult(JIterable it) throws JEPException {
			int len = it.len();
			if (len<=0)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_NOT_ITERABLE, it.toString());
			JObjectsVector v = it.getItems();
			JObjectI<?> min = v.elementAt(0);
			for (int idx = 1; idx < it.len() ; idx++) {
				JObjectI<?> el = v.elementAt(idx);
				if (JObjectHelper.compare(el, min)<0)
					min = el;
			}
			return min;
		}
	}
	
	private class MaxFunction extends AbstractIterableFunction {

		public MaxFunction(JKey key) {
			super(key);
		}

		@Override
		public JObjectI<?> getResult(JIterable it) throws JEPException {
			int len = it.len();
			if (len<=0)
				throw new JEP2Exception(this, JEP2Trouble.JEP2_NOT_ITERABLE, it.toString());
			JObjectsVector v = it.getItems();
			JObjectI<?> max = v.elementAt(0);
			for (int idx = 1; idx < it.len() ; idx++) {
				JObjectI<?> el = v.elementAt(idx);
				if (JObjectHelper.compare(el, max)>0)
					max = el;
			}
			return max;
		}
	}
	
	
	private class SumFunction extends AbstractIterableFunction {

		public SumFunction(JKey key) {
			super(key);
		}

		@Override
		public JObjectI<?> getResult(JIterable it) throws JEPException {
			int len = it.len();
			if (len<=0)
				return JEP2.createNumber(0L);;
			JObjectsVector v = it.getItems();
			
			JObjectI<?> sum = v.elementAt(0);
			for (int idx = 1; idx < it.len() ; idx++) 
				sum = sum.add(v.elementAt(idx));
			return sum;
		}
	}

	private class ProdFunction extends AbstractIterableFunction {

		public ProdFunction(JKey key) {
			super(key);
		}

		@Override
		public JObjectI<?> getResult(JIterable it) throws JEPException {
			int len = it.len();
			if (len<=0)
				return JEP2.createNumber(0L);;
			JObjectsVector v = it.getItems();
			
			JObjectI<?> sum = v.elementAt(0);
			for (int idx = 1; idx < it.len() ; idx++) 
				sum = sum.mul(v.elementAt(idx));
			return sum;
		}
	}
}