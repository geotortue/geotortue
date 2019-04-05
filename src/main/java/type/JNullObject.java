/**
 * 
 */
package type;

import org.nfunk.jep.addon.JEPException;

/**
 * @author Salvatore Tummarello
 *
 */
public class JNullObject extends JAbstractObject<Object> {

	public final static JNullObject NULL_OBJECT = new JNullObject();
	
	private JNullObject() {
		super(new Object());
	}

	@Override
	public type.JObjectI.JEP2Type getType() {
		return JEP2Type.NULL;
	}

	@Override
	public JObjectI<?> add(JObjectI<?> o) throws JEPException {
		return NULL_OBJECT;
	}

	@Override
	public JObjectI<?> mul(JObjectI<?> prod) throws JEPException {
		return NULL_OBJECT;
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
	public String toString() {
		return "___null___";
	}
}
