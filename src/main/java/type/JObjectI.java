/**
 * 
 */
package type;

import org.nfunk.jep.addon.JEPException;

/**
 * @author Salvatore Tummarello
 *
 */
public interface JObjectI<T extends Object> {

	public enum JEP2Type {NULL, DOUBLE, STRING, LIST, BOOLEAN, LONG, SLICER, METHOD, ASSIGNMENT, DICT, MUSIC, COLOR}
	
	public JEP2Type getType();
	
	public T getValue();

	public JObjectI<?> add(JObjectI<?> o) throws JEPException;

	public JObjectI<?> mul(JObjectI<?> prod) throws JEPException;
	
	public boolean isIterable();
	
	public boolean isANumber();

	public boolean isHashable();

	public boolean isMutable();

}
