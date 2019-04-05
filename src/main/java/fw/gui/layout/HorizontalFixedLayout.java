package fw.gui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;


public class HorizontalFixedLayout extends BasicLayoutAdapter {
	private int hgap;
	
	public HorizontalFixedLayout(int hgap){
		this.hgap = hgap;
	}
	
	@Override
	public void layoutComponent(Component c, int idx) {
	}

	public void layoutContainer(Container parent) {
		int componentCount = parent.getComponentCount();
		if (componentCount==0)
			return;
		
		init(parent);
		currX += hgap;
		
		int w0 = (parentW-(componentCount+1)*hgap)/componentCount;
		
		Component[] components = parent.getComponents();
		for (int idx = 0; idx < components.length; idx++) {
			Component c = components[idx];
			int h0 = c.getPreferredSize().height;
			c.setBounds(currX, insets.top+(parentH-h0)/2, w0, h0);
			currX += hgap + w0;
		}
	}

	public Dimension minimumLayoutSize(Container parent) {
		Component[] components = parent.getComponents();
		int n = components.length;
		int mWidth = 0;
		int mHeight = 0;
		for (int idx = 0; idx < n; idx++){
			Dimension d = components[idx].getMinimumSize();
			if (d.width>mWidth)
				mWidth = d.width;
			if (d.height>mHeight)
				mHeight = d.height;
		}
		return new Dimension(n*mWidth+(n+1)*hgap, mHeight);
	}

	public static JPanel createPanel(int hgap, JComponent... components){
		JPanel pane = new JPanel(new HorizontalFixedLayout(hgap));
		if (components!=null)
			for(Component c : components)
				pane.add(c);
		return pane;
	}
}