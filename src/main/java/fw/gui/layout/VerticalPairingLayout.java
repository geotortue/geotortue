package fw.gui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JPanel;


public class VerticalPairingLayout extends BasicLayoutAdapter {

	private int hgap, vgap, leftCellW, rightCellW;

	public VerticalPairingLayout(boolean span, int hgap, int vgap) {
		this.span = span;
		this.hgap = hgap;
		this.vgap = vgap;
		this.leftCellW = 0;
		this.rightCellW = 0;
	}
	
	public VerticalPairingLayout(int hgap, int vgap) {
		this(false, hgap, vgap);
	}
	
	private final boolean span;
	private int margin;

	@Override
	protected void init(Container parent) {
		super.init(parent);
		currY += vgap;
		Component[] components = parent.getComponents();

		if (span) {
			leftCellW = (parentW - 3*hgap)/2;
			rightCellW = leftCellW ;
		} else 
			computeCellWidths(components);
		
		margin = getLeftOffset(hgap);
	}

	protected int getLeftOffset(int hgap) {
		if (span)
			return insets.left;
		int m = (parentW- 3*hgap - leftCellW - rightCellW ) / 2;
		if (m > 0) 
			return m;
		return 0;
	}
	
	@Override
	public void layoutComponent(Component c, int idx) {
	}

	public void layoutContainer(Container parent) {
		init(parent);
		Component[] components = parent.getComponents();

		for (int idx = 0; idx < components.length; idx++) {
			Component leftComponent = components[idx];
			int leftCellH = leftComponent.getPreferredSize().height;

			idx++;
			if (idx==components.length) {
				leftComponent.setBounds(margin, currY, leftCellW, leftCellH);
				return;
			}
			
			Component rightComponent = components[idx];
			int rightCellH = rightComponent.getPreferredSize().height;
			
			int cellH = Math.max(leftCellH, rightCellH);

			leftComponent.setBounds(margin, currY, leftCellW, cellH);
			rightComponent.setBounds(margin + hgap + leftCellW, currY, rightCellW, cellH);

			currY += cellH + vgap;
		}
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		Component[] components = parent.getComponents();
		Insets insets = parent.getInsets();
		int currY = insets.top + vgap;
		for (int idx = 0; idx < components.length/2; idx++) {
			Component leftComponent = components[2*idx];
			Component rightComponent = components[2*idx + 1];
			int leftCellH = leftComponent.getPreferredSize().height;
			int rightCellH = rightComponent.getPreferredSize().height;
			int cellH = Math.max(leftCellH, rightCellH);

			currY += cellH + vgap;
		}
		currY+=vgap+insets.bottom;
		computeCellWidths(components);
		return new Dimension(insets.left+insets.right+3*hgap+leftCellW+rightCellW, currY);
	}

	private void computeCellWidths(Component[] components) {
		leftCellW = 0;
		rightCellW = 0;
		for (int idx = 0; idx < components.length; idx++) {
			Component leftComponent = components[idx];
			idx++;
			
			if (idx==components.length) {
				leftCellW = Math.max(leftCellW, leftComponent.getPreferredSize().width+1);
				return;
			}
			
			Component rightComponent = components[idx];
			leftCellW = Math.max(leftCellW, leftComponent.getPreferredSize().width+1);
			rightCellW = Math.max(rightCellW, rightComponent.getPreferredSize().width+1);
		}
	}
	
	public static JPanel createPanel(boolean span, int hgap, int vgap, Component... components){
		JPanel pane = new JPanel(new VerticalPairingLayout(span, hgap, vgap));
		for(Component c : components)
			pane.add(c);
		pane.doLayout();
		return pane;
	}
	
	public static JPanel createPanel(int hgap, int vgap, Component... components){
		return createPanel(false, hgap, vgap, components);
	}
	
	public static JPanel createPanel(Component... components){
		return createPanel(10, 10, components);
	}
}