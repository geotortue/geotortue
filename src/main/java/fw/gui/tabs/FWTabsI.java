/**
 * 
 */
package fw.gui.tabs;

interface FWTabsI<T extends Object> {

	public void updatePane(T item);
	
	public T addNewItem();

	public T getItem(int idx);

	public boolean removeItem(T item);

	public boolean moveItem(T item, int idx);
	
	public String getTitle(T item);

	public T copyItem(T item);
}