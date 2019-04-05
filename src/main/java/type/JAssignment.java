/**
 * 
 */
package type;

import org.nfunk.jep.addon.JEPException;

import jep2.JEP2.JEP2Trouble;

/**
 * @author Salvatore Tummarello
 *
 */
public class JAssignment implements JObjectI<Object> {

	private final String varName;
	private final Object newValue;
	
	public JAssignment(String varName, Object newValue) {
		this.varName = varName;
		this.newValue = newValue;
	}

	@Override
	public JEP2Type getType() {
		return JEP2Type.ASSIGNMENT;
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
	public boolean isIterable() {
		return false;
	}

	@Override
	public boolean isANumber() {
		return false;
	}

	@Override
	public Object getValue() {
		return newValue;
	}

	public String getVarName() {
		return varName;
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