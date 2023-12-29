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

public class TanH extends PostfixMathCommand
{
	public TanH()
	{
		numberOfParameters = 1;
	}

	public void run(final Stack<Object> inStack)
		throws ParseException
	{
		checkStack(inStack);
		final Object param = inStack.pop();
		inStack.push(tanh(param));
	}

	public Object tanh(final Object param)
		throws ParseException
	{
		if (param instanceof Complex)
		{
			return ((Complex) param).tanh();
		}
		
		if (param instanceof Number)
		{
			final double value = ((Number) param).doubleValue();
			return Double.valueOf((Math.exp(value) - Math.exp(-value)) / (Math.pow(Math.E, value) + Math.pow(Math.E, -value)));
		}
		throw new ParseException("Invalid parameter type");
	}

}
