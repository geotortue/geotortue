/**
 * 
 */
package fw.gui.tabs;

import java.util.Vector;

import javax.swing.JPanel;

/**
 *
 */
public interface FWTabItemSupplier<T extends Object> {

	public JPanel getPane(T t);

	public T addNewItem();
	
	public Vector<T> getItems();

	public boolean removeItem(T item);

	public boolean moveItem(T item, int idx);

	public String getTitle(T item);

	public T copyItem(T item);
}
