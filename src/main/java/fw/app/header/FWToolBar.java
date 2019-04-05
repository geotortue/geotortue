package fw.app.header;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.Hashtable;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fw.app.FWActionManager;
import fw.app.FWActionManager.NoSuchActionFound;
import fw.app.FWToolKit;
import fw.gui.FWAccessibilityManager;
import fw.gui.FWAccessible;
import fw.gui.FWButton;
import fw.text.TextStyle;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class FWToolBar extends JToolBar implements XMLCapabilities {

	private static final long serialVersionUID = 2674769353888169861L;
	private FWActionManager manager;
	private Hashtable<String, ButtonGroup> buttonGroups = new Hashtable<String, ButtonGroup>();
	
	public FWToolBar(XMLReader e, FWActionManager m) {
		this.manager = m;
		setBackground(UIManager.getColor("FWToolBar.background"));
		setFloatable(false);
		loadXMLProperties(e);
	}
	
	public String getXMLTag() {
		return "FWToolBar";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		
		for (Component c : getComponents()) {
			if (c instanceof FWToolBarButton)
				e.put((FWToolBarButton) c);
			if (c instanceof FWToolBarSeparator)
				e.put((FWToolBarSeparator) c);
		}
		return e;
	}
	
	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		JComponent c = getNextItem(child); 
		while (c!=null){
			add(c);
			c = getNextItem(child);
		}
		return child;
	}
	
	private JComponent getNextItem(XMLReader e) {
		String tag = e.getNextChildTag();
		if (tag == null)
			return null;
		if (tag.equals("FWToolBarButton"))
			return new FWToolBarButton(e);
		if (tag.equals("FWToolBarToggleButton"))
			return new FWToolBarToggleButton(e);
		if (tag.equals("FWToolBarSeparator"))
			return new FWToolBarSeparator(e);
		return null;
	}
	
	/**
	 *
	 */
	private class FWToolBarButton extends JButton implements XMLCapabilities, FWAccessible {
		private static final long serialVersionUID = 7696295077137061013L;

		private FWToolBarButton(XMLReader e) {
			loadXMLProperties(e);
			setFocusable(false);
			setMargin(new Insets(0, 0, 0, 0));
			FWButton.removeBackground(this);
			setMnemonic(0);
			FWAccessibilityManager.register(this);
		}

		public String getXMLTag() {
			return "FWToolBarButton";
		}

		@Override
		public XMLWriter getXMLProperties() {
			XMLWriter e = new XMLWriter(this);
			e.setAttribute("key", getActionCommand());
			return e;
		}

		public XMLReader loadXMLProperties(XMLReader e) {
			XMLReader child = e.popChild(this);
			try {
				String key = child.getAttribute("key");
				setAction(manager.get(key));
			} catch (XMLException | NoSuchActionFound ex) {
				ex.printStackTrace();
			}
			
			try {
				if (child.getAttributeAsBoolean("title"))
					setText((String) getAction().getValue(Action.NAME)+" ");
				else
					setText("");
			} catch (XMLException ex) {
				setText("");
			}
			return child;
		}
		
		@Override
		public void setFont(TextStyle s) {
		}
	}
	
	/**
	 *
	 */
	private class FWToolBarToggleButton extends JToggleButton implements XMLCapabilities, FWAccessible {
		private static final long serialVersionUID = -5655747978723283186L;

		private FWToolBarToggleButton(XMLReader e) {
			loadXMLProperties(e);
			setFocusable(false);
			setMargin(new Insets(0, 0, 0, 0));
			setBorderPainted(false);
			getModel().addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					ButtonModel model = (ButtonModel) e.getSource();
					boolean ir = model.isRollover() || model.isSelected();
					setBorderPainted(ir);
				}
		    });
			FWAccessibilityManager.register(this);
		}

		public String getXMLTag() {
			return "FWToolBarToggleButton";
		}

		@Override
		public XMLWriter getXMLProperties() {
			XMLWriter e = new XMLWriter(this);
			e.setAttribute("key", getActionCommand());
			return e;
		}

		public XMLReader loadXMLProperties(XMLReader e) {
			XMLReader child = e.popChild(this);
			try {
				String key = child.getAttribute("key");
				setAction(manager.get(key));
				String groupKey = child.getAttribute("group");
				ButtonGroup group = buttonGroups.get(groupKey);
				if (group == null) {
					group = new ButtonGroup();
					buttonGroups.put(groupKey, group);
				}
				group.add(this);
			} catch (XMLException | NoSuchActionFound ex) {
				ex.printStackTrace();
			}
			
			try {
				if (child.getAttributeAsBoolean("title"))
					setText((String) getAction().getValue(Action.NAME)+" ");
				else
					setText("");
			} catch (XMLException ex) {
				setText("");
			}
			
			try {
					String iconFile = child.getAttribute("selectedIcon");
					Icon icon = FWToolKit.getIcon(iconFile);
					if (icon!=null) {
						setSelectedIcon(icon);
						setRolloverIcon(icon);
					}
			} catch (XMLException ex) {
			}
			
			return child;
		}
		
		@Override
		public void setFont(TextStyle s) {
			setFont(s.getFont());
		}
	}
	
	/**
	 * 
	 *
	 */
	private class FWToolBarSeparator extends JToolBar.Separator implements XMLCapabilities {
		private static final long serialVersionUID = 873096477222782303L;

		private FWToolBarSeparator(XMLReader e) {
			loadXMLProperties(e);
		}
		
		public String getXMLTag() {
			return "FWToolBarSeparator";
		}
		
		@Override
		public XMLWriter getXMLProperties() {
			return new XMLWriter(this);
		}

		public XMLReader loadXMLProperties(XMLReader e) {
			XMLReader child = e.popChild(this);
			int size = child.getAttributeAsInteger("size", 10);
			setSeparatorSize(new Dimension(size, size));
			return child;
		}
	}
}