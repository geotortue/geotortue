/*****************************************************************************

 JEP 2.4.1, Extensions 1.1.1
      April 30 2007
      (c) Copyright 2007, Nathan Funk and Richard Morris
      See LICENSE-*.txt for license information.

*****************************************************************************/
package org.nfunk.jep.function;

import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.type.Complex;

/**
 * Log bass 10.
 * <p>
 * RJM change return real results for positive real arguments.
 * Speedup by using static final fields.
 */
public class Logarithm extends PostfixMathCommand
{
	private static final double LOG10 = Math.log(10);
	private static final Complex CLOG10 = new Complex(Math.log(10),0);
	
	public Logarithm() {
		numberOfParameters = 1;
	}

	public void run(final Stack<Object> inStack) throws ParseException {
		checkStack(inStack);
		final Object param = inStack.pop();
		inStack.push(log(param));
	}
	
	public Object log(final Object param) throws ParseException {
		if (param instanceof Complex) {
		   return ((Complex) param).log().div(CLOG10);
		}
		
		if (param instanceof Number) {
			final double num = ((Number) param).doubleValue();

			// Now returns NaN as it
			if (Double.isNaN(num)) {
				return num;
			}

			if (num >= 0) {
				return Double.valueOf(Math.log(num) / LOG10);
			}

			// Now returns Complex if param is < 0
			final Complex temp = new Complex(num);
			return temp.log().div(CLOG10);
		}

		throw new ParseException("Invalid parameter type");
	}

}
