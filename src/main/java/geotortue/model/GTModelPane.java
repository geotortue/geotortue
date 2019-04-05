/**
 * 
 */
package geotortue.model;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.Translator.TKey;
import fw.gui.FWButton;
import fw.gui.FWRoundToggleButton;
import fw.gui.FWTitledButtonedPane;
import fw.gui.layout.BasicLayoutAdapter;
import geotortue.core.GeoTortue.GTModelWelcomeAssistant;

public class GTModelPane extends JPanel {
	
	private static final TKey GRAPHICS = new TKey(GTModelPane.class, "graphics");
	private static final ActionKey REFRESH_MODEL = new ActionKey(GTModelPane.class, "refreshModel");
	private static final ActionKey PREVIOUS_MODEL = new ActionKey(GTModelPane.class, "prevModel");
	private static final ActionKey NEXT_MODEL = new ActionKey(GTModelPane.class, "nextModel");
	private static final ActionKey ONLINE_CATALOG = new ActionKey(GTModelPane.class, "onlineCatalog");
	private static final ActionKey LOCAL_CATALOG = new ActionKey(GTModelPane.class, "localCatalog");
	
	private final GTModelManager modelManager;
	private static final long serialVersionUID = -462499167082154919L;
	private GTModel model; 
	private NavigationPane navigationPane;
	private FWAction onlineCatalogAction, localCatalogAction ;
	
	public GTModelPane(GTModelManager m) {
		super();
		this.modelManager = m;
		init();
	}
	
	private FWAction action_refreshModel = new FWAction(REFRESH_MODEL, "view-refresh.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) { 
			modelManager.refreshGraphics(model);
		}
	});
	
	private FWAction action_prevModel = new FWAction(PREVIOUS_MODEL, "up.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			NavigationLayout layout = navigationPane.getLayout();
			layout.shiftDown();
			navigationPane.doLayout();
			action_prevModel.setEnabled(layout.canShiftDown());
			action_nextModel.setEnabled(layout.canShiftUp());
		}
	});
	
	private FWAction action_nextModel = new FWAction(NEXT_MODEL, "down.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			NavigationLayout layout = navigationPane.getLayout();
			layout.shiftUp();
			navigationPane.doLayout();
			action_prevModel.setEnabled(layout.canShiftDown());
			action_nextModel.setEnabled(layout.canShiftUp());
		}
	});

	public void init() {
		if (modelManager.getModels().isEmpty()) {
			if (onlineCatalogAction == null)
				return;
			setLayout(new WelcomeLayout());
			removeAll();
			addButton(onlineCatalogAction); 
			addButton(localCatalogAction);
			validate();
			return ;
		} else {
			setLayout(new ModelLayout());
			navigationPane = new NavigationPane();
			setModel(modelManager.getModels().firstElement());
			action_prevModel.setEnabled(false);
		}
	}
	
	private void addButton(FWAction action) {
		JButton b = action.getButton();
		b.setText("<html>"+b.getText()+"</html>");
		b.setContentAreaFilled(false);
		b.setRolloverEnabled(true);
		b.setFocusable(false);
		b.setVerticalTextPosition(SwingConstants.BOTTOM);
	    b.setHorizontalTextPosition(SwingConstants.CENTER);

		add(b);
	}
	
	public void setWelcomeAssistant(final GTModelWelcomeAssistant assistant) {
		onlineCatalogAction = new FWAction(ONLINE_CATALOG, "package_graphics_web_256.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				assistant.showOnlineCatalog();
			}
		});
		localCatalogAction = new FWAction(LOCAL_CATALOG, "package_graphics_local_256.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				assistant.showLocalCatalog();
			}
		});
		init();
	}

	
	
	
	private void setModel(GTModel m) {
		this.model = m;
		removeAll();
		
		JScrollPane graphics = new JScrollPane(modelManager.getGraphicSpace().getPane());
		add(new FWTitledButtonedPane(GRAPHICS, graphics, new JButton(action_refreshModel)));
		
		JScrollPane html = new JScrollPane(m.getHTMLPane());
		add(html);
			
		add(navigationPane);
			
		validate();
		modelManager.refreshGraphics(m);
	}
	
	
	/*
	 *	LAYOUT 
	 */
	
	private class ModelLayout extends BasicLayoutAdapter {
		protected int gap = 2;
		
		public void layoutContainer(Container parent) {
			init(parent);
			layoutModel(parent.getComponents());
		}
		
		private void layoutModel(Component[] components) {
			Component c = components[0]; // graphics
			Dimension d = GTModelPane.this.modelManager.getGraphicSpace().getSize();
			int graphicsW = Math.min(parentW, d.width+4);
			int graphicsH = Math.min(d.height+4+24, parentH-100);
			currX = (parentW - graphicsW)/2-gap;
			c.setBounds(currX, currY, graphicsW, graphicsH);
			
			c = components[1]; // htmlPane
			currY += graphicsH + gap;
			c.setBounds(currX, currY, graphicsW, parentH - gap -graphicsH);
			
			c = components[2]; // navPane
			currX += graphicsW + gap;
			currY = insets.top;
			c.setBounds(currX, currY, (parentW - graphicsW-gap)/2, parentH);
		}

		@Override
		public void layoutComponent(Component c, int idx) {}
	}

	private class WelcomeLayout extends BasicLayoutAdapter {
		public void layoutContainer(Container parent) {
			init(parent);
			Component[] components = parent.getComponents();
			Component c = components[0]; // button
			Dimension d = c.getPreferredSize();
			int iconW = d.width;
			int iconH = d.height;
			currX = (parentW - 5*iconW/2)/2;
			currY = (parentH - iconH)/2;
			c.setBounds(currX, currY, iconW, iconH);
			
			c = components[1]; // button
			currX += 3*iconW/2;
			c.setBounds(currX, currY, iconW, iconH);
		}

		@Override
		public void layoutComponent(Component c, int idx) {}
	}
	
	/*
	 * NAVIGATION
	 */
	private class NavigationPane extends JPanel {

		private static final long serialVersionUID = -9217546521139160963L;
		
		public NavigationPane() {
			super();
			Vector<GTModel> models = modelManager.getModels();
			int count = models.size();
			setLayout(new NavigationLayout(count));
			if (count>0) {
				addButton(action_prevModel);
				
				ButtonGroup group = new ButtonGroup();
				for (int idx = 0; idx < count; idx++) {
					JToggleButton b = getButton(idx, models.elementAt(idx));
					group.add(b);
					add(b);			
				}
				
				addButton(action_nextModel);
			}
		}
		
		@Override
		public NavigationLayout getLayout() {
			return (NavigationLayout) super.getLayout();
		}
		
		private void addButton(FWAction a) {
			JButton b = new JButton(a);
			FWButton.removeBackground(b);
			b.setText("");
			b.setFocusable(false);
			add(b);
		}
		
		private JToggleButton getButton(int idx, final GTModel m) {
			AbstractAction action = new AbstractAction((idx+1)+"") {
				private static final long serialVersionUID = -2451343858962873594L;

				@Override
				public void actionPerformed(ActionEvent e) {
					GTModelPane.this.setModel(m);
				}
			};

			FWRoundToggleButton b = new FWRoundToggleButton();
			b.setAction(action);
			b.getModel().setSelected(idx==0);
			return b;
		}
	}
	
	private class NavigationLayout extends BasicLayoutAdapter {
		private final int count;
		private int gap = 4;
		private int size = 42; 
		private int x = 0;
		private int shift = 0;
		private boolean shiftEnabled;
		private int maxNum = 0; 
		
		public NavigationLayout(int count) {
			this.count = count;
		}
		
		public boolean canShiftUp() {
			return shift < count-maxNum ;
		}
		
		public boolean canShiftDown() {
			return shift > 0 ;
		}
			
		public void shiftUp() {
			if (canShiftUp())
				shift ++;
		}

		public void shiftDown() {
			if (canShiftDown()) 
				shift --;
		}
		
		@Override
		protected void init(Container parent) {
			super.init(parent);
			x = Math.min((parentW - size)/2, 40);
			maxNum = (parentH)/(gap+size)-3;
			shiftEnabled = count>maxNum;
		}


		@Override
		public void layoutComponent(Component c, int idx) {
			if (idx==0) { // prevButton
				if (shiftEnabled) {
					currY += (size-22)/2;
					c.setBounds(x+(size-22)/2, currY, 22, 22);
					currY += gap+22;
				} else
					c.setBounds(parentW+10, 0, 22, 22); // don't show 
				return ;
			}
			
			currY += gap;
			
			if (idx == count+1) { // nextButton
				//currY -= (size-22)/2;
				if (shiftEnabled) 
					c.setBounds(x+(size-22)/2, currY, 22, 22); 
				else
					c.setBounds(parentW+10, 0, 22, 22); // don't show
				currY += (size-22);
				return;
			}
			
			if (shiftEnabled) 
				if (idx <= shift || idx > maxNum + shift) {
					c.setBounds(parentW+10, 0, size, size); // don't show
					currY -= gap;
					return;
				} 
			
			c.setBounds(x, currY, size, size); // toggleButtons
			currY += (gap+size);
		}
	}
}