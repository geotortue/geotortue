package geotortue.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import fw.app.FWActionManager;
import fw.app.header.FWMenuBar.FWMenuTitles;
import fw.app.header.FWMenuHeader;
import fw.app.header.FWToolBar;
import fw.gui.layout.BasicLayoutAdapter;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class GTMenuHeader extends FWMenuHeader implements XMLCapabilities {
	private static final long serialVersionUID = 1293231435237311798L;
	
	private FWToolBar toolBar2;

	public GTMenuHeader(XMLReader e, FWActionManager m, FWMenuTitles titles) throws XMLException {
		super(e, m, titles);
	}
	
	public String getXMLTag() {
		return "GTMenuHeader";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = super.getXMLProperties();
		e.put(toolBar2);
		return e;
	}
	
	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = super.loadXMLProperties(e);
		removeAll();
		this.toolBar2 = new FWToolBar(child, manager);
		return child;
	}
	
	protected void init() {
		super.init();
		add(toolBar2);
		setLayout(new Layout(24, 30));
	}
	
	private class Layout extends BasicLayoutAdapter {
		private int menuBarHeight, toolBarHeight, toolBar2Width;
		
		public Layout(int menuBarH, int toolBarH){
			this.menuBarHeight = menuBarH;
			this.toolBarHeight = toolBarH;
		}
		
		@Override
		protected void init(Container parent) {
			super.init(parent);
			toolBar2Width = toolBar2.getMinimumSize().width;
		}

		@Override
		public void layoutComponent(Component c, int idx) {
			if (idx==0) // menuBar
				c.setBounds(0, 0, parentW, menuBarHeight);
			if (idx==1)// toolBar
				c.setBounds(0, 1+menuBarHeight, parentW - toolBar2Width, toolBarHeight);
			if (idx==2)// toolBar2
				c.setBounds(parentW - toolBar2Width , 1+menuBarHeight, toolBar2Width, toolBarHeight);
		}
		
		public Dimension preferredLayoutSize(Container parent) {
			return new Dimension(parent.getWidth(), menuBarHeight+toolBarHeight+2);
		}
	}
}