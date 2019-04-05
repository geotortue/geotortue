package geotortue.geometry;

import java.awt.Container;

import javax.swing.JPanel;

import fw.app.FWConsole;
import fw.app.Translator.TKey;
import fw.geometry.GeometryI;
import fw.geometry.GeometryManager;
import fw.gui.FWComboBox;
import fw.gui.FWComboBox.FWComboBoxListener;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalFlowLayout;
import fw.renderer.MouseManager;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.gui.GTActionSettingsPuller;


public class GTGeometryManager extends GeometryManager<GTPoint> implements XMLCapabilities, FWSettings {

	private static final TKey NAME = new TKey(GTGeometryManager.class, "settings");

	@Override
	public TKey getTitle() {
		return NAME;
	}


	private final MouseManager mouseManager = new MouseManager();
	
	public GTGeometryManager()  {
		super(new GTGeometry[]{ 
			new GTEuclidean2DGeometry(),
			new GTEuclidean3DGeometry(),
			new GTSphericalGeometry(),
			new GTPoincareHPGeometry(),
			new GTPoincareDiscGeometry(),
			//new GTHyperboloidGeometry(),
			new GTFlatQuotientGeometry(), 
			new GTEuclidean4DGeometry()});
	}

	@Override
	public GTGeometry getGeometry() {
		return (GTGeometry) super.getGeometry();
	}
	
	private void setGeometry(String name) {
		GeometryI<GTPoint>[] geometries = getAvailableGeometries();
		for (int idx = 0; idx < geometries.length; idx++)
			if (((GTGeometry) geometries[idx]).getXMLTag().equals(name)) {
				setGeometry(idx);
				return;
			}
		FWConsole.printWarning(this, "The geometry named \""+name+"\" is not available.");
	}
	
	public MouseManager getMouseManager() {
		return mouseManager;
	}
	
	protected void setGeometry(int idx) {
		super.setGeometry(idx);
		getGeometry().update(mouseManager);
	}
	
	/*
	 * XML
	 */

	@Override
	public String getXMLTag() {
		return "GTGeometryManager";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.setAttribute("selected", getGeometry().getXMLTag());
		for (GeometryI<GTPoint> g : getAvailableGeometries())
			e.put((GTGeometry) g);
		e.put(getMouseManager());
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		getMouseManager().loadXMLProperties(child);
		for (GeometryI<GTPoint> g : getAvailableGeometries())
			((GTGeometry) g).loadXMLProperties(child);
		try {
			setGeometry(child.getAttribute("selected"));
		} catch (XMLException ex) {
			FWConsole.printWarning(this, ex.getMessage());
			FWConsole.printInfo(this, "Default geometry selected");
			setGeometry(0);
		}
		return child;
	}
	
	/*
	 * FWS
	 */
	
	
	@Override
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		final JPanel p = new JPanel(new VerticalFlowLayout(10));
		FWComboBox geoCB = new FWComboBox(getAvailableGeometries(), getGeometry(), new FWComboBoxListener() {
			public void itemSelected(Object o) {
				setGeometry(((GTGeometry) o).getXMLTag());
				appendGeometrySettingsPane(p, actions);
				actions.fire(GTActionSettingsPuller.UPDATE_GEOMETRY);
			}
		});
		p.add(geoCB);
		appendGeometrySettingsPane(p, actions);
		return p;
	}

	private void appendGeometrySettingsPane(JPanel p, FWSettingsActionPuller actions) {
		switch (p.getComponentCount()) {
		case 2 :
			p.remove(1);
		case 1:
			p.add(getGeometry().getSettingsPane(actions));
			Container owner = p.getTopLevelAncestor();
			if (owner != null)
				owner.validate();
			break;
		}
	}

	
}