/**
 * 
 */
package type;

import java.util.Stack;

import org.nfunk.jep.addon.JEPException;

import function.JFunction;
import jep2.JEP2.JEP2Trouble;

/**
 * @author Salvatore Tummarello
 *
 */
public class JMethod extends JAbstractObject<JFunction> {

	
	public JMethod(JFunction value) {
		super(value);
	}

	@Override
	public type.JObjectI.JEP2Type getType() {
		return JEP2Type.METHOD;
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

	/**
	 * @param o
	 * @return
	 * @throws JEPException 
	 */
	public JObjectI<?> getResult(JObjectI<?> o) throws JEPException {
		Stack<Object> inStack = new Stack<>();
		inStack.push(o);
		return getValue().getResult(inStack);
	}
	

}
