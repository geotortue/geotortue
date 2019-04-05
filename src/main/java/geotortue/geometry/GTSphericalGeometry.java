package geotortue.geometry;

import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;

import fw.app.Translator.TKey;
import fw.geometry.GeometryI;
import fw.geometry.SphericalGeometry;
import fw.geometry.util.MathException;
import fw.geometry.util.MathException.ZeroVectorException;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.geometry.util.TangentVector;
import fw.gui.FWColorBox;
import fw.gui.FWComboBox;
import fw.gui.FWComboBox.FWComboBoxListener;
import fw.gui.FWLabel;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalFlowLayout;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWBoolean;
import fw.gui.params.FWColor;
import fw.gui.params.FWDouble;
import fw.gui.params.FWInteger;
import fw.gui.params.FWParameterListener;
import fw.renderer.MouseManager;
import fw.renderer.core.RendererI;
import fw.renderer.light.LightingContext;
import fw.renderer.mesh.FVMesh;
import fw.renderer.mesh.Face;
import fw.renderer.mesh.Mesh;
import fw.renderer.mesh.Sphere3D;
import fw.renderer.shader.FaceShader;
import fw.renderer.shader.FlatShader;
import fw.renderer.shader.GouraudShader;
import fw.renderer.shader.PhongShader;
import fw.renderer.shader.ShaderI;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.core.GTEnhancedJEP;
import geotortue.core.GTJEPFunctionFactory.GFunctionI;
import geotortue.core.Turtle;
import geotortue.core.TurtlePen;
import geotortue.geometry.GTTransport.MODE;
import geotortue.geometry.obj.GTPolygon;
import geotortue.geometry.obj.GTPolygon.NonFlatPolygonException;
import geotortue.geometry.obj.GTSphericalPolygon;
import geotortue.renderer.GTRendererI;
import geotortue.renderer.GTRendererManager.RENDERER_TYPE;
import jep2.JKey;


public class GTSphericalGeometry extends GTGeometry {

	private static final JKey X_FUN = new JKey(GTSphericalGeometry.class, "X");
	private static final JKey Y_FUN = new JKey(GTSphericalGeometry.class, "Y");
	private static final JKey Z_FUN = new JKey(GTSphericalGeometry.class, "Z");
	private static final JKey U_FUN = new JKey(GTSphericalGeometry.class, "U");
	private static final JKey V_FUN = new JKey(GTSphericalGeometry.class, "V");
	private static final JKey R_FUN = new JKey(GTSphericalGeometry.class, "R");
	private static final TKey RADIUS = new TKey(GTSphericalGeometry.class, "radius");
	private static final TKey IS_SPHERE_VISIBLE = new TKey(GTSphericalGeometry.class, "isSphereVisible");
	private static final TKey SPHERE_COLOR = new TKey(GTSphericalGeometry.class, "sphereColor");
	private static final TKey TESSELATION = new TKey(GTSphericalGeometry.class, "tesselation");
	private static final TKey SHADER = new TKey(GTSphericalGeometry.class, "shader");
	
	private FVMesh sphere;
	private FWInteger tessellation = new FWInteger("tessellation", 16, 6, 128);
	
	private Color defaultSphereColor = new Color(255, 204, 153);
	private FWColor sphereColor = new FWColor("sphereColor", defaultSphereColor);
	
	private FWBoolean isSphereVisible = new FWBoolean("isSphereVisible", true);
	private FWDouble radius = new FWDouble("radius", 100, 0.1, 1000, 1);
	
	private double radius(){
		return radius.getValue();
	}
	
	private SphereShader[] shaders = new SphereShader[]{
			new SphereShader(new FlatShader()),
			new SphereShader(new GouraudShader()),
			new SphereShader(new PhongShader())};
	
	private SphereShader shader;
	
	private final SphericalGeometry<GTPoint> delegate = new SphericalGeometry<GTPoint>() {
		@Override
		public String getXMLTag() {
			return "GTSphericalGeometry";
		}
	};
	
	
	public GTSphericalGeometry() {
		shader = shaders[0];
		updateSphere();
	}
	
	
	@Override
	public void teleport(Turtle t, GTPoint p) throws GeometryException, NonFlatPolygonException {
		GTPoint p1 = new GTPoint(p.getU2(), p.getU1(), 0); // reversed to match latitude/longitude
		super.teleport(t, p1);
	}


	@Override
	public double distance(GTPoint p, GTPoint q) {
		return radius()*super.distance(p, q);
	}

	@Override
	public GeometryI<GTPoint> getDelegateGeometry() {
		return delegate;
	}

	public GTTransport getGTTransport(GTPoint gp, TangentVector v, double d0) throws GeometryException {
		double d = d0 / radius();
		Point3D p = get3DCoordinates(gp);

		Point3D axe = MathUtils.crossProduct(p, v.getPoint3D());
		try {
			QRotation r = new QRotation(axe, d);
			GTPoint gq = getSphericCoordinates(r.apply(p));

			if (d > 3 * Math.PI / 2) {
				QRotation r1 = new QRotation(axe, 2 * Math.PI / 3);
				QRotation r2 = new QRotation(axe, 4 * Math.PI / 3);
				GTPoint p1 = getSphericCoordinates(r1.apply(p));
				GTPoint p2 = getSphericCoordinates(r2.apply(p));
				if (d > 2 * Math.PI)
					return new GTTransport(MODE.CUMULATE_ROTATION, r, gp, p1,
							p2, gp, gq);
				return new GTTransport(MODE.CUMULATE_ROTATION, r, gp, p1, p2,
						gq);
			} else if (d > 3 * Math.PI / 4) {
				QRotation r1 = new QRotation(axe, 2 * Math.PI / 3);
				GTPoint q1 = getSphericCoordinates(r1.apply(p));
				return new GTTransport(MODE.CUMULATE_ROTATION, r, gp, q1, gq);
			}
			return new GTTransport(MODE.CUMULATE_ROTATION, r, gp, gq);
		} catch (ZeroVectorException ex) {
			ex.printStackTrace(); // should not occur
			return new GTTransport(gp, gp);
		}
	}

	private GTPoint getSphericCoordinates(Point3D p) {
		double x = p.x;
		double y = p.y;
		double z = p.z;
	
		double phi = Math.asin(y);
		if (Double.isNaN(phi)) {
			if (y >= 1)
				phi = Math.PI/2;
			if (y <= -1)
				phi = -Math.PI/2;
		}
		
		double theta = Math.atan2(x, z);
		return new GTPoint(theta, phi, 0);
	}

	@Override
	public void resetToNorthOrientation(Turtle t) {
		GTPoint p = t.getPosition();
		Point3D u = getUTheta(p);
		Point3D v = getUPhi(p);
		if (v.y<0) {
			u=u.opp();
			v=v.opp();
		}
		try {
			QRotation r = new QRotation(u, v);
			t.setRotation(new GTRotation(r));
		} catch (MathException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void setParallelOrientation(Turtle ref, Turtle t) throws GeometryException {
		resetToNorthOrientation(t);
		GTPoint p = ref.getPosition();
		Point3D n = get3DCoordinates(p);
		Point3D uPhi = getUPhi(p);
		Point3D hy = ref.getRotation().apply(new Point3D(0, 1, 0));
		Point3D refn = MathUtils.crossProduct(uPhi, hy);
		double angle = Math.atan2(refn.abs(), MathUtils.dotProduct(hy, uPhi));
		if (MathUtils.dotProduct(n, refn)<0)
			angle = -angle;
		
		Point3D tn = get3DCoordinates(t.getPosition());
		try {
			QRotation r = new QRotation(tn, angle);
			r = r.apply(t.getRotation());
			t.setRotation(new GTRotation(r));
		} catch (ZeroVectorException ex) { // TODO : test this (tortue au pole nord ?)
			ex.printStackTrace();
			//throw new GeometryException(GTTrouble.IMITATE);
		}
	}

	@Override
	public GTRotation getOrientation(Turtle t1, Turtle t2) {
		Point3D p = get3DCoordinates(t1.getPosition());
		Point3D q = get3DCoordinates(t2.getPosition());
		Point3D u = MathUtils.crossProduct(q, p);
		Point3D v = MathUtils.crossProduct(p, u);
		try {
			QRotation r = new QRotation(u, v);
			return new GTRotation(r);
		} catch (MathException e) {
			e.printStackTrace();
		}
		return new GTRotation();
	}

	private Point3D getUPhi(GTPoint p) {
		double theta = p.getU1();
		double phi = p.getU2();
		double cosPhi  = Math.cos(phi);
		double sinPhi  = Math.sin(phi);
		double cosTheta = Math.cos(theta);
		double sinTheta = Math.sin(theta);
		return new Point3D(-sinPhi*sinTheta, cosPhi, -sinPhi*cosTheta);
	}


	private Point3D getUTheta(GTPoint p) {
		double theta = p.getU1();
		return new Point3D(Math.cos(theta), 0, -Math.sin(theta));
	}
	
	
	@Override
	public void fill(final GTPolygon poly, final GTRendererI r) {
		FVMesh mesh = ((GTSphericalPolygon) poly).getMesh(GTSphericalGeometry.this, r);
		if (mesh != null) 
			r.draw(mesh);
	}
	
	@Override
	public GTPolygon createPolygon(GTPoint position, TurtlePen pen) {
		return new GTSphericalPolygon(position, pen) ;
	}
	
	public final void update(MouseManager m) {
		m.setAllAbilitiesAvailable(true);
	}
	
	private void updateSphere() {
		sphere = new Sphere3D(0.99, tessellation.getValue()).getMesh(sphereColor.getValue(), shader);
	}
	
	
	@Override
	public void paintBackground(RendererI<GTPoint> rc) {
		if (isSphereVisible.getValue())
			rc.draw(sphere);
	}
	
	public void centerWorldOn(GTPoint gp, GTRendererI r) {
		double u = r.getUnit();
		init(r);
		Point3D p = get3DCoordinates(gp);
		double angle = Math.acos(p.z);
		try {
			r.setSpaceTransform(new QRotation(new Point3D(p.y, -p.x, 0), angle));
		} catch (ZeroVectorException ex) {
			ex.printStackTrace(); // should not occur
		}
		r.setUnit(u);
	}
	
	@Override
	public RENDERER_TYPE getRendererType() {
		return RENDERER_TYPE.FW3D;
	}

	
	/*
	 * XML
	 */

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = super.getXMLProperties();
		isSphereVisible.storeValue(e);
		radius.storeValue(e);
		tessellation.storeValue(e);
		sphereColor.storeValue(e);
		e.setAttribute("shader", shader.getXMLTag());
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = super.loadXMLProperties(e);
		radius.fetchValue(child, 100d);
		isSphereVisible.fetchValue(child, true);
		tessellation.fetchValue(child, 8);
		sphereColor.fetchValue(e, defaultSphereColor);
		String shaderKey = child.getAttribute("shader", shader.getXMLTag());
		for (SphereShader s : shaders)
			if (s.getXMLTag().equals(shaderKey))
				shader = s;
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
		
		JCheckBox isSphereVisibleCB = isSphereVisible.getComponent(new FWParameterListener<Boolean>() {
			@Override
			public void settingsChanged(Boolean value) {
				actions.fire(FWSettingsActionPuller.REPAINT);
			}
		});
		
		FWColorBox sphereColorCB = sphereColor.getComponent(new FWParameterListener<Color>() {
			@Override
			public void settingsChanged(Color value) {
				updateSphere();
				actions.fire(FWSettingsActionPuller.REPAINT);
			}
		});
		
		JSpinner tessellationSpinner = tessellation.getComponent(new FWParameterListener<Integer>() {
			@Override
			public void settingsChanged(Integer value) {
				updateSphere();
				actions.fire(FWSettingsActionPuller.REPAINT);
			}
		});
		
		FWComboBox shaderCB = new FWComboBox(shaders, shader, new FWComboBoxListener() {
			public void itemSelected(Object o) {
				shader = (SphereShader) o;
				updateSphere();
				actions.fire(FWSettingsActionPuller.REPAINT);
			}
		});

		
		return VerticalFlowLayout.createPanel(10, 
				VerticalPairingLayout.createPanel(10, 10, 
						new FWLabel(RADIUS, SwingConstants.RIGHT), radiusSpinner),
				VerticalPairingLayout.createPanel(10, 10, 
						new FWLabel(IS_SPHERE_VISIBLE, SwingConstants.RIGHT), isSphereVisibleCB,
						new FWLabel(SPHERE_COLOR, SwingConstants.RIGHT), sphereColorCB,
						new FWLabel(TESSELATION, SwingConstants.RIGHT), tessellationSpinner, 
						new FWLabel(SHADER, SwingConstants.RIGHT), shaderCB));
	}
	
	
	/*
	 * Shader
	 */
	private class SphereShader implements ShaderI {
		
		private final ShaderI delegateShader;
		
		public SphereShader(ShaderI s) {
			this.delegateShader = s;
		}
		
		public FaceShader getFaceShader(LightingContext lc, Face f) {
			return delegateShader.getFaceShader(lc, f);
		}

		public void prepare(Mesh mesh) {
			delegateShader.prepare(mesh);
		}
		
		public String toString() {
			return getName().translate();
		}
		
		@Override
		public TKey getName() {
			return delegateShader.getName();
		}

		@Override
		public String getXMLTag() {
			return delegateShader.getXMLTag();
		}
	}
	
	/*
	 * JEP
	 */
	

	@Override
	public void addFunctions(final GTEnhancedJEP jep) {
		super.addFunctions(jep);
		jep.addGFunction(X_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return get3DCoordinates(ps[0]).x*radius();
			}
		});
		
		jep.addGFunction(Y_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return get3DCoordinates(ps[0]).y*radius();
			}
		});
		
		jep.addGFunction(Z_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return get3DCoordinates(ps[0]).z*radius();
			}
		});
		
		jep.addGFunction(U_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return jep.convertToCurrentAngleMode(ps[0].getU2());
			}
		});
		
		jep.addGFunction(V_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return jep.convertToCurrentAngleMode(ps[0].getU1());
			}
		});
		
		jep.addGFunction(R_FUN, 0, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return radius();
			}
		});
	}
}