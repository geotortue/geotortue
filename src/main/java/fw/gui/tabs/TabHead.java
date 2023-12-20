/**
 * 
 */
package fw.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.FWToolKit;
import fw.gui.FWButton;
import fw.gui.FWMouseListener;
import fw.gui.layout.BasicLayoutAdapter;
import fw.util.swing.SwingUtilities2;

/**
 */
class TabHead<T extends Object> extends JLayeredPane  {
	
	private static final ActionKey COPY_TAB = new ActionKey(TabHead.class, "copyTab");
	private static final ActionKey PREVIOUS_TAB = new ActionKey(TabHead.class, "previousTab");
	private static final ActionKey NEXT_TAB = new ActionKey(TabHead.class, "nextTab");
	private static final ActionKey ADD_TAB = new ActionKey(TabHead.class, "addTab");
	private static final ActionKey REMOVE_TAB = new ActionKey(TabHead.class, "removeTab");

	private static final long serialVersionUID = -8955153752369699292L;
	
	private final Font font = UIManager.getFont("FWTabs.font");
	private final FWTabsI<T> itemManager;
	private final Vector<Tab> tabs = new Vector<>();
	private int tabWidth = 120;
	private int tabHeight = 40; 
	private int selectedIdx = 0;
	
	private final FWAction actionRemoveTab = new FWAction(REMOVE_TAB, "close.png", new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			TabHead.this.removeSelectedTab();
			TabHead.this.doLayout();
		}
	});
	private final JButton removeButton = actionRemoveTab.getButton();
	
	private final FWAction actionAddTab = new FWAction(ADD_TAB, "list-add.png", new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			Tab tab = new Tab(itemManager.addNewItem());
			tabs.add(tab);
			add(tab);
			validate();
			selectTab(tab);
			doLayout();
		}
	});

	private final FWAction action_nextTab = new FWAction(NEXT_TAB, "next.png", new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			layout.offset--;
			doLayout();
		}
	});

	private final FWAction action_previousTab = new FWAction(PREVIOUS_TAB, "previous.png", new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			layout.offset++;
			doLayout();
		}
	});
	
	private final FWAction action_copyTab = new FWAction(COPY_TAB, "list-copy.png", new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			Tab tab = tabs.elementAt(selectedIdx);
			T item = itemManager.copyItem(tab.item);
			Tab newTab = new Tab(item);
			tabs.add(selectedIdx+1, newTab);
			add(newTab);
			validate();
			selectTab(newTab);
			doLayout();
		}
	});
	
	private final HeadLayout layout = new HeadLayout();
	private final Vector<CustomButton> buttons = new Vector<>();

	public TabHead(FWTabsI<T> items) {
		this.itemManager = items;
		setLayout(layout);
		FontMetrics fm = getFontMetrics(font);
		tabHeight = 3*fm.getHeight()/2;
		
		removeButton.setText(null);
		removeButton.setRolloverIcon(FWToolKit.getIcon("close-2.png"));
		removeButton.setPreferredSize(new Dimension(22, 22));
		removeButton.setBorderPainted(false);
		removeButton.setContentAreaFilled(false);
		removeButton.setFocusable(false);
		
		buttons.add(new CustomButton(action_copyTab));
		buttons.add(new CustomButton(actionAddTab));
		buttons.add(new CustomButton(action_nextTab));
		buttons.add(new CustomButton(action_previousTab));
		
		for (CustomButton b : buttons)
			add(b);
		
		setPreferredSize(new Dimension(600, tabHeight));
	}
	
	void init(final Vector<T> items) {
		if (items.isEmpty()) {
			new Exception().printStackTrace();
			return;
		}
		if (SwingUtilities.isEventDispatchThread())
			init_(items);
		else
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						init_(items);
					}
				});
			} catch (InvocationTargetException | InterruptedException ex) {
				ex.printStackTrace();
			}
	}
	
	private void init_(final Vector<T> items) {
		removeAll();
		selectedIdx = 0;

		for (CustomButton b : buttons)
			add(b);
		
		Tab tab = null;
		tabs.clear();
		for (T item : items) {
			tab = new Tab(item);
			tabs.add(tab);
			add(tab);
		}
		validate();
		selectTab(tab);
	}

	
	private void selectTab(int idx) {
		selectTab(tabs.elementAt(idx));
	}
	
	private void selectTab(Tab t) {
		for (Tab tab : tabs)
			tab.setSelected(false);
		
		moveToBack(tabs.elementAt(selectedIdx));
		
		selectedIdx = tabs.indexOf(t);
		t.setSelected(true);
		moveToFront(t);
		
		repaint();
		
		itemManager.updatePane(t.item);
	}
	
	private void move(Tab tab, int shift) {
		if (shift==0)
			return;
		int idx = tabs.indexOf(tab) + shift;
		idx = Math.min(idx, tabs.size()-1);
		idx = Math.max(idx, 0);
		if (itemManager.moveItem(tab.item, idx)) {
			tabs.remove(tab);
			tabs.add(idx, tab);
			validate();
			
			selectTab(tab);
		}
	}

	private void removeSelectedTab() {
		int count = tabs.size();
		if (count==1)
			return;
		Tab tab = tabs.elementAt(selectedIdx);
		if (itemManager.removeItem(tab.item)) {
			remove(tab);
			tabs.remove(tab);
			
			if (selectedIdx == count-1) selectedIdx--; 
			selectTab(selectedIdx);
		}
	}
	
	private class Tab extends HeadElement {
		private static final long serialVersionUID = -8530479346248371104L;
		private final T item;
		private boolean isSelected = false;
		private int dragOffset = 0;
		
		public Tab(T item) {
			super(new BorderLayout());
			this.item = item;
			setOpaque(false);
			
			FWMouseListener manager = new FWMouseListener() {
				@Override
				public void mouseClicked(MouseEvent e, int mode) {
					TabHead.this.selectTab(tabs.indexOf(Tab.this));
				}
		
				@Override
				public void mouseDragged(int x, int y, int mode) {
					if (isSelected) {
						dragOffset = x;
						TabHead.this.doLayout();
					}
				}
		
				@Override
				public void mouseReleased(MouseEvent e) {
					super.mouseReleased(e);
					if (isSelected) {
						int idx = (int) ((dragOffset)/tabWidth);
						if (idx!=0)
							TabHead.this.move(Tab.this, idx);
						dragOffset = 0;
						TabHead.this.doLayout();
					}
				}
			};
			addMouseListener(manager);
			addMouseMotionListener(manager);
		}
		
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int width = getWidth();
			int height = getHeight();
			int radius = height/2;
			
			paintRoundBorder(g2, width, height, radius);
			
	        g2.setFont(font);
	
	        FontMetrics fm = getFontMetrics(font);
	        String text1 = SwingUtilities2.clipStringIfNecessary(this, fm, itemManager.getTitle(item), width-3*radius);
	        
	        int w = SwingUtilities2.stringWidth(this, fm, text1);
	        int h = fm.getHeight();
	        int x = (getWidth()-w)/2;
	        int y = (getHeight()-h)/2+fm.getMaxAscent();
	        
	        g2.setColor(Color.BLACK);
	        g2.drawString(text1, x, y);
			 if (isSelected) {
		        	super.paint(g);
		        }
		}
		
		private void setSelected(boolean b) {
			isSelected = b;
			if (isSelected) {
				add(removeButton, BorderLayout.EAST);
			} else {
				removeAll();
			}
			validate();
		}
		
		private void paintRoundBorder(Graphics2D g2, int width, int height, int r) {
			g2.setColor(UIManager.getColor("Panel.background"));
			g2.fillRect(0, 0, width, height);
			if (isSelected) {
				g2.setColor(UIManager.getColor("TabbedPane.selected"));
				Area rect = new Area(new Rectangle(0, r, width, height-r));
				Area circle = new Area(new Ellipse2D.Double(-r, height-2*r-1, 2*r, 2*r));
				g2.fill(rect);
				rect = new Area(new Rectangle(r, 0, width-2*r, r));
				circle = new Area(new Ellipse2D.Double(0, -1, 2*r, 2*r));
				rect.add(circle);
				circle = new Area(new Ellipse2D.Double(width-2*r, -1, 2*r, 2*r));
				rect.add(circle);
				g2.fill(rect);
				g2.setColor(Color.GRAY);
				g2.drawLine(1, r, 1, height);
				g2.drawLine(width-1, r, width-1, height);
				g2.drawLine(r, 0, width-r, 0);
				g2.drawArc(1, 0, 2*r, 2*r, 90, 90);
				g2.drawArc(width-2*r-1, 0, 2*r, 2*r, 0, 90);
			} else {
				g2.setColor(Color.LIGHT_GRAY);
				g2.drawLine(width-1, 2, width-1, height-2);
			}
		}
	
		public int getOffset() {
			return tabs.indexOf(this)*tabWidth + dragOffset;
		}
	}
	
	private class CustomButton extends HeadElement {
		
		private static final long serialVersionUID = -5535638175576030924L;

		public CustomButton(FWAction a) {
			super(new BorderLayout());
			JButton button = a.getButton();
			button.setPreferredSize(new Dimension(22, 22));
			button.setText(null);
			FWButton.removeBackground(button);
			button.setFocusable(false);
			add(button, BorderLayout.WEST);
		}
		
		@Override
		public int getOffset() {
			return tabs.size()*tabWidth+12;
		}
	}
	
	private abstract class HeadElement extends JPanel {
		
		private static final long serialVersionUID = -1255816659718258548L;

		public HeadElement(LayoutManager layout) {
			super(layout);
		}

		public abstract int getOffset();
	}
		
	private class HeadLayout extends BasicLayoutAdapter {
		
		private int offset = 0;
		private int tabIssueCount = 0;
		
		public void layoutContainer(Container parent) {
			init(parent);
			
			int idx = 0 ;
			for (CustomButton b : buttons) {
				layoutButton(b, idx);
				idx++ ;
			}

			
//			synchronized (parent.getTreeLock()) {
//				Component[] components = parent.getComponents();
//				for (int idx = 0; idx <= 4; idx++) 
//					layoutComponent(components[idx], idx);
//			}
			

			for (Tab tab : tabs) {
				tabIssueCount = 0;
				layoutTab(tab, idx);
				idx++;
			}
			
			action_nextTab.setEnabled(tabIssueCount > 0);
			action_previousTab.setEnabled(offset < 0);
		}
		
		public void layoutTab(Tab tab, int idx) {
			int x = tab.getOffset() + offset * tabWidth;
			if (x+tabWidth < parentW - 100) 
				tab.setBounds(x, 0, tabWidth, tabHeight);
			else { 
				tab.setBounds(parentW, 0, tabWidth, tabHeight);
				tabIssueCount ++;
			}
		}
		
		public void layoutButton(CustomButton b, int idx) {
			b.setBounds(parentW - 40 - idx*24, 0, tabWidth, tabHeight);
		}

		@Override
		public void layoutComponent(Component c, int idx) {}
		
		
	}
}
