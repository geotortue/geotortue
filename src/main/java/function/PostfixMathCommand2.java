/**
 * 
 */
package function;

import java.util.EmptyStackException;
import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.addon.JEPException;
import org.nfunk.jep.function.PostfixMathCommand;

import jep2.JEP2;
import jep2.JEP2.JEP2Trouble;
import type.JIterable;
import type.JList;
import type.JNullObject;
import type.JNumber;
import type.JObjectI;
import type.JString;

/**
 * @author Salvatore Tummarello
 *
 */
public abstract class PostfixMathCommand2 extends PostfixMathCommand {

	public PostfixMathCommand2(int numberOfparam) {
		this.numberOfParameters = numberOfparam;
	}
	
	public void run(Stack<Object> inStack) throws JEPException {
		try {
			checkStack(inStack); // check the stack
		} catch (ParseException ex) {
			// should not occur
			ex.printStackTrace();
			throw new JEPException(JEP2Trouble.JEP2_UNEXPECTED_ERROR);
		}
		inStack.push(getResult(inStack));
	}

	public abstract JObjectI<?> getResult(Stack<Object> inStack) throws JEPException;

	protected JObjectI<?> popJEPObjectI(Stack<Object> inStack) {
		try {
		Object o = inStack.pop();
		if (o instanceof String)
			return JEP2.createString((String) o);
		try {
			return (JObjectI<?>) o;
		} catch (ClassCastException ex) {
			ex.printStackTrace();
			return null;
		}
		} catch (EmptyStackException ex) {
			ex.printStackTrace();
			return JNullObject.NULL_OBJECT;
		}
	}

	protected long popLong(Stack<Object> inStack) throws JEPException {
		JObjectI<?> o = popJEPObjectI(inStack);
		return JEP2.getLong(o);
	}

	
	protected double popDouble(Stack<Object> inStack) throws JEPException {
		JObjectI<?> o = popJEPObjectI(inStack);
		return JEP2.getDouble(o);
	}

	protected JNumber<?> popNumber(Stack<Object> inStack) throws JEPException {
		JObjectI<?> o = popJEPObjectI(inStack);
		return JEP2.getNumber(o);
	}
	
	protected boolean popBoolean(Stack<Object> inStack) throws JEPException {
		JObjectI<?> o = popJEPObjectI(inStack);
		return JEP2.getBoolean(o);
	}

	protected JList popList(Stack<Object> inStack) throws JEPException {
		JObjectI<?> o = popJEPObjectI(inStack);
		return JEP2.getList(o);
	}
	
	protected JString popString(Stack<Object> inStack) throws JEPException {
		JObjectI<?> o = popJEPObjectI(inStack);
		return JEP2.getString(o);
	}
	
	protected JIterable popIterable(Stack<Object> inStack) throws JEPException {
		JObjectI<?> o = popJEPObjectI(inStack);
		return JEP2.getIterable(o);
	}

}