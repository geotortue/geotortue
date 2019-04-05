package geotortue.geometry;

import java.awt.event.MouseEvent;
import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;

import fw.app.FWToolKit;
import fw.app.Translator.TKey;
import fw.geometry.GeometryI;
import fw.geometry.PoincareDiscGeometry;
import fw.geometry.obj.GPoint;
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
import geotortue.geometry.obj.GTDiscGeoPolygon;
import geotortue.geometry.obj.GTPolygon;
import geotortue.geometry.obj.GTPolygon.NonFlatPolygonException;
import geotortue.renderer.GTRendererI;
import geotortue.renderer.GTRendererManager.RENDERER_TYPE;
import jep2.JKey;


public class GTPoincareDiscGeometry extends GTGeometry {
	
	private static final JKey R_FUN = new JKey(GTPoincareDiscGeometry.class, "R");
	private static final JKey Y_FUN = new JKey(GTPoincareDiscGeometry.class, "Y");
	private static final JKey X_FUN = new JKey(GTPoincareDiscGeometry.class, "X");
	private static final TKey H_SHIFT = new TKey(GTPoincareDiscGeometry.class, "hShift");
	private static final TKey H_ROTATION = new TKey(GTPoincareDiscGeometry.class, "hRotation");
	private static final TKey RADIUS = new TKey(GTPoincareDiscGeometry.class, "radius");
	
	private FWDouble radius = new FWDouble("radius", 100, 0.1, 1000, 1);
	protected FWBoolean hRotationEnabled = new FWBoolean("hRotationEnabled", true);
	protected FWBoolean hShiftEnabled = new FWBoolean("hShiftEnabled", true);
	private double phi = 0;
	private GPoint shift = new GPoint(0, 0);
	
	final private DelegateGeometry delegate = new DelegateGeometry();
	
	private class DelegateGeometry extends PoincareDiscGeometry<GTPoint> {
		@Override
		public String getXMLTag() {
			return "GTPoincareDiscGeometry";
		}
		
		@Override
		public Point3D get3DCoordinates(GTPoint gp) { 
			double x0 = gp.getU1();
			double y0 = gp.getU2();
			
			double x = x0;
			double y = y0;
			if (phi!=0) {
				double cosPhi = Math.cos(phi);
				double sinPhi = Math.sin(phi);
				x = cosPhi*x0 - sinPhi*y0;
				y = sinPhi*x0 + cosPhi*y0;
			}
			
			double u = shift.getU1();
			double v = shift.getU2();
			
			double t = Math.abs(u) +Math.abs(v);
			if (t==0) 
				return super.get3DCoordinates(new GTPoint(x, y, 0));

			double z = Math.sqrt(1+x*x+y*y);
			
			double psi = Math.atan2(-u, v);
			double cosht = Math.cosh(t);
			double sinht = Math.sinh(t);
			double c = 1-cosht;
			
			double cosPsi = Math.cos(psi);
			double sinPsi = Math.sin(psi);
			double cosPsi2 = cosPsi*cosPsi;
			double sinPsi2 = sinPsi*sinPsi;
			double cosSinPsi = cosPsi*sinPsi;
			
			double x1 = (1-c*sinPsi2)*x + c*cosSinPsi*y - sinht*sinPsi*z;
			double y1 = c*cosSinPsi*x + (1-c*cosPsi2)*y + sinht*cosPsi*z;
			
			return super.get3DCoordinates(new GTPoint(x1, y1, 0));
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

		double ux = ( ds*(1+x*x+z) + dt*x*y )/(z+1);
		double uy = ( dt*(1+y*y+z) + ds*x*y )/(z+1);
		double uz = x*ds + y*dt;
		
		double x1 = x*coshd + sinhd*ux;
		double y1 = y*coshd + sinhd*uy;
		double z1 = z*coshd + sinhd*uz;
		
		try {
			delegate.check(x1, y1, z1);
		} catch (MathException ex) {
			throw new GeometryException(GTTrouble.GTJEP_DISC_TRANSPORT);
		}
		
		GTPoint gq = new GTPoint(x1, y1, z1);
		
		double ds1 = ux*coshd + x*sinhd +ux*z-uz*x;
		double dt1 = uy*coshd + y*sinhd +uy*z-uz*y;
		
		double theta = Math.atan2(dt1, ds1);
		QRotation r = QRotation.getZRotation(theta-Math.PI/2);		
		return new GTTransport(MODE.REDEFINE_ROTATION, r, gp, gq);
	}
	
	public void teleport(Turtle turtle, GTPoint p) throws GeometryException, NonFlatPolygonException {
		double s = p.getU1()/radius.getValue();
		double t = p.getU2()/radius.getValue();
		if (s*s + t*t > 1)
			throw new GeometryException(GTTrouble.GTJEP_DISC_TELEPORTATION, "("+p.getU1()+", "+p.getU2()+")");
		double d = (1-s*s-t*t);
		double u = 2*s/d; 
		double v = 2*t/d;			
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
		
		double ds = qx*(pz+1)-px*(qz+coshd);
		double dt = qy*(pz+1)-py*(qz+coshd);
		
		double theta = Math.atan2(dt, ds);
		return GTRotation.getZRotation(theta - Math.PI/2);
	}
	
	@Override
	public void fill(GTPolygon poly, GTRendererI renderer) { 
		Mesh mesh = ((GTDiscGeoPolygon) poly).getMesh(this, renderer);
		renderer.draw(mesh);
	}
	
	@Override
	public GTPolygon createPolygon(GTPoint position, TurtlePen pen) {
		return new GTDiscGeoPolygon(position, pen) ;
	}

	public QRotation getOrientationAt(GTPoint gp) {
		if (shift.getU1()==0 && shift.getU2()==0) {
			if (phi!=0)
				return QRotation.getZRotation(phi);
			else
				return super.getOrientationAt(gp);
		}
		
		double x0 = gp.getU1();
		double y0 = gp.getU2();
		double x = x0;
		double y = y0;
		double cosPhi = Math.cos(phi);
		double sinPhi = Math.sin(phi);
		double x2 = x*x;
		double y2 = y*y;
		double z2 = x2+y2+1;
		double z = Math.sqrt(z2);

		double u = shift.getU1();
		double v = shift.getU2();
		
		double t = Math.abs(u) +Math.abs(v);
		double cosht = Math.cosh(t);
		double sinht = Math.sinh(t);
		
		double psi = Math.atan2(-u, v);
		double cosPsi = Math.cos(psi);
		double sinPsi = Math.sin(psi);

		double C = cosht-1;
		
		//double ds = (((cosht-1)*z2+(2-2*cosht)*y2-cosht+1)*sinPhi+(-sinht*x*z-sinht*x)*cosPsi+(2*cosht-2)*x*y*cosPhi)*sinPsi2+((2-2*cosht)*x*y*cosPsi*sinPhi+((cosht-1)*z2+(2-2*cosht)*y2-cosht+1)*cosPhi*cosPsi-sinht*y*z-sinht*y)*sinPsi+(-cosht*z2+(-cosht-1)*z+(cosht-1)*y2-1)*sinPhi+(-sinht*x*z-sinht*x)*cosPsi2*cosPsi+(1-cosht)*x*y*cosPhi;
		//double dt = ((2-2*cosht)*x*y*sinPhi+((cosht-1)*z2+(2-2*cosht)*y2-cosht+1)*cosPhi)*sinPsi2+(((1-cosht)*z2+(2*cosht-2)*y2+cosht-1)*cosPsi*sinPhi+(2-2*cosht)*x*y*cosPhi*cosPsi-sinht*x*z-sinht*x)*sinPsi+(cosht-1)*x*y*sinPhi+(sinht*y*z+sinht*y)*cosPsi+(z2+(cosht+1)*z+(cosht-1)*y2+cosht)*cosPhi;
		
		double zp1 = z+1;
		double zp12 = zp1*zp1;
		double xy= x*y;
		
		//double ds = (-2*x*y*C*cosPsi*sinPhi+(x2-y2)*C*cosPhi*cosPsi-sinht*y*(z+1))*sinPsi+((y2-x2)*C*cosPsi2+(-z-y2-1)*C-(z+1)*(z+1))*sinPhi-2*x*y*C*cosPhi*cosPsi2 -sinht*x*(z+1)*cosPsi+x*y*C*cosPhi;
		//double dt = ((x2-y2)*C*cosPhi-2*x*y*C*sinPhi)*sinPsi2+((y2-x2)*C*cosPsi*sinPhi-2*x*y*C*cosPhi*cosPsi-sinht*x*z-sinht*x)*sinPsi+x*y*C*sinPhi+(sinht*y*z+sinht*y)*cosPsi+((z+y2+1)*C+2*z+y2+x2+2)*cosPhi;
		//dt = ((x2-y2)*C*cosPhi-2*x*y*C*sinPhi)*sinPsi2+((y2-x2)*C*cosPsi*sinPhi-2*x*y*C*cosPhi*cosPsi-sinht*x*z-sinht*x)*sinPsi+x*y*C*sinPhi+(sinht*y*z+sinht*y)*cosPsi+((z+y2+1)*C+2*z+y2+x2+2)*cosPhi;
		
		double psiMinusPhi = psi-phi;
		double cosPsiMinusPhi = Math.cos(psiMinusPhi);
		double sinPsiMinusPhi = Math.sin(psiMinusPhi);
		
		double ds = C*((x2-y2)*cosPsiMinusPhi + 2*xy*sinPsiMinusPhi)*sinPsi - sinht*zp1*(x*cosPsi + y*sinPsi) - xy*C*cosPhi - (C*(x2+zp1) + zp12)*sinPhi;
		double dt = C*((x2-y2)*sinPsiMinusPhi - 2*xy*cosPsiMinusPhi)*sinPsi - sinht*zp1*(x*sinPsi - y*cosPsi) + xy*C*sinPhi + (C*(y2+zp1) + zp12)*cosPhi;
		
		double beta = Math.atan2( dt, ds) - Math.PI/2;
		return QRotation.getZRotation(beta);
	}

	public void init(RendererI<GTPoint> r) {
		super.init(r);
		phi = 0;
		shift = new GPoint(0, 0);
		addSpecialListener(r);
	}
	
	private HashSet<RendererI<GTPoint>> registeredRenderers = new HashSet<RendererI<GTPoint>>();
	
	private void addSpecialListener(final RendererI<GTPoint> r) {
		if (registeredRenderers.contains(r))
			return;
		FWMouseListener hRotationListener = new FWMouseListener() {
			private double phi0;
			private GPoint shift0; 

			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if (getMouseModifiers() == RIGHT_SHIFT && hRotationEnabled.getValue()) {
					phi0  = phi;
					r.getPane().setCursor(FWToolKit.getCursor("turnZ.gif"));
				} else if (getMouseModifiers() == RIGHT_CTRL && hShiftEnabled.getValue()) {
					shift0 = shift;
					r.getPane().setCursor(FWToolKit.getCursor("move.gif"));
				}
			}

			@Override
			public void mouseDragged(int x, int y, int mode) {
				if (mode == RIGHT_SHIFT && hRotationEnabled.getValue()) {
					phi = phi0 + x/200.;
					r.getPane().repaint();
				} else if (getMouseModifiers() == RIGHT_CTRL && hShiftEnabled.getValue()) {
					double step = Math.min(0.6, r.getWidth()/r.getUnit());
					shift = shift0.getTranslated(x*step/100., -y*step/100.);
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
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = super.getXMLProperties();
		radius.storeValue(e);
		hRotationEnabled.storeValue(e);
		hShiftEnabled.storeValue(e);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = super.loadXMLProperties(e);
		radius.fetchValue(child, 1d);
		hRotationEnabled.fetchValue(child, true);
		hShiftEnabled.fetchValue(child, true);
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
						new FWLabel(H_ROTATION, SwingConstants.RIGHT), hRotationEnabled.getComponent(),
						new FWLabel(H_SHIFT, SwingConstants.RIGHT), hShiftEnabled.getComponent());
	}

	public void doHRotation(double angle) {
		phi += angle;
	}

	public void doHTranslation(double x, double y) {
		shift = shift.getTranslated(x, y);
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
//		jep.addGFunction(new JKey(GTPoincareDiscGeometry.class, "U1"), 1, new GFunctionI() {
//			@Override
//			public double getValue(GTPoint... ps) {
//				return ps[0].getU1();
//			}
//		});
//		
//		jep.addGFunction(new JKey(GTPoincareDiscGeometry.class, "U2"), 1, new GFunctionI() {
//			@Override
//			public double getValue(GTPoint... ps) {
//				return ps[0].getU2();
//			}
//		});
//		
//		jep.addGFunction(new JKey(GTPoincareDiscGeometry.class, "U3"), 1, new GFunctionI() {
//			@Override
//			public double getValue(GTPoint... ps) {
//				return ps[0].getU3();
//			}
//		});
		
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