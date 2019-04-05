package fw.gui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;



public class HorizontalFlowLayout extends BasicLayoutAdapter {

	int hgap;
	
	public HorizontalFlowLayout(int gap){
		this.hgap=gap;
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
		JPanel pane = new JPanel(new HorizontalFlowLayout(hgap));
		if (components!=null)
			for(JComponent c : components)
				pane.add(c);
		return pane;
	}
	
	public static JPanel createPanel(JComponent... components){
		return createPanel(10, components);
	}
}
