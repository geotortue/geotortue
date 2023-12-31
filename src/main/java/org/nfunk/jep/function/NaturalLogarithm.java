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
 * Natural logarithm.
 *
 * RJM Change: fixed so ln(positive Double) is Double.
 */
public class NaturalLogarithm extends PostfixMathCommand
{
	public NaturalLogarithm()
	{
		numberOfParameters = 1;

	}

	public void run(Stack<Object> inStack)
		throws ParseException
	{
		checkStack(inStack);// check the stack
		Object param = inStack.pop();
		inStack.push(ln(param));//push the result on the inStack
	}

	public Object ln(Object param)
		throws ParseException
	{
		if (param instanceof Complex)
		{
			return ((Complex) param).log();
		}
		
		if (param instanceof Number)
		{
			final double num = ((Number) param).doubleValue();

			// Now returns NaN as it
			if (Double.isNaN(num)) {
				return num;
			}

			if (num >= 0) {
				return Double.valueOf(Math.log(num));
			}
			
			// Now returns Complex if param is < 0
			final Complex temp = new Complex(num);
			return temp.log();
		}

		throw new ParseException("Invalid parameter type");
	}
}
