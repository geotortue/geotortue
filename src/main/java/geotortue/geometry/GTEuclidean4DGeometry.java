package geotortue.geometry;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashSet;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.FWActionManager;
import fw.app.FWToolKit;
import fw.app.Translator.TKey;
import fw.app.header.FWToolBar;
import fw.files.FileUtilities.HTTPException;
import fw.geometry.Euclidean4DGeometry;
import fw.geometry.GeometryI;
import fw.geometry.util.MathException.ZeroVectorException;
import fw.geometry.util.Point3D;
import fw.geometry.util.Point4D;
import fw.geometry.util.QRotation;
import fw.geometry.util.QRotation4D;
import fw.geometry.util.Quaternion;
import fw.geometry.util.TangentVector;
import fw.gui.FWLabel;
import fw.gui.FWMouseListener;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWAngle;
import fw.gui.params.FWBoolean;
import fw.gui.params.FWParameterListener;
import fw.renderer.MouseManager;
import fw.renderer.core.RendererI;
import fw.renderer.mesh.FVMesh;
import fw.xml.XMLException;
import fw.xml.XMLFile;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.core.GTEnhancedJEP;
import geotortue.core.GTJEPFunctionFactory.GFunctionI;
import geotortue.core.Turtle;
import geotortue.core.TurtlePen;
import geotortue.geometry.obj.GTPolygon;
import geotortue.geometry.obj.GTVerticesPolygon;
import geotortue.renderer.GTRendererI;
import geotortue.renderer.GTRendererManager.RENDERER_TYPE;
import jep2.JKey;


public class GTEuclidean4DGeometry extends GTGeometry {
	private static final JKey X4_FUN = new JKey(GTEuclidean4DGeometry.class, "X4");
	private static final JKey X3_FUN = new JKey(GTEuclidean4DGeometry.class, "X3");
	private static final JKey X2_FUN = new JKey(GTEuclidean4DGeometry.class, "X2");
	private static final JKey X1_FUN = new JKey(GTEuclidean4DGeometry.class, "X1");
	private static final TKey TOOLBAR = new TKey(GTEuclidean4DGeometry.class, "toolbar");
	private static final TKey FOV = new TKey(GTEuclidean4DGeometry.class, "fieldOfView");
	private static final TKey CONIC_PROJECTION = new TKey(GTEuclidean4DGeometry.class, "conicProjection");
	private static final ActionKey TXY_VIEW = new ActionKey(GTEuclidean4DGeometry.class, "txyView");
	private static final ActionKey ZTX_VIEW = new ActionKey(GTEuclidean4DGeometry.class, "ztxView");
	private static final ActionKey YZT_VIEW = new ActionKey(GTEuclidean4DGeometry.class, "yztView");
	private static final ActionKey XYZ_VIEW = new ActionKey(GTEuclidean4DGeometry.class, "xyzView");
	private static final ActionKey YZ_ROTATION = new ActionKey(GTEuclidean4DGeometry.class, "yzRotation");
	private static final ActionKey XZ_ROTATION = new ActionKey(GTEuclidean4DGeometry.class, "xzRotation");
	private static final ActionKey XY_ROTATION = new ActionKey(GTEuclidean4DGeometry.class, "xyRotation");
	

	private JToolBar toolbar;
	final private FWActionManager actionManager = new FWActionManager();
	public static enum ROTATION {xt, yt, zt, xy, yz, xz};
	private ROTATION rotationType = ROTATION.xz;
	private double f = 1000;
	private FWBoolean conicProjection = new FWBoolean("conicProjection", true);
	
	private FWAngle fov = new FWAngle("fieldOfView", 47*Math.PI/180, 1, 120);
	
	public GTEuclidean4DGeometry() {
		super();
		
		this.fov.addParamaterListener(new FWParameterListener<Double>() {
			
			@Override
			public void settingsChanged(Double value) {
				updateFocale();
				
			}
		});;
		
		FWAction xyRotation = new FWAction(XY_ROTATION, "xy-rotation.png", new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				rotationType = ROTATION.xy;
			}
		});
		
		FWAction xzRotation = new FWAction(XZ_ROTATION, "xz-rotation.png", new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				rotationType = ROTATION.xz;
			}
		});
		
		FWAction yzRotation = new FWAction(YZ_ROTATION, "yz-rotation.png", new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				rotationType = ROTATION.yz;
			}
		});
		
		FWAction xyzView = new FWAction(XYZ_VIEW, "xyz-axis.png", new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				rotation = new QRotation4D();
				resetRenderersSpaceTransform();
			}
		});

		FWAction yztView = new FWAction(YZT_VIEW, "yzt-axis.png", new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				rotation = QRotation4D.getYZT();
				resetRenderersSpaceTransform();
			}
		});

		FWAction ztxView = new FWAction(ZTX_VIEW, "ztx-axis.png", new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				rotation = QRotation4D.getZTX();
				resetRenderersSpaceTransform();
			}
		});

		FWAction txyView = new FWAction(TXY_VIEW, "txy-axis.png", new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				rotation = QRotation4D.getTXY();
				resetRenderersSpaceTransform();
			}
		});

		actionManager.addAction(xyRotation);
		actionManager.addAction(xzRotation);
		actionManager.addAction(yzRotation);
		xzRotation.putValue(Action.SELECTED_KEY, true);

		actionManager.addAction(xyzView);
		actionManager.addAction(yztView);
		actionManager.addAction(ztxView);
		actionManager.addAction(txyView);

		
		try {
			XMLReader e = new XMLFile(getClass().getResource("/cfg/header.xml")).parse();
			toolbar = new G4DToolBar(e.popChild(this), actionManager);
			toolbar.setFloatable(true);
			toolbar.setName(TOOLBAR.translate());
		} catch (IOException | XMLException | HTTPException ex) {
			ex.printStackTrace();
		}
		
		conicProjection.addParamaterListener(new FWParameterListener<Boolean>() {
			@Override
			public void settingsChanged(Boolean value) {
				fov.setEnabled(value);
			}
		});
		
	}
	
	private void resetRenderersSpaceTransform() {
		for (RendererI<GTPoint> r : registeredRenderers) {
			r.setSpaceTransform(new QRotation());
			r.getPane().repaint();
		}
	}

	private void updateFocale() {
		f = 1000 / (2*Math.tan(fov.getValue() /2));
	}
	
	private Euclidean4DGeometry<GTPoint> delegate = new Euclidean4DGeometry<GTPoint>(){
		@Override
		public String getXMLTag() {
			return "GTEuclidean4DGeometry";
		}
		
		@Override
		public Point3D get3DCoordinates(GTPoint p) {
			Point4D q = rotation.apply(new Point4D(p.getU1(), p.getU2(), p.getU3(), p.getU4()));
			double x = q.x;
			double y = q.y;
			double z = q.z;
			if (conicProjection.getValue()) {
				double lambda = f /(f - q.t);
				x *= lambda;
				y *= lambda;
				z *= lambda;
			}
			return new Point3D(x, y, z);
		}
	};
	
	@Override
	protected GeometryI<GTPoint> getDelegateGeometry() {
		return delegate;
	}
	
	@Override
	public GTTransport getGTTransport(GTPoint p, TangentVector v, double d) throws GeometryException {
		return new GTTransport(p, p.getTranslated(v.getPoint4D().getScaled(d)));
	}

	@Override
	public GTRotation getOrientation(Turtle t1, Turtle t2) {
		GTPoint p = t1.getPosition();
		GTPoint q = t2.getPosition();
		try {
			Point4D u = new Point4D(
					q.getU1() - p.getU1(), 
					q.getU2() - p.getU2(),
					q.getU3() - p.getU3(),
					q.getU4() - p.getU4()).getNormalized();
			
			if (u.y==1)
				return new GTRotation();
			
			Quaternion quat = new Quaternion(u.t, u.x, u.y-1, u.z);
			QRotation r = new QRotation(quat);
			return new GTRotation(r, r);
		} catch (ZeroVectorException ex) {
			ex.printStackTrace();
		}
		return new GTRotation();
	}
	
	public void fill(GTPolygon poly, GTRendererI r) {
		GTVerticesPolygon vPoly = (GTVerticesPolygon) poly;
		FVMesh mesh = vPoly.getMesh(this, r);
		r.draw(mesh);
		mesh.reverseOrientation();
		r.draw(mesh);
	}
	
	@Override
	public GTPolygon createPolygon(GTPoint position, TurtlePen pen) {
		return new GTVerticesPolygon(position, pen);
	}
	
	public void init(final RendererI<GTPoint> r) {
		super.init(r);
		rotation = new QRotation4D();
		addSpecialListener(r);
		r.getPane().add(toolbar);
	}
	
	private HashSet<RendererI<GTPoint>> registeredRenderers = new HashSet<RendererI<GTPoint>>();
	private QRotation4D rotation = new QRotation4D();
	
	private void addSpecialListener(final RendererI<GTPoint> renderer) {
		if (registeredRenderers.contains(renderer))
			return;
		FWMouseListener hRotationListener = new FWMouseListener() {
			private QRotation4D rotation0;

			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if (getMouseModifiers() == RIGHT_CTRL_SHIFT) {
					rotation0 = rotation;
					renderer.getPane().setCursor(FWToolKit.getCursor("turnZ.gif"));
				}
			}

			@Override
			public void mouseDragged(int x, int y, int mode) {
				if (mode == RIGHT_CTRL_SHIFT) {
					QRotation4D r = new QRotation4D();
					switch (rotationType) {
					case xy:
						r = QRotation4D.getXY(x / 200.);
						break;
					case xz:
						r = QRotation4D.getXZ(x / 200.);
						break;
					case yz:
						r = QRotation4D.getYZ(x / 200.);
						break;
					default:
						break;
					}
					rotation = r.apply(rotation0);
					renderer.getPane().repaint();
				}
			}
		};
		
		JPanel pane = renderer.getPane();
		pane.addMouseListener(hRotationListener);
		pane.addMouseMotionListener(hRotationListener);
		registeredRenderers.add(renderer);
	}
	
	public final void update(MouseManager m) {
		m.setAllAbilitiesAvailable(true);
	}

	public QRotation4D getRotation4D() {
		return rotation;
	}
	
	@Override
	public RENDERER_TYPE getRendererType() {
		return RENDERER_TYPE.FW4D;
	}
	
	private class G4DToolBar extends FWToolBar {
		private static final long serialVersionUID = 3812608398049848668L;

		public G4DToolBar(XMLReader e, FWActionManager m) {
			super(e, m);
		}
	}

	public void do4DRotation(String axe, double angle) throws IllegalArgumentException {
		ROTATION key = ROTATION.valueOf(ROTATION.class, axe);
		switch (key) {
			case xt:
				rotation = QRotation4D.getXT(angle).apply(rotation);
				break;
			case yt:
				rotation = QRotation4D.getYT(angle).apply(rotation);
				break;
			case zt:
				rotation = QRotation4D.getZT(angle).apply(rotation);
				break;
			case xy:
				rotation = QRotation4D.getXY(angle).apply(rotation);
				break;
			case xz:
				rotation = QRotation4D.getXZ(angle).apply(rotation);
				break;
			case yz:
				rotation = QRotation4D.getYZ(angle).apply(rotation);
				break;
			default:
				throw new IllegalArgumentException(axe);
		}
	}
	
	/*
	 * XML
	 */
	
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
		JCheckBox conicProjectionCB = conicProjection.getComponent(new FWParameterListener<Boolean>() {
			@Override
			public void settingsChanged(Boolean value) {
				actions.fire(FWSettingsActionPuller.REPAINT);
			}
		});
		JSpinner fovSpinner = fov.getComponent(new FWParameterListener<Double>() {
			@Override
			public void settingsChanged(Double value) {
				actions.fire(FWSettingsActionPuller.REPAINT);
			}
		});
		return VerticalPairingLayout.createPanel(10, 10,
				new FWLabel(CONIC_PROJECTION), conicProjectionCB,
				new FWLabel(FOV), fovSpinner);
	}

	/*
	 * JEP
	 */
	

	@Override
	public void addFunctions(GTEnhancedJEP jep) {
		super.addFunctions(jep);
		jep.addGFunction(X1_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return ps[0].getU1();
			}
		});
		
		jep.addGFunction(X2_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return ps[0].getU2();
			}
		});
		
		jep.addGFunction(X3_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return ps[0].getU3();
			}
		});
		
		jep.addGFunction(X4_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return ps[0].getU4();
			}
		});
		
//		jep.addGFunction(new JKey(GTEuclidean4DGeometry.class, "X"), 1, new GFunctionI() {
//			@Override
//			public double getValue(GTPoint... ps) {
//				GTPoint p = ps[0];
//				Point4D q = rotation.apply(new Point4D(p.getU1(), p.getU2(), p.getU3(), p.getU4()));
//				return q.x;
//			}
//		});
//		
//		jep.addGFunction(new JKey(GTEuclidean4DGeometry.class, "Y"), 1, new GFunctionI() {
//			@Override
//			public double getValue(GTPoint... ps) {
//				GTPoint p = ps[0];
//				Point4D q = rotation.apply(new Point4D(p.getU1(), p.getU2(), p.getU3(), p.getU4()));
//				return q.y;
//			}
//		});
//		
//		jep.addGFunction(new JKey(GTEuclidean4DGeometry.class, "Z"), 1, new GFunctionI() {
//			@Override
//			public double getValue(GTPoint... ps) {
//				GTPoint p = ps[0];
//				Point4D q = rotation.apply(new Point4D(p.getU1(), p.getU2(), p.getU3(), p.getU4()));
//				return q.z;
//			}
//		});
	}
}