/**
 * 
 */
package function;

import java.util.Stack;

import org.nfunk.jep.addon.JEPException;

import jep2.JEP2;
import jep2.JEP2.JEP2Trouble;
import jep2.JEP2Exception;
import jep2.JKey;
import type.JInteger;
import type.JObjectI;

public class Binomial extends JFunction {

	public Binomial(JKey key) {
		super(key, 2);
	}

	public JObjectI<?> getResult(Stack<Object> inStack) throws JEPException {
		long k = popLong(inStack);
		long n = popLong(inStack);
		if (n < 0) 
			throw new JEP2Exception(this, JEP2Trouble.JEP2_BINOM, n+"", k+"");
		if (k < 0 || k > n)
			return JEP2.createNumber(0L);
		
		try {
			return JEP2.createNumber(binomAsLong(n, k));
		} catch (ArithmeticException ex) {
			return JEP2.createNumber(binomAsDouble(n, k));
		}
	}

	private static long binomAsLong(long n, long k) throws ArrayIndexOutOfBoundsException {
		if (n==0 || k==0)
			return 1L;
		if (2*k>n)
			return binomAsLong(n, n-k);
		else return ((long) JInteger.mulAndCheck(n, binomAsLong(n-1, k-1)).longValue())/k; // TUR
	}
	
	private static double binomAsDouble(long n, long k) throws ArrayIndexOutOfBoundsException {
		if (n==0 || k==0)
			return 1;
		if (2*k>n)
			return binomAsLong(n, n-k);
		else return n*binomAsDouble(n-1, k-1)/k;
	}
}
