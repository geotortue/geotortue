package geotortue.renderer;

import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import fw.app.FWAction.ActionKey;
import fw.app.Translator.TKey;
import fw.geometry.util.MathException.ZeroVectorException;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.gui.FWLabel;
import fw.gui.FWModularList;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalFlowLayout;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWBoolean;
import fw.gui.params.FWDouble;
import fw.gui.params.FWParameterListener;
import fw.renderer.light.DirectionalLight;
import fw.renderer.light.Light;
import fw.renderer.light.LightingContext;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;


public class GTLightingContext extends LightingContext implements FWSettings, XMLCapabilities {

	private static final TKey NAME = new TKey(GTLightingContext.class, "settings");

	@Override
	public TKey getTitle() {
		return NAME;
	}

	private static final TKey LIGHT_SETTINGS= new TKey(GTLight.class, "lightSettings");
	private static final TKey LIGHTS = new TKey(GTLightingContext.class, "lights");
	private static final TKey AMBIENT_LIGHT = new TKey(GTLightingContext.class, "ambientLight");
	private final static ActionKey ADD_LIGHT = new ActionKey(GTLightingContext.class, "addLight");
	private final static ActionKey REMOVE_LIGHT = new ActionKey(GTLightingContext.class, "removeLight");
	private final static TKey PHI = new TKey(GTLightingContext.class,  "phi");
	private final static TKey THETA = new TKey(GTLightingContext.class, "theta");
	private final static TKey IS_FIXED = new TKey(GTLightingContext.class, "isFixed");

	
	
	private final ArrayList<Light> lights = new ArrayList<Light>();
	private FWDouble ambient = new FWDouble("ambientLight", 0.4, 0, 1, 0.05);

	public GTLightingContext() {
		lights.add(new GTLight(34, -29));
	}
	
	@Override
	public double getAmbientComponent() {
		return ambient.getValue();
	}

	@Override
	public ArrayList<Light> getLights() {
		return lights;
	}

	public void reset() {
		for (Light light : lights) 
			((GTLight) light).reset();
	}

	public void setOrientation(QRotation r) {
		for (Light light : lights) 
			((GTLight) light).setOrientation(r);
	}
	
	@Override
	public String getXMLTag() {
		return "GTLightingContext";
	}

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		ambient.storeValue(e);
		for (Light l : lights)
			e.put((GTLight) l);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		ambient.fetchValue(child, 0.4);
		lights.clear();
		while (child.hasChild(GTLight.XML_TAG))
			lights.add(new GTLight(child));
		if (child.hasError() && lights.isEmpty())
			lights.add(new GTLight(34, -29));
		return child;
	}

	@Override
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		final FWModularList<GTLight> list = new FWModularList<GTLight>(ADD_LIGHT, REMOVE_LIGHT){
			private static final long serialVersionUID = -6180071507484707265L;

			@Override
			protected GTLight getDefaultItem() {
				GTLight light = new GTLight(0, 0);
				lights.add(light);
				actions.fire(FWSettingsActionPuller.REPAINT);
				return light;
			}

			@Override
			protected boolean removeItem(GTLight light, Window owner) {
				lights.remove(light);
				actions.fire(FWSettingsActionPuller.REPAINT);
				return true;
			}

			@Override
			protected JComponent[] getItemComponents(final GTLight light) {
				
				return new JComponent[]{light.getSettingsPane(actions)};
			}
		};
		
		list.addItem(lights.toArray(new GTLight[lights.size()]));
		
		JSpinner ambientSpinner = ambient.getComponent(new FWParameterListener<Double>() {
			@Override
			public void settingsChanged(Double value) {
				actions.fire(FWSettingsActionPuller.REPAINT);
			}
		});
		
		return VerticalFlowLayout.createPanel(
				VerticalPairingLayout.createPanel(
					new FWLabel(AMBIENT_LIGHT, SwingConstants.RIGHT), ambientSpinner),
				new FWLabel(LIGHTS, SwingConstants.CENTER), 
				list);
	}


	private static class GTLight extends DirectionalLight implements FWSettings, XMLCapabilities {

		private static final XMLTagged XML_TAG = XMLTagged.Factory.create("GTLight");
		@Override
		public TKey getTitle() {
			return LIGHT_SETTINGS;
		}


		private Point3D absoluteDirection;
		private FWDouble phi = new FWDouble("phi", 0, -360, 360, 1); 
		private FWDouble theta = new FWDouble("theta", 0, -360, 360, 1);
		private FWBoolean fixed = new FWBoolean("fixed", true);
	
		private GTLight(double lx, double ly) {
			phi = new FWDouble("phi", lx, -360, 360, 1);
			theta = new FWDouble("theta", ly, -360, 360, 1);
			updateDirection();
		}
		
		private GTLight(XMLReader e) {
			loadXMLProperties(e);
		}
		
		private void updateDirection() {
			double ph = phi.getValue()*Math.PI/180;
			double th = theta.getValue()*Math.PI/180;
			double cosPhi  = Math.cos(ph);
			double sinPhi  = Math.sin(ph);
			double cosTheta = Math.cos(th);
			double sinTheta = Math.sin(th);
			this.absoluteDirection = new Point3D(cosPhi * sinTheta, sinPhi, cosPhi * cosTheta);
			try {
				setDirection(absoluteDirection);
			} catch (ZeroVectorException ex) {
				ex.printStackTrace(); // should not occur
			}
		}
	
		private void reset() {
			if (fixed.getValue())
				try {
					setDirection(absoluteDirection);
				} catch (ZeroVectorException ex) {
					ex.printStackTrace(); // should not occur
				}
		}
	
		private void setOrientation(QRotation r) {
			if (fixed.getValue())
				try {
					setDirection(r.inv().apply(absoluteDirection));
				} catch (ZeroVectorException ex) {
					ex.printStackTrace(); // should not occur
				}
		}

		@Override
		public String getXMLTag() {
			return XML_TAG.getXMLTag();
		}

		
		@Override
		public XMLWriter getXMLProperties() {
			XMLWriter e = new XMLWriter(this);
			phi.storeValue(e);
			theta.storeValue(e);
			fixed.storeValue(e);
			return e;
		}

		@Override
		public XMLReader loadXMLProperties(XMLReader e) {
			XMLReader child = e.popChild(this);
			phi.fetchValue(child, 0d);
			theta.fetchValue(child, 0d);
			fixed.fetchValue(child, true);
			updateDirection();
			return child;
		}

		@Override
		public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
			FWParameterListener<Double> l1 = new FWParameterListener<Double>() {
				
				@Override
				public void settingsChanged(Double value) {
					updateDirection();
					actions.fire(FWSettingsActionPuller.REPAINT);
				}
			};
			
			FWParameterListener<Boolean> l2 = new FWParameterListener<Boolean>() {
				
				@Override
				public void settingsChanged(Boolean value) {
					updateDirection();
					actions.fire(FWSettingsActionPuller.REPAINT);
				}
			};
			
			JPanel coordinatesPane = new JPanel(new FlowLayout());
			coordinatesPane.add(new FWLabel(PHI, SwingConstants.RIGHT));
			coordinatesPane.add(phi.getComponent(l1));
			coordinatesPane.add(new FWLabel(THETA, SwingConstants.RIGHT));
			coordinatesPane.add(theta.getComponent(l1));
			JPanel p = VerticalFlowLayout.createPanel(2,
					coordinatesPane, 
					VerticalPairingLayout.createPanel(10, 2, 
							new FWLabel(IS_FIXED, SwingConstants.RIGHT), fixed.getComponent(l2)));
			p.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			return p;
		}
	}
}