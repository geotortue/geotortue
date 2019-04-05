/**
 * 
 */
package type;

import org.nfunk.jep.addon.JEPException;

/**
 * @author Salvatore Tummarello
 *
 */
public class JBoolean extends JAbstractHashable<Boolean> {

	public final static JBoolean TRUE = new JBoolean(true);
	public final static JBoolean FALSE = new JBoolean(false);
	
	
	JBoolean(Boolean value) {
		super(value);
	}

	@Override
	public type.JObjectI.JEP2Type getType() {
		return JEP2Type.BOOLEAN;
	}

	@Override
	public JObjectI<?> add(JObjectI<?> o) throws JEPException {
		return getValue() ? TRUE : o;
	}

	@Override
	public JObjectI<?> mul(JObjectI<?> o) throws JEPException {
		return getValue() ? o : FALSE;
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
	public Object getHashValue() {
		return getValue();
	}
}
