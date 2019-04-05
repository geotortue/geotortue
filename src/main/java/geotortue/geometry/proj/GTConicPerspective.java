package geotortue.geometry.proj;

import java.awt.Dimension;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import fw.app.Translator.TKey;
import fw.geometry.proj.Perspective;
import fw.geometry.proj.PerspectiveI;
import fw.geometry.proj.ZPoint;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;
import fw.gui.FWLabel;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWAngle;
import fw.gui.params.FWParameterListener;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class GTConicPerspective extends GTPerspective {
	
	private static final TKey NAME = new TKey(GTConicPerspective.class, "name");

	@Override
	public TKey getTitle() {
		return NAME;
	}


	private static final TKey FOCAL_DISTANCE = new TKey(GTConicPerspective.class, "focalDistance");
	private static final TKey FOV = new TKey(GTConicPerspective.class, "fieldOfView");

	private final DelegatePerspective delegate = new DelegatePerspective();

	private double f = 1000;
	private JLabel focalLabel = new JLabel("");
	
	private FWAngle fov = new FWAngle("fieldOfView", 47*Math.PI/180, 1, 160);
	
	public GTConicPerspective() {
		updateFocale();
	}


	private void updateFocale() {
		f = delegate.getDiagonal() / (2*Math.tan(fov.getValue() /2));
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setDecimalSeparatorAlwaysShown(true);
		focalLabel.setText(df.format(f));
	}
	
	
	private class DelegatePerspective extends Perspective{
		@Override
		public double getMaximumZDepth() {
			return f;
		}
		
		@Override
		public ZPoint toZSpace(Point3D p) throws InvisibleZPointException {
			if (p.z >= f )
				throw new InvisibleZPointException(this, p.z);
			double lambda = f /(f - p.z);
			double x = lambda * p.x;
			double y = lambda * p.y;
			double z = lambda * p.z;
			return new ZPoint(x, y, z);
		}
		
		@Override
		public Point3D liftTo3DSpace(ZPoint p) {
			double lambdaI = (f - p.z)/ f;
			double x = lambdaI * p.x;
			double y = lambdaI * p.y;
			return new Point3D(x, y, p.z);
		}
		
		private double getDiagonal() {
			Dimension size = getScreenSize();
			Point3D tlCorner = liftTo3DSpace(new Pixel(0, 0));
			Point3D brCorner = liftTo3DSpace(new Pixel(size.width, size.height));
			return MathUtils.abs(tlCorner.x - brCorner.x, tlCorner.y - brCorner.y);
		}
		
		public void setScreenSize(Dimension d) {
			super.setScreenSize(d);
			updateFocale();
		}
	};
	

	@Override
	protected PerspectiveI getDelegateProjection() {
		return delegate;
	}

	@Override
	public String getXMLTag() {
		return "GTConicPerspective";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		fov.storeValue(e);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = super.loadXMLProperties(e);
		fov.fetchValue(child, 47*Math.PI/180);
		return child;
	}
	
	/*
	 * FWS
	 */
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		JSpinner fovSpinner = fov.getComponent(new FWParameterListener<Double>() {
			@Override
			public void settingsChanged(Double value) {
				updateFocale();
				actions.fire(GTPerspectiveManager.UPDATE_PERSPECTIVE);
			}
		});
		return VerticalPairingLayout.createPanel(10, 10, 
				new FWLabel(FOV), fovSpinner,
				new FWLabel(FOCAL_DISTANCE), focalLabel);
	}


	public double getFocalDistance() {
		return f;
	}
}
