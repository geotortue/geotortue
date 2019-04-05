package fw.gui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;


public abstract class BasicLayoutAdapter implements LayoutManager {

	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(100, 50);
	}

	public Dimension preferredLayoutSize(Container parent) {
		return minimumLayoutSize(parent);
	}

	public void addLayoutComponent(String str, Component c) {
	}

	public void removeLayoutComponent(Component c) {
	}
	
	public void addLayoutComponent(Component comp, Object constraints) {
	}

//	@Override
//	public Dimension maximumLayoutSize(Container target) {
//		return maximumLayoutSize(target);
//	}
//
//	@Override
//	public float getLayoutAlignmentX(Container target) {
//		return target.getAlignmentX();
//	}
//
//	@Override
//	public float getLayoutAlignmentY(Container target) {
//		return target.getAlignmentY();
//	}
//
//	@Override
//	public void invalidateLayout(Container target) {
//		layoutContainer(target);
//	}



	protected int currX = 0, currY = 0;
	protected int parentW, parentH;
	protected Insets insets;
	
	protected void init(Container parent) {
		insets = parent.getInsets();
		currX = insets.left;
		currY = insets.top;
		
		parentW = parent.getWidth() - insets.left - insets.right; 
		parentH = parent.getHeight() - insets.top - insets.bottom;
	}
	
	public void layoutContainer(Container parent) {
		init(parent);
		synchronized (parent.getTreeLock()) {
			Component[] components = parent.getComponents();
			for (int idx = 0; idx < components.length; idx++) {
				layoutComponent(components[idx], idx);
			}
		}
	}
	
	protected int getMaxComponentWidth(Container parent) {
		int w = 0;
		synchronized (parent.getTreeLock()) {
			Component[] components = parent.getComponents();
			for (int idx = 0; idx < components.length; idx++)
				w = Math.max(w,  components[idx].getPreferredSize().width);
		}
		return w;
	}

	public abstract void layoutComponent(Component c, int idx);

}
