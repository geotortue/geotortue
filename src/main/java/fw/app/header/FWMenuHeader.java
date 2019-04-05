package fw.app.header;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JPanel;

import fw.app.FWActionManager;
import fw.app.header.FWMenuBar.FWMenuTitles;
import fw.gui.layout.BasicLayoutAdapter;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


/**
 * 
 * 
 *
 */

public class FWMenuHeader extends JPanel implements XMLCapabilities {

	private static final long serialVersionUID = -4884849018737713199L;
	
	protected final FWActionManager manager;
	private FWMenuTitles titles;
	private FWMenuBar menuBar;
	private FWToolBar toolBar;

	/*
	 * XML
	 */
	
	public FWMenuHeader(XMLReader e, FWActionManager m, FWMenuTitles titles) throws XMLException{
		this.manager = m;
		this.titles = titles;
		loadXMLProperties(e);
		init();
	}
	
	public String getXMLTag() {
		return "FWMenuHeader";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.put(menuBar);
		e.put(toolBar);
		return e;
	}
	
	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		removeAll();
		this.menuBar = new FWMenuBar(child, manager, titles);
		this.toolBar = new FWToolBar(child, manager);
		return child;
	}
	
	protected void init(){
		add(menuBar);
		add(toolBar);
		setBorder(null);
		setLayout(new Layout(24, 30));
	}
	
	private class Layout extends BasicLayoutAdapter {

		private int menuBarHeight;
		private int toolBarHeight;
		
		public Layout(int menuBarH, int toolBarH){
			this.menuBarHeight = menuBarH;
			this.toolBarHeight = toolBarH;
		}
		
		@Override
		public void layoutComponent(Component c, int idx) {
			if (idx==0) // menuBar
				c.setBounds(0, 0, parentW, menuBarHeight);
			if (idx==1)// toolBar
				c.setBounds(0, 1+menuBarHeight, parentW, toolBarHeight);
		}
		
		public Dimension preferredLayoutSize(Container parent) {
			return new Dimension(parent.getWidth(), menuBarHeight+toolBarHeight+2);
		}
	}
}