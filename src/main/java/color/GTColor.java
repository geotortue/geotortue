/**
 * 
 */
package color;

import java.awt.Color;

import org.nfunk.jep.addon.JEPException;

import jep2.JEP2.JEP2Trouble;
import type.JAbstractObject;
import type.JObjectI;

/**
 * @author Salvatore Tummarello
 *
 */
public class GTColor extends JAbstractObject<Color> {

	public GTColor(Color value) {
		super(value);
	}

	@Override
	public JEP2Type getType() {
		return JEP2Type.COLOR;
	}
	
	public JObjectI<?> add(JObjectI<?> o) throws JEPException {
		throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_SUM, toString(), o.toString());
	}

	@Override
	public JObjectI<?> mul(JObjectI<?> o) throws JEPException {
		throw new JEPException(JEP2Trouble.JEP2_ILLEGAL_PROD, toString(), o.toString());
	}
	
	 
	@Override
	public boolean isIterable() {
		return false;
	}

	 
	@Override
	public boolean isANumber() {
		return false;
	}
	
	public String format() {
		Color c = getValue();
		return "( r="+c.getRed()+", v="+c.getGreen()+" b="+c.getBlue()+" )";
	}
}
