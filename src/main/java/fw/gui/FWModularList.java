package fw.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.Translator.TKey;
import fw.gui.layout.BasicLayoutAdapter;
import fw.gui.layout.HorizontalFixedLayout;
import fw.gui.layout.VerticalFlowLayout;


public abstract class FWModularList<T> extends JPanel {
	private static final long serialVersionUID = 2401654851244956535L;
	
	protected int hgap = 4;
	protected int vgap = 6;
	protected int buttonSize = 20;
	final private Hashtable<T, JPanel> table = new Hashtable<T, JPanel>();
	private final  ActionKey removeKey;
	
	public FWModularList(ActionKey addKey, ActionKey removeKey, int gapW, int gapH, TKey... headings){
		super(new VerticalFlowLayout(gapH));
		this.hgap = gapW;
		this.vgap = gapH;
		this.removeKey = removeKey;
		
		JPanel head = new JPanel(new Layout());
		FWLabel[] labels = new FWLabel[headings.length];
		for (int idx = 0; idx < headings.length; idx++)
			labels[idx] = new FWLabel(headings[idx], SwingConstants.CENTER);
		head.add(HorizontalFixedLayout.createPanel(hgap, labels));

		JButton addItemButton = new FWAction(addKey, "list-add.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addAnItem(getDefaultItem());
				refresh();
			}
		}).getButton();
		
		addItemButton.setText("");
		head.add(addItemButton);
		add(head);
	}
	
	public FWModularList(ActionKey addKey, ActionKey removeKey, TKey... headings){
		this(addKey, removeKey, 4, 6, headings);
	}
	
	private void addAnItem(T item){
		if (item == null)
			return;
		JPanel itemPane = new JPanel(new Layout());
		itemPane.add(HorizontalFixedLayout.createPanel(hgap, getItemComponents(item)));
		itemPane.add(createRemoveItemButton(item));
		add(itemPane);
		table.put(item, itemPane);
	}
	
	public void addItem(T item){
		addAnItem(item);
		refresh();
	}
	
	public void addItem(T[] items){
		for (T item : items)
			addAnItem(item);
		refresh();
	}
	
	private void refresh() {
		if (getTopLevelAncestor()!=null) 
			getTopLevelAncestor().validate();
		repaint();
	}

	protected abstract T getDefaultItem();
	
	protected abstract JComponent[] getItemComponents(T item);
	
	protected abstract boolean removeItem(T item, Window owner);

		
	private JButton createRemoveItemButton(final T item){
		JButton removeItemButton = new JButton(new FWAction(removeKey, "list-remove.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (removeItem(item, (Window) FWModularList.this.getTopLevelAncestor()))
					remove(table.get(item));
				refresh();
			}
		}));
		removeItemButton.setText("");
		return removeItemButton;
	}
	
	/*
	 * LAYOUT
	 */
	
	private class Layout extends BasicLayoutAdapter {
		
		private int h0, w0;
		
		@Override
		protected void init(Container parent) {
			super.init(parent);
			h0 = Math.max(parent.getComponent(0).getPreferredSize().height, buttonSize);
			w0 = parentW-buttonSize-3*hgap;
		}

		@Override
		public void layoutComponent(Component c, int idx) {
			if (idx==0) 
				c.setBounds(insets.left+hgap, insets.top, w0, c.getPreferredSize().height);
			if (idx==1)
				c.setBounds(insets.left+2*hgap+w0, insets.top+(h0-buttonSize)/2, buttonSize, buttonSize);
		}

		public Dimension minimumLayoutSize(Container parent) {
			Component[] components = parent.getComponents();
			if (components.length!=2)
				return new Dimension(200, buttonSize);
			
			int mHeight=components[0].getPreferredSize().height;
			if (mHeight<buttonSize)
				mHeight=buttonSize;
			
			return new Dimension(getWidth(), mHeight);
		}
	}
}