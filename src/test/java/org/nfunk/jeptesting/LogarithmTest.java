package org.nfunk.jeptesting;

import java.util.Stack;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.Logarithm;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class LogarithmTest {

	/**
	 * Test method for 'org.nfunk.jep.function.Logarithm.run(Stack)'
	 * Tests the return value of log(NaN). This is a test for bug #1177557
	 */
	@DisplayName("Test Logarithm")
    @Test
	public void testLogarithm() {
		final Logarithm logFunction = new Logarithm();
		final Stack<Object> stack = new Stack<>();
		final Double original = Double.valueOf(Double.NaN);
		stack.push(original);
		try {
			logFunction.run(stack);
		} catch (ParseException e) {
			fail();
		}
		final Object value = stack.pop();

		if (value instanceof Double) {
			assertTrue(Double.isNaN(((Double) value).doubleValue()));
		} else {
			fail();
		}
	}

}
