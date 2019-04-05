package fw.renderer;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import fw.app.FWToolKit;
import fw.app.Translator.TKey;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.gui.FWLabel;
import fw.gui.FWMouseListener;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWBoolean;
import fw.renderer.core.RendererI;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class MouseManager implements XMLCapabilities, FWSettings {
	

	private static final TKey NAME = new TKey(MouseManager.class, "settings");

	@Override
	public TKey getTitle() {
		return NAME;
	}
	


	
	private final static TKey DRAG = new TKey(MouseManager.class, "dragEnabled");
	private final static TKey ZOOM = new TKey(MouseManager.class, "zoomEnabled");
	private final static TKey ROTATION_X = new TKey(MouseManager.class, "rotationXEnabled");
	private final static TKey ROTATION_Y= new TKey(MouseManager.class, "rotationYEnabled");
	private final static TKey ROTATION_Z= new TKey(MouseManager.class, "rotationZEnabled");

	private final Vector<Ability> abilities = new Vector<Ability>();
	
	public final Ability translationAbility = new Ability(DRAG, "dragEnabled"), 
			zoomAbility = new Ability(ZOOM, "zoomEnabled"), 
			xRotationAbility = new Ability(ROTATION_X, "rotationXEnabled"), 
			yRotationAbility = new Ability(ROTATION_Y, "rotationYEnabled"), 
			zRotationAbility = new Ability(ROTATION_Z, "rotationZEnabled"); 

	public MouseManager() {
		FWToolKit.createCursor("move.gif", 7, 7);
		FWToolKit.createCursor("turnX.gif", 7, 7);
		FWToolKit.createCursor("turnY.gif", 7, 7);
		FWToolKit.createCursor("turnXY.gif", 7, 7);
		FWToolKit.createCursor("turnZ.gif", 7, 7);
	}
	
	public void setAllAbilitiesAvailable(boolean b){
		for (Ability ability : abilities)
			ability.setAvailable(b);
	}
	
	public final class Ability {
		private boolean available = true;
		public boolean isAvailable() {
			return available;
		}

		private final FWBoolean selected;
		private final TKey key;
		
		private Ability(TKey key, String tag) {
			this.key = key;
			this.selected = new FWBoolean(tag, true);
			abilities.add(this);
		}
		
		public void setAvailable(boolean b) {
			this.available = b;
			selected.setEnabled(b);
		}
		
		public boolean isEnabled() {
			return available && selected.getValue();
		}
		
		
	}
	
	
	private HashSet<RendererI<?>> registeredRenderers = new HashSet<RendererI<?>>();
	
	public final void addMouseListener(final RendererI<?> renderer) {
		if (registeredRenderers.contains(renderer))
			return;

		FWMouseListener listener = getMouseListener(renderer);
		JPanel pane = renderer.getPane();
		pane.addMouseListener(listener);
		pane.addMouseMotionListener(listener);
		pane.addMouseWheelListener(listener);
		
		registeredRenderers.add(renderer);
	}

	public FWMouseListener getMouseListener(final RendererI<?> renderer) {
		FWMouseListener listener = new FWMouseListener() {
			private Point3D origin;
			private QRotation spaceTransform;
			private double dragFactor = 1/200.;
			private double zoomFactor = 1.125;
			
			@Override
			public void mousePressed(MouseEvent e){
				super.mousePressed(e);
				origin = renderer.getOrigin();
				spaceTransform = renderer.getSpaceTransform();

				String cursorName = null;
				int mode = getMouseModifiers();
				if (mode == RIGHT && translationAbility.isEnabled()) {
					cursorName = "move.gif";
				} else if (mode == RIGHT_SHIFT && zRotationAbility.isEnabled()) {
					cursorName = "turnZ.gif";
				} else if (mode == RIGHT_CTRL && (xRotationAbility.isEnabled() || yRotationAbility.isEnabled())) {
					if (!yRotationAbility.isEnabled())
						cursorName = "turnX.gif";
					else if (!xRotationAbility.isEnabled())
						cursorName = "turnY.gif";
					else 
						cursorName = "turnXY.gif";
				} 
				if (cursorName!=null)
					renderer.getPane().setCursor(FWToolKit.getCursor(cursorName));
			}

			@Override
			public void mouseDragged(int x, int y, int mode) {
				if (mode==RIGHT && translationAbility.isEnabled()) {
					renderer.setOrigin(origin.getTranslated(x, -y, 0));
				} else  {
					QRotation r = new QRotation();
					if (mode == RIGHT_SHIFT && zRotationAbility.isEnabled()) {
						r = QRotation.getZRotation(x * dragFactor).apply(r);
					} else if (mode == RIGHT_CTRL && (xRotationAbility.isEnabled() || yRotationAbility.isEnabled())) {
						if (xRotationAbility.isEnabled())
							r = QRotation.getXRotation(y * dragFactor).apply(r);
						if (yRotationAbility.isEnabled())
							r = QRotation.getYRotation(x * dragFactor).apply(r);
					} else
						return;
					
					renderer.setSpaceTransform(r.apply(spaceTransform));
					renderer.setOrigin(r.apply(origin));
				}
				renderer.getPane().repaint();
			}

			public void mouseReleased(MouseEvent e, int mode) {
				renderer.getPane().setCursor(Cursor.getDefaultCursor());
			}

			public void mouseWheelMoved(MouseWheelEvent e) {
				if (zoomAbility.isEnabled()){
					if (e.getWheelRotation()<0)
						renderer.zoom(zoomFactor, e.getPoint());
					else
						renderer.zoom(1/zoomFactor, e.getPoint());
					renderer.getPane().repaint();
				}
			}
		};
		return listener;
	}
	
	/*
	 * FWS
	 */
	
	public JPanel getSettingsPane(FWSettingsActionPuller actions) {
		JPanel p = new JPanel(new VerticalPairingLayout(10, 10));
		for (Ability ability : abilities) {
			p.add(new FWLabel(ability.key, SwingConstants.RIGHT));
			p.add(ability.selected.getComponent());
		}
		return p;
	}
	
	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return "MouseManager";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		for (Ability ability : abilities)
			ability.selected.storeValue(e);
		return e;
	}
	
	
	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		for (Ability ability : abilities)
			ability.selected.fetchValue(child, true);
		return child;
	}
}