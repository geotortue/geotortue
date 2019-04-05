package fw.gui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;



public class HorizontalCenteredFlowLayout extends BasicLayoutAdapter {

	int hgap;
	
	public HorizontalCenteredFlowLayout(int gap){
		this.hgap=gap;
	}
	
	protected void init(Container parent) {
		super.init(parent);
		int w = 0; 
		Component[] components = parent.getComponents();
		for (int idx = 0; idx < components.length; idx++) {
			Component c = components[idx];
			w += c.getWidth();
		}
		currX = hgap/2 + parentW/2 - (w + parent.getComponentCount()*hgap)/2;
	}
	
	@Override
	public void layoutComponent(Component c, int idx) {
		int cellW = c.getPreferredSize().width;
		c.setBounds(currX, insets.top, cellW, parentH);
		currX += cellW + hgap;
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		Component[] components = parent.getComponents();
		int width = parent.getInsets().left;
		int height = 0;
		for (int idx = 0; idx < components.length; idx++) {
			Component c = components[idx];
			Dimension preferredSize = c.getPreferredSize(); 
			int cellW = preferredSize.width;
			width += cellW + hgap;
			if (preferredSize.height>height)
				height = preferredSize.height;
		}
		return new Dimension(width, height);
	}
	
	public static JPanel createPanel(int hgap, JComponent... components){
		JPanel pane = new JPanel(new HorizontalCenteredFlowLayout(hgap));
		if (components!=null)
			for(JComponent c : components)
				pane.add(c);
		return pane;
	}
	
	public static JPanel createPanel(JComponent... components){
		return createPanel(10, components);
	}
}
