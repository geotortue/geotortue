/**
 * 
 */
package type;

/**
 * @author Salvatore Tummarello
 *
 */
public interface JMutable {

	public void addJMutableListener(String name, JMutableListener l);
	
	public void notifyListeners();
}
