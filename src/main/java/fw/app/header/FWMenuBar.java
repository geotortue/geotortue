package fw.app.header;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import fw.app.FWAction;
import fw.app.FWActionManager;
import fw.app.FWActionManager.NoSuchActionFound;
import fw.app.FWConsole;
import fw.app.Translator.TKey;
import fw.gui.FWAccessibilityManager;
import fw.gui.FWAccessible;
import fw.text.TextStyle;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;


/**
 *
 */
public class FWMenuBar extends JMenuBar implements XMLCapabilities, FWAccessible {

	private static final long serialVersionUID = 1998299812375304345L;
	private FWActionManager manager;
	private FWMenuTitles titles;
	
	public FWMenuBar(XMLReader e, FWActionManager m, FWMenuTitles titles) {
		this.manager = m;
		this.titles = titles;
		loadXMLProperties(e);
		FWAccessibilityManager.register(this);
	}
	
	public String getXMLTag() {
		return "FWMenuBar";
	}
	
	public FWMenuBar(TKey title, FWAction... actions){
		JMenu menu = new JMenu(title.translate());
		for (FWAction a : actions)
			menu.add(new FWMenuItem(a));
		add(menu);
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		for (int idx = 0; idx < getMenuCount(); idx++) {
			e.put((FWMenu) getMenu(idx));
		}
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		while (child.hasChild(FW_MENU_XML_TAG)){
			add(new FWMenu(child));
		}
		return child;
	}
	
	@Override
	public void setFont(TextStyle s) {
		setFont(s.getFont());
	}

	
	public static class FWMenuTitles {
		private final Hashtable<String, MKey> table = new Hashtable<>();
		
		public void add(MKey key) {
			table.put(key.key, key);
		}

		public MKey get(String title) {
			return table.get(title);
		}
	}
	
	public static class MKey extends TKey {
		private final TKey mnemonic;
		private final String key;
		public MKey(Class<?> c, String key) {
			super(c, key+".title");
			this.key = key;
			this.mnemonic = new TKey(c, key+".mnemonic");
		}
	}
	
	private static final XMLTagged FW_MENU_XML_TAG = XMLTagged.Factory.create("FWMenu");
	
	private class FWMenu extends JMenu implements XMLCapabilities, FWAccessible {
		
		private static final long serialVersionUID = 8613628533203490980L;

		private FWMenu(XMLReader e) {
			loadXMLProperties(e);
			FWAccessibilityManager.register(this);
		}
		
		public String getXMLTag() {
			return FW_MENU_XML_TAG.getXMLTag();
		}
		
		@Override
		public XMLWriter getXMLProperties() {
			XMLWriter e = new XMLWriter(this);
			e.setAttribute("title", getActionCommand());
			for(Component c : getMenuComponents()){
				if (c instanceof FWMenu)
					e.put((FWMenu) c);
				if (c instanceof FWMenuItem)
					e.put((FWMenuItem) c);
				if (c instanceof FWMenuSeparator)
					e.put((FWMenuSeparator) c);
			}	
			return e;
		}
		
		@Override
		public XMLReader loadXMLProperties(XMLReader e) {
			XMLReader child = e.popChild(this);
			String text = "";
			try {
				text = child.getAttribute("title");
			} catch (XMLException ex) {
				ex.printStackTrace();
				return child;
			}
			setText(titles.get(text).translate()); 
			String mnemo = titles.get(text).mnemonic.translate();
			if (mnemo.length()>0)
				setMnemonic(mnemo.charAt(0));
			JComponent c = getNextItem(child, manager); 
			while (c!=null){
				add(c);
				c = getNextItem(child, manager);
			}
			return child;
		}
		
		private JComponent getNextItem(XMLReader e, FWActionManager manager) {
			String tag = e.getNextChildTag();
			if (tag == null)
				return null;
			if (tag.equals("FWMenu"))
				return new FWMenu(e);
			if (tag.equals("FWMenuItem"))
				return new FWMenuItem(e);
			if (tag.equals("FWMenuSeparator"))
				return new FWMenuSeparator(e);
			return null;
		}
		
		@Override
		public void setFont(TextStyle s) {
			setFont(s.deriveFont(TextStyle.PLAIN, 2));
		}

	}

	/**
	 * 
	 *
	 */
	private class FWMenuItem extends JMenuItem implements XMLCapabilities, FWAccessible {
		private static final long serialVersionUID = 1835814003493812298L;

		private FWMenuItem(XMLReader e) {
			loadXMLProperties(e);
			FWAccessibilityManager.register(this);
		}
		
		private FWMenuItem(FWAction a){
			setAction(a);
			FWAccessibilityManager.register(this);
		}
		
		public String getXMLTag() {
			return "FWMenuItem";
		}
		
		@Override
		public XMLWriter getXMLProperties() {
			XMLWriter e = new XMLWriter(this);
			e.setAttribute("key", getActionCommand());
			return e;
		}
		
		@Override
		public XMLReader loadXMLProperties(XMLReader e) {
			XMLReader child = e.popChild(this);
			boolean optional = false;
			try {
				String key = child.getAttribute("key");
				optional = key.startsWith("*");
				setAction(manager.get(key));
			} catch (XMLException ex) {
				ex.printStackTrace();
			} catch (NoSuchActionFound ex) {
				if (!optional)
					FWConsole.printWarning(this, "NoSuchAction : \"" + ex.getMessage()+"\"");
				setPreferredSize(new Dimension());
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
	private class FWMenuSeparator extends JSeparator implements XMLCapabilities {
		private static final long serialVersionUID = -3749567019633187983L;

		
		private FWMenuSeparator(XMLReader e) {
			loadXMLProperties(e);
		}
		
		public String getXMLTag() {
			return "FWMenuSeparator";
		}
		
		@Override
		public XMLWriter getXMLProperties() {
			return new XMLWriter(this);
		}
		
		@Override
		public XMLReader loadXMLProperties(XMLReader e) {
			return e.popChild(this);
		}

	}
}