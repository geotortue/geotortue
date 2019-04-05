/**
 * 
 */
package geotortue.core;

import java.util.Observer;

/**
 * @author Salvatore Tummarello
 *
 */
public interface SymbolTableListener extends Observer {
	
	public void itemRemoved(String o);
	
}
