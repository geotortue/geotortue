package fw.gui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;



public class VerticalFlowLayout extends BasicLayoutAdapter {

	private int vgap, vgap0, width, margin;
	
	public VerticalFlowLayout(int gap0, int gap){
		this.vgap0=gap0;
		this.vgap=gap;
	}
	
	public VerticalFlowLayout(int gap){
		this(0, gap);
	}
	
	@Override
	protected void init(Container parent) {
		super.init(parent);
		currY += vgap + vgap0;
		width = getWidth(parent);
		margin = getLeftOffset(width);
	}
	
	protected int getLeftOffset(int width) {
		return insets.left+(parentW-width)/2;
	}

	@Override
	public void layoutComponent(Component c, int idx) {
			int cellH = c.getPreferredSize().height;
			c.setBounds(margin, currY, width, cellH);
			currY += cellH + vgap;
	}
	
	protected int getWidth(Container parent) {
		return parentW-12;
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		Component[] components = parent.getComponents();
		int width = 0;
		int height = parent.getInsets().top+vgap;
		for (int idx = 0; idx < components.length; idx++) {
			Component c = components[idx];
			Dimension preferredSize = c.getPreferredSize(); 
			int cellH = preferredSize.height;
			height += cellH + vgap;
			if (preferredSize.width>width)
				width = preferredSize.width;
		}
		return new Dimension(width, height);
	}
	
	public static JPanel createPanel(int vgap0, int vgap, JComponent... components){
		JPanel pane = new JPanel(new VerticalFlowLayout(vgap0, vgap));
		if (components!=null)
			for(JComponent c : components)
				pane.add(c);
		return pane;
	}
	
	public static JPanel createPanel(int vgap, JComponent... components){
		return createPanel(0, vgap, components);
	}
	public static JPanel createPanel(JComponent... components){
		return createPanel(10, components);
	}
}