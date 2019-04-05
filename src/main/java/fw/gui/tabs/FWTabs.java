/**
 * 
 */
package fw.gui.tabs;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 */
public class FWTabs<T extends Object> extends JPanel implements FWTabsI<T>  {
	
	private static final long serialVersionUID = -4117024592470334500L;
	
	private final TabHead<T> head;
	private FWTabItemSupplier<T> supplier;

	public FWTabs(FWTabItemSupplier<T> s) {
		this.supplier = s;
		this.head = new TabHead<T>(this);
		setLayout(new BorderLayout());
		head.init(supplier.getItems());
	}
	
	public void setSupplier(FWTabItemSupplier<T> supplier) {
		this.supplier = supplier;
		head.init(supplier.getItems());
	}

	@Override
	public void updatePane(final T item) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				removeAll();
				add(head, BorderLayout.NORTH);
				add(supplier.getPane(item), BorderLayout.CENTER);
				validate();		
			}
		});
		
	}


	@Override
	public T addNewItem() {
		return supplier.addNewItem();
	}
	
	@Override
	public T getItem(int idx) {
		return supplier.getItems().elementAt(idx);
	}

	@Override
	public boolean removeItem(T item) {
		return supplier.removeItem(item);
	}
	
	@Override
	public boolean moveItem(T item, int idx) {
		return supplier.moveItem(item, idx);
	}


	@Override
	public String getTitle(T item) {
		return supplier.getTitle(item);
	}

	@Override
	public T copyItem(T item) {
		return supplier.copyItem(item);
		
	}
	
	
}
