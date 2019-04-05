/**
 * 
 */
package geotortue.sandbox;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JPanel;

import fw.gui.layout.VerticalFlowLayout;
import fw.gui.layout.VerticalPairingLayout;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.core.GTDocumentFactory;

/*
 * GTSandBoxButtonPane 
 */
class GTSandBoxButtonPane extends JPanel implements XMLCapabilities {
	private static final long serialVersionUID = 2385763665903600980L;

	private final GTDocumentFactory docFactory;
	enum TYPE {LEFT, RIGHT};

	GTSandBoxButtonPane(GTDocumentFactory df, TYPE t) {
		this.docFactory = df;
		setLayout(t);
	}
	
	public Component add(GTSandBoxButton c){
		return super.add(c);
	}
	
	public Component add(Component c){
		new Exception("add only GTSandBoxButton !").printStackTrace();
		return null;
	}

	private void setLayout(TYPE t) {
		switch (t) {
		case LEFT:
			setLayout(new VerticalFlowLayout(0, 20) {
				@Override
				protected int getWidth(Container parent) {
					return getMaxComponentWidth(parent);
				}
			});
			break;
		case RIGHT:
			setLayout(new VerticalPairingLayout(false, 7, 20) {

				@Override
				protected int getLeftOffset(int hgap) {
					return hgap;
				}
				
			});
			break;
		}
	}

	@Override
	public String getXMLTag() {
		return "GTSandBoxButtonPane";
	}

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		for (Component c : getComponents())
			e.put((GTSandBoxButton) c);
		return e;
	}

	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		removeAll();
		while (child.hasChild(GTSandBoxButton.XML_TAG))
			add(new GTSandBoxButton(docFactory, child));
		invalidate();
		repaint();
		return child;
	}

	/**
	 * @return
	 */
	GTSandBoxButton newButton() {
		GTSandBoxButton b = new GTSandBoxButton(docFactory);
		add(b);
		doLayout();
		return b;
	}
}