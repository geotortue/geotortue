package geotortue.geometry;
import java.awt.event.MouseEvent;
import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;

import fw.app.FWToolKit;
import fw.app.Translator.TKey;
import fw.geometry.GeometryI;
import fw.geometry.PoincareHPGeometry;
import fw.geometry.util.MathException;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.geometry.util.TangentVector;
import fw.gui.FWLabel;
import fw.gui.FWMouseListener;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWBoolean;
import fw.gui.params.FWDouble;
import fw.gui.params.FWParameterListener;
import fw.renderer.MouseManager;
import fw.renderer.core.RendererI;
import fw.renderer.mesh.Mesh;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.core.GTEnhancedJEP;
import geotortue.core.GTJEPFunctionFactory.GFunctionI;
import geotortue.core.GTMessageFactory.GTTrouble;
import geotortue.core.Turtle;
import geotortue.core.TurtlePen;
import geotortue.geometry.GTTransport.MODE;
import geotortue.geometry.obj.GTHPGeoPolygon;
import geotortue.geometry.obj.GTPolygon;
import geotortue.geometry.obj.GTPolygon.NonFlatPolygonException;
import geotortue.renderer.GTRendererI;
import geotortue.renderer.GTRendererManager.RENDERER_TYPE;
import jep2.JKey;


public class GTPoincareHPGeometry extends GTGeometry {

	private static final JKey R_FUN = new JKey(GTPoincareHPGeometry.class, "R");
	private static final JKey Y_FUN = new JKey(GTPoincareHPGeometry.class, "Y");
	private static final JKey X_FUN = new JKey(GTPoincareHPGeometry.class, "X");
	private static final TKey RADIUS = new TKey(GTPoincareHPGeometry.class, "radius");
	private static final TKey ROTATION = new TKey(GTPoincareHPGeometry.class, "rotation");
	
	private FWDouble radius = new FWDouble("radius", 100, 0.1, 1000, 1);
	protected FWBoolean hRotationEnabled = new FWBoolean("hRotationEnabled", true);
	private double phi = 0;
	
	final private DelegateGeometry delegate = new DelegateGeometry();
	
	private class DelegateGeometry extends PoincareHPGeometry<GTPoint> {
		@Override
		public String getXMLTag() {
			return "GTPoincareHPGeometry";
		}
		
		@Override
		public Point3D get3DCoordinates(GTPoint gp) {
			double u0 = gp.getU1();
			double v0 = gp.getU2();
			
			double u = u0;
			double v = v0;
			if (phi!=0) {
				double cosPhi = Math.cos(phi);
				double sinPhi = Math.sin(phi);
				u = cosPhi*u0 - sinPhi*v0;
				v = sinPhi*u0 + cosPhi*v0;
			}
			
			return super.get3DCoordinates(new GTPoint(u, v, 0));
		}
		
		public Point3D getAbsolute3DCoordinates(GTPoint gp) {
			return super.get3DCoordinates(new GTPoint(gp.getU1(), gp.getU2(), 0));
		}
	}
	
	@Override
	protected GeometryI<GTPoint> getDelegateGeometry() {
		return delegate;
	}
	
	@Override
	public double distance(GTPoint p, GTPoint q) {
		return radius.getValue()*super.distance(p, q);
	}

	@Override
	public GTTransport getGTTransport(GTPoint gp, TangentVector v, double d0) throws GeometryException {
			double ds = v.x;
			double dt = v.y;
			
			double x = gp.getU1();
			double y = gp.getU2();
			double z = Math.sqrt(1+x*x+y*y);

			double d = d0/radius.getValue();
			double coshd = Math.cosh(d);
			double sinhd = Math.sinh(d);

			double lambda = coshd - sinhd*dt;
			
			double u = sinhd*(y*ds + dt)/(x+z);
			
			double x1 = x*lambda - u;
			double y1 = y*lambda + sinhd*ds;
			double z1 = z*lambda + u;
			
			try {
				delegate.check(x1, y1, z1);
			} catch (MathException ex) {
				throw new GeometryException(GTTrouble.GTJEP_HP_TRANSPORT, ex.getMessage());
			}
			
			GTPoint gq = new GTPoint(x1, y1, z1);

			double theta = Math.atan2(coshd*dt - sinhd, ds);
			QRotation r = QRotation.getZRotation(theta-Math.PI/2);
			
			return new GTTransport(MODE.REDEFINE_ROTATION, r, gp, gq);
	}
	
	public void teleport(Turtle turtle, GTPoint p) throws GeometryException, NonFlatPolygonException {
		if (p.getU2()<=0)
			throw new GeometryException(GTTrouble.GTJEP_HP_TELEPORTATION, "("+p.getU1()+"; "+p.getU2()+")");
		double s = p.getU1()/radius.getValue();
		double t = p.getU2()/radius.getValue();
		double u = (1-s*s-t*t)/(2*t); 
		double v = s/t;
		super.teleport(turtle, new GTPoint(u, v, 0));
	}

	@Override
	public GTRotation getOrientation(Turtle t1, Turtle t2) {
		GTPoint p = t1.getPosition();
		GTPoint q = t2.getPosition();

		double px = p.getU1();
		double py = p.getU2();
		double pz = Math.sqrt(1+px*px+py*py);

		double qx = q.getU1();
		double qy = q.getU2();
		double qz = Math.sqrt(1+qx*qx+qy*qy);
		
		double coshd = - px*qx - py*qy + pz*qz;
		
		double ds = qy*(px+pz) - py*(qx+qz);
		double dt = coshd*(pz+px) - (qz+qx);
		
		double theta = Math.atan2(dt, ds);
		return GTRotation.getZRotation(theta - Math.PI/2);
	}
	
	@Override
	public void fill(GTPolygon poly, GTRendererI renderer) { 
		Mesh mesh = ((GTHPGeoPolygon) poly).getMesh(this, renderer);
		renderer.draw(mesh);
	}
	
	@Override
	public GTPolygon createPolygon(GTPoint position, TurtlePen pen) {
		return new GTHPGeoPolygon(position, pen) ;
	}
	
	public QRotation getOrientationAt(GTPoint gp) {
		if (phi==0)
			return super.getOrientationAt(gp);
		double x = gp.getU1();
		double y = gp.getU2();
		if (x==0 && y==0)
			return QRotation.getZRotation(phi);
		double x2 = x*x;
		double y2 = y*y;
		double z = Math.sqrt(x2+y2+1);
		double x_plus_z = x+z;

		double cosPhi = Math.cos(phi);
		double sinPhi = Math.sin(phi);
		
		double dt = x_plus_z*(y*sinPhi - x*cosPhi) - x*z -x2 -y2 - cosPhi;
		double ds = x_plus_z*sinPhi+ y*(cosPhi-1);
		
		double beta = Math.atan2( dt, ds) + Math.PI/2;
		return QRotation.getZRotation(beta);
	}
	
	public void init(final RendererI<GTPoint> r) {
		super.init(r);
		phi = 0;
		addSpecialListener(r);
	}
	
	private HashSet<RendererI<GTPoint>> registeredRenderers = new HashSet<RendererI<GTPoint>>();
	
	private void addSpecialListener(final RendererI<GTPoint> r) {
		if (registeredRenderers.contains(r))
			return;
		FWMouseListener hRotationListener = new FWMouseListener() {
			private double phi0;

			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if (getMouseModifiers() == RIGHT_SHIFT && hRotationEnabled.getValue()) {
					phi0  = phi;
					r.getPane().setCursor(FWToolKit.getCursor("turnZ.gif"));
				}
			}

			@Override
			public void mouseDragged(int x, int y, int mode) {
				if (mode == RIGHT_SHIFT && hRotationEnabled.getValue()) {
					double step = r.getWidth()/r.getUnit();
					step = (step>1) ? 1/(1+step) : step/2; // ad hoc
					phi = phi0 + x*step/100;
					r.getPane().repaint();
				}
			}
		};
		
		JPanel pane = r.getPane();
		pane.addMouseListener(hRotationListener);
		pane.addMouseMotionListener(hRotationListener);
		registeredRenderers.add(r);
	}
	
	@Override
	public void update(MouseManager m) {
		m.setAllAbilitiesAvailable(false);
		m.translationAbility.setAvailable(true);
		m.zoomAbility.setAvailable(true);
	}
	
	/*
	 * XML
	 */
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = super.getXMLProperties();
		radius.storeValue(e);
		hRotationEnabled.storeValue(e);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = super.loadXMLProperties(e);
		radius.fetchValue(child, 1d);
		hRotationEnabled.fetchValue(child, true);
		return child;
	}
	
	/*
	 * FWS
	 */
	
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		JSpinner radiusSpinner = radius.getComponent(new FWParameterListener<Double>() {
			@Override
			public void settingsChanged(Double value) {
				actions.fire(FWSettingsActionPuller.REPAINT); 
			}
		});
		return 	VerticalPairingLayout.createPanel(10, 10, 
						new FWLabel(RADIUS, SwingConstants.RIGHT), radiusSpinner,
						new FWLabel(ROTATION, SwingConstants.RIGHT), hRotationEnabled.getComponent());
	}

	public void doHRotation(double u) {
		phi += u;
	}
	
	@Override
	public RENDERER_TYPE getRendererType() {
		return RENDERER_TYPE.FW2D;
	}

	/*
	 * JEP
	 */
	

	@Override
	public void addFunctions(GTEnhancedJEP jep) {
		super.addFunctions(jep);
//		jep.addGFunction("U1", 1, new GFunctionI() {
//			@Override
//			public double getValue(GTPoint... ps) {
//				return ps[0].getU1();
//			}
//		});
//		
//		jep.addGFunction("U2", 1, new GFunctionI() {
//			@Override
//			public double getValue(GTPoint... ps) {
//				return ps[0].getU2();
//			}
//		});
//		
//		jep.addGFunction("U3", 1, new GFunctionI() {
//			@Override
//			public double getValue(GTPoint... ps) {
//				return ps[0].getU3();
//			}
//		});
//		
		
		jep.addGFunction(X_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return delegate.getAbsolute3DCoordinates(ps[0]).x*radius.getValue();
			}
		});
		
		jep.addGFunction(Y_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return delegate.getAbsolute3DCoordinates(ps[0]).y*radius.getValue();
			}
		});
		
		jep.addGFunction(R_FUN, 0, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return radius.getValue();
			}
		});
	}
}