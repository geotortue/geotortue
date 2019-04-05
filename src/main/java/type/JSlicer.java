/**
 * 
 */
package type;

import java.util.Vector;

import org.nfunk.jep.addon.JEPException;

import jep2.JEP2;
import jep2.JEP2.JEP2Trouble;

/**
 * @author Salvatore Tummarello
 *
 */
public class JSlicer implements JObjectI<Vector<Integer>> {

	private final int min;
	private final int max;
	private final int step;

	public JSlicer(JObjectI<?> x, JObjectI<?> y) throws JEPException {
		switch (x.getType()) {
		case NULL:
			this.min = Integer.MIN_VALUE;
			this.step = 1;
			if (y.getType()==JEP2Type.NULL)
				this.max = Integer.MAX_VALUE;
			else
				this.max = (int) JEP2.getLong(y);
			break;
		case LONG:
			this.min = ((JInteger) x).intValue();
			this.step = 1;
			if (y.getType()==JEP2Type.NULL)
				this.max = Integer.MAX_VALUE;
			else
				this.max = (int) JEP2.getLong(y);
			break;
		case SLICER:
			JSlicer s = (JSlicer) x;
			this.min = s.min;
			this.max = s.max;
			if (y.getType()==JEP2Type.NULL)
				this.step = 1;
			else 
				this.step = (int) JEP2.getLong(y);
			if (this.step == 0)
				throw new JEPException(JEP2Trouble.JEP2_ZERO_STEP);
			break;
		default:
			throw new JEPException(JEP2Trouble.JEP2_NOT_AN_INT, x.toString());
		}
	}

	@Override
	public type.JObjectI.JEP2Type getType() {
		return JEP2Type.SLICER;
	}

	@Override
	public JObjectI<?> add(JObjectI<?> o) throws JEPException {
		throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_SUM, toString(), o.toString());
	}

	@Override
	public JObjectI<?> mul(JObjectI<?> o) throws JEPException {
		throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_PROD, toString(), o.toString());
	}

	@Override
	public Vector<Integer> getValue() { // Don't use this method but getInterval
		System.err.println("JSlice.getValue() : illegal use");
		new Exception().printStackTrace();
		return null;
	}

	@Override
	public boolean isIterable() {
		return false;
	}

	@Override
	public boolean isANumber() {
		return false;
	}

	public Vector<Integer> getInterval(int n) throws JEPException {
		Vector<Integer> v = new Vector<Integer>();
		int start = min;
		if (start == Integer.MIN_VALUE)
			start = (step>0) ? 0: n-1;
		else
			if (start<0)
				start +=n;
		
		int end = max;
		if (end == Integer.MAX_VALUE)
			end = (step>0) ? n : -1;
		else if (end<0)
			end +=n;

		JIterable.Checker.checkLength(Math.abs((start - end)/step));
		
		if (step>0) {
			int idx = (start < 0) ? 0 : start;
			while (idx < end) {
				v.add(idx);
				idx += step;
			}
		} else {
			int idx = (start > n-1) ? n-1 : start;
			while (idx > end) {
				v.add(idx);
				idx += step;
			}
		}
		return v;
	}

	@Override
	public String toString() {
		return "[" + min + " : " + max + " : " + step+"]";
	}

	/**
	 * @param s
	 * @return
	 */
	public boolean isTheSameAs(JSlicer s) {
		if (min != s.min)
			return false;
		if (max != s.max)
			return false;
		if (step != s.step)
			return false;
		return true;
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