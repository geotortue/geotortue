package geotortue.geometry.proj;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import fw.app.FWConsole;
import fw.app.Translator.TKey;
import fw.geometry.proj.PerspectiveI;
import fw.geometry.proj.PerspectiveI.InvisibleZPointException;
import fw.geometry.proj.PerspectiveManager;
import fw.geometry.proj.ZPoint;
import fw.geometry.util.MathException;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;
import fw.gui.FWComboBox;
import fw.gui.FWComboBox.FWComboBoxListener;
import fw.gui.FWSettings;
import fw.gui.FWSettingsAction;
import fw.gui.FWSettingsActionPuller;
import fw.gui.FWSettingsActionPuller.FWSettingsActionKey;
import fw.gui.layout.VerticalFlowLayout;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.gui.GTActionSettingsPuller;


public class GTPerspectiveManager extends PerspectiveManager implements XMLCapabilities, FWSettings {


	private static final TKey NAME = new TKey(GTPerspectiveManager.class, "settings");

	@Override
	public TKey getTitle() {
		return NAME;
	}


	
	private final PerspectiveSelectionPane perspectiveSelectionPane = new PerspectiveSelectionPane();
	
	public GTPerspectiveManager() {
		super(new GTOrthogonalPerspective(),
			new GTCavalierPerspective(),
			new GTIsometricPerspective(),
			new GTDimetricPerspective(),
			new GTAxonometricPerspective(),
			new GTConicPerspective());
	}
	
	@Override
	public GTPerspective getPerspective() {
		return (GTPerspective) super.getPerspective();
	}

	private final void setPerspective(String name) {
		PerspectiveI[] perspectives = getAvailablePerspectives();
		for (int idx = 0; idx < perspectives.length; idx++)
			if (((GTPerspective) perspectives[idx]).getXMLTag().equals(name)) {
				setPerspective(idx);
				return;
			}
		FWConsole.printWarning(this, "The perspective named \""+name+"\" is not available.");
	}

	/*
	 * XML
	 */

	@Override
	public String getXMLTag() {
		return "GTPerspectiveManager";
	}

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.setAttribute("selected", getPerspective().getXMLTag());
		for (PerspectiveI p  : getAvailablePerspectives())
			e.put((GTPerspective) p);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		try {
			setPerspective(child.getAttribute("selected"));
		} catch (XMLException ex) {
			FWConsole.printWarning(this, ex.getMessage());
			FWConsole.printInfo(this, "Default perspective selected");
			setPerspective(0);
		}
		for (PerspectiveI p : getAvailablePerspectives())
			((GTPerspective) p).loadXMLProperties(child);
		return child;
	}

	/*
	 * FWS
	 */
	
	protected final static FWSettingsActionKey UPDATE_PERSPECTIVE = new FWSettingsActionKey();
	@Override
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		actions.register(UPDATE_PERSPECTIVE, new FWSettingsAction() {
			@Override
			public void fire() {
				actions.fire(GTActionSettingsPuller.UPDATE_PERSPECTIVE);
				perspectiveSelectionPane.repaint();
			}
		});
		
		final JPanel p = new JPanel(new VerticalFlowLayout(10));
		FWComboBox geoCB = new FWComboBox(getAvailablePerspectives(), getPerspective(), new FWComboBoxListener() {
			public void itemSelected(Object o) {
				setPerspective(((GTPerspective) o).getXMLTag());
				appendPerspectiveSettingsPane(p, actions);
				actions.fire(UPDATE_PERSPECTIVE);
			}
		});
		
		p.add(geoCB);
		JPanel p1 = new JPanel();
		p1.add(perspectiveSelectionPane);
		p.add(p1);
		appendPerspectiveSettingsPane(p, actions);
		return p;
	}
	
	private void appendPerspectiveSettingsPane(JPanel p, final FWSettingsActionPuller actions) {
		switch (p.getComponentCount()) {
		case 3 :
			p.remove(2);
		case 2:
			p.add(getPerspective().getSettingsPane(actions));
			Container owner = p.getTopLevelAncestor();
			if (owner != null)
				owner.validate();
			break;
		}
	}
	

	private class PerspectiveSelectionPane extends JPanel {
		private static final long serialVersionUID = 7634345674409812648L;
		
		private final int d = 30;
		
		private PerspectiveSelectionPane() {
			super();
			setPreferredSize(new Dimension(5*d, 5*d));
			setBackground(Color.WHITE);
			setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		}
		
		private Pixel getPixel(double x, double y, double z) {
			PerspectiveI p = getPerspective();
			try {
				return toScreen(p.toZSpace(new Point3D(x, y, z)));
			} catch (InvisibleZPointException ex) {
				return new Pixel(0, 0);
			}
		}
		
		public Pixel toScreen(ZPoint p) {
			Dimension screenSize = getSize();
			return getClosestPixel(screenSize.width/2. + p.x, screenSize.height/2. - p.y);
		}
		
		private Pixel getClosestPixel(double x, double y) {
			try {
				return new Pixel(MathUtils.round(x), MathUtils.round(y));
			} catch (MathException e) {
				return new Pixel(0, 0);
			}
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Pixel p000 = getPixel(0, 0, 0);
			Pixel px = getPixel(2*d, 0, 0);
			Pixel py = getPixel(0, 2*d, 0);
			Pixel pz = getPixel(0, 0, 2*d);
			
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			g.setColor(Color.BLUE);
			g.drawLine(p000.i, p000.j, px.i, px.j);
			g.drawString("X", px.i+5, px.j+5);

			g.setColor(Color.RED);
			g.drawLine(p000.i, p000.j, py.i, py.j);
			g.drawString("Y", py.i+5, py.j+5);

			g.setColor(Color.GREEN);
			g.drawLine(p000.i, p000.j, pz.i, pz.j);
			g.drawString("Z", pz.i+5, pz.j+5);
			
			
			Pixel p100 = getPixel(d, 0, 0);
			Pixel p010 = getPixel(0, d, 0);
			Pixel p110 = getPixel(d, d, 0);
			Pixel p001 = getPixel(0, 0, -d);
			Pixel p101 = getPixel(d, 0, -d);
			Pixel p011 = getPixel(0, d, -d);
			Pixel p111 = getPixel(d, d, -d);
			
			g.setColor(Color.GRAY);
			g.drawLine(p000.i, p000.j, p100.i, p100.j);
			g.drawLine(p100.i, p100.j, p110.i, p110.j);
			g.drawLine(p110.i, p110.j, p010.i, p010.j);
			g.drawLine(p010.i, p010.j, p000.i, p000.j);
			
			g.drawLine(p001.i, p001.j, p101.i, p101.j);
			g.drawLine(p101.i, p101.j, p111.i, p111.j);
			g.drawLine(p111.i, p111.j, p011.i, p011.j);
			g.drawLine(p011.i, p011.j, p001.i, p001.j);
			
			g.drawLine(p000.i, p000.j, p001.i, p001.j);
			g.drawLine(p100.i, p100.j, p101.i, p101.j);
			g.drawLine(p110.i, p110.j, p111.i, p111.j);
			g.drawLine(p010.i, p010.j, p011.i, p011.j);
			
		}
	}


}