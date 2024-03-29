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
 * Implements the arcTanH function.
 * 
 * @author Nathan Funk
 * @since 2.3.0 beta 2 - Now returns Double result rather than Complex for -1 < x < 1 
 */
public class ArcTanH extends PostfixMathCommand
{
	public ArcTanH()
	{
		numberOfParameters = 1;
	}

	@Override
	public void run(Stack<Object> inStack)
		throws ParseException
	{
		checkStack(inStack);
		Object param = inStack.pop();
		inStack.push(atanh(param));
	}

	public Object atanh(Object param)
		throws ParseException
	{
		if (param instanceof Complex)
		{
			return ((Complex)param).atanh();
		}
		else if (param instanceof Number)
		{
			double val = ((Number)param).doubleValue();
			if(val > -1.0 && val < 1) {
				double res = Math.log((1+val)/(1-val))/2;
				return Double.valueOf(res);
			}
			else
			{
				Complex temp = new Complex(val,0.0);
				return temp.atanh();
			}
		}

		throw new ParseException("Invalid parameter type");
	}

}
