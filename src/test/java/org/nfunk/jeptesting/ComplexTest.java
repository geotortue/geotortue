package org.nfunk.jeptesting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.type.Complex;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComplexTest {

	@DisplayName("Test Complex Power")
    @Test	
	public void testPower() {
		Complex one = new Complex(1, 0);
		Complex negOne = new Complex(-1, 0);
		// Complex i = new Complex(0, 1);
		Complex two = new Complex(2, 0);
		// Complex negTwo = new Complex(-2, 0);
		// Complex negEight = new Complex(-8, 0);
		
		// multiplication
		assertTrue((one.mul(one)).equals(one,0));
		assertTrue((one.mul(negOne)).equals(negOne,0));
		assertTrue((negOne.mul(one)).equals(negOne,0));

		// power
		assertTrue((one.power(one)).equals(one,0));
		assertTrue((one.power(-1)).equals(one,0));
		assertTrue((one.power(negOne)).equals(one,0));
		assertTrue((negOne.power(two)).equals(one,0));
		//assertTrue((negEight.power(1.0/3)).equals(negTwo,0));
	}

}
