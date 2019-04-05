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
public interface JIterable {
	
	public JObjectsVector getItems();

	public JObjectI<?> elementAt(JList indices) throws JEPException;

	public int len();
	
	public JObjectI<?> changeElementAt(JList idx, JObjectI<?> v) throws JEPException;
	
	public static class Checker {
		
		public static void checkLength(Number value) throws JEPException {
			if (value.doubleValue()>1E5)
				throw new JEPException(JEP2Trouble.JEP2_LIST_OVERFLOW, value.toString());
		}
	}
}
