/**
 * 
 */
package type;

import java.util.Vector;

public class JObjectsVector extends Vector<JObjectI<?>> {

	private static final long serialVersionUID = -933415872950496418L;

	public JObjectsVector() {
		super();
	}
	
	 public synchronized void insertElementAt(JObjectI<?> obj, int index) {
		 super.insertElementAt(obj, index);
	 }

}