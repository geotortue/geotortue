package geotortue.geometry;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import fw.app.Translator.TKey;
import fw.geometry.Euclidean2DGeometry;
import fw.geometry.GeometryI;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.geometry.util.TangentVector;
import fw.gui.FWColorBox;
import fw.gui.FWLabel;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWBoolean;
import fw.gui.params.FWColor;
import fw.gui.params.FWParameterListener;
import fw.renderer.core.RendererI;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.core.GTEnhancedJEP;
import geotortue.core.GTJEPFunctionFactory.GFunctionI;
import geotortue.core.Turtle;
import geotortue.core.TurtlePen;
import geotortue.geometry.obj.GTArc;
import geotortue.geometry.obj.GTCircle;
import geotortue.geometry.obj.GTPath;
import geotortue.geometry.obj.GTPolygon;
import geotortue.geometry.obj.GTPolygon2D;
import geotortue.gui.GTActionSettingsPuller;
import geotortue.renderer.GTRendererI;
import geotortue.renderer.GTRendererManager.RENDERER_TYPE;
import jep2.JKey;




public class GTEuclidean2DGeometry extends GTGeometry implements FrameSupport {
	
	
	private static final JKey X_FUN = new JKey(GTEuclidean2DGeometry.class, "X");
	private static final JKey Y_FUN = new JKey(GTEuclidean2DGeometry.class, "Y");
	private static final TKey BACKGROUND = new TKey(GTEuclidean2DGeometry.class, "background");
	private static final TKey AXIS_ON = new TKey(GTEuclidean2DGeometry.class, "axisOn");
	private static final TKey AXIS_COLOR = new TKey(GTEuclidean2DGeometry.class, "axisColor");
	private static final TKey GRID_ON = new TKey(GTEuclidean2DGeometry.class, "gridOn");
	private static final TKey GRID_COLOR = new TKey(GTEuclidean2DGeometry.class, "gridColor");

	private final FWColor backgroundColor = new FWColor("background", Color.WHITE);
	private final FWColor axisColor = new FWColor("axisColor", Color.GRAY);
	private final FWColor gridColor = new FWColor("gridColor", Color.LIGHT_GRAY);
	
	private static class FWBoolean2 extends FWBoolean {
		public FWBoolean2(String xmlTag, boolean v) {
			super(xmlTag, v);
		}
		
		public void toggleValue() {
			setValue(!getValue());
		}
	}
	
	private final FWBoolean2 axisOn = new FWBoolean2("axis", false);
	private final FWBoolean2 gridOn = new FWBoolean2("grid", false);

	private final Font font = UIManager.getFont("FWFont.font12").deriveFont(16f).deriveFont(Font.BOLD);
	
	final private Euclidean2DGeometry<GTPoint> delegate = new Euclidean2DGeometry<GTPoint>() {
		@Override
		public String getXMLTag() {
			return "GTEuclidean2DGeometry";
		}
	};
	
	
	
	@Override
	public GTTransport getGTTransport(GTPoint p, TangentVector v, double d) throws GeometryException {
		return new GTTransport(p, p.getTranslated(v.getPoint3D().getScaled(d)));
	}
	
	@Override
	public GTRotation getOrientation(Turtle t1, Turtle t2) {
		Point3D p = get3DCoordinates(t1.getPosition());
		Point3D q = get3DCoordinates(t2.getPosition());
		double angle = Math.atan2(q.y - p.y, q.x - p.x);
		return GTRotation.getZRotation(angle - Math.PI/2);
	}

	@Override
	protected GeometryI<GTPoint> getDelegateGeometry() {
		return delegate;
	}

	@Override
	public RENDERER_TYPE getRendererType() {
		return RENDERER_TYPE.FW2D;
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
				return ps[0].getU1();
			}
		});
		
		jep.addGFunction(Y_FUN, 1, new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return ps[0].getU2();
			}
		});
	}
	
	public void draw(GTCircle c, GTRendererI r) {
		Point3D p = get3DCoordinates(c.getCenter());
		r.drawCircle(p, c.getRadius(), c.getColor(), c.getThickness());
	}
	
	public void fill(GTCircle c, GTRendererI r) {
		Point3D p = get3DCoordinates(c.getCenter());
		r.fillCircle(p, c.getRadius(), c.getColor());
	}

	
	public void draw(GTArc arc, GTRendererI r) {
		Point3D center = get3DCoordinates(arc.getCenter());
		Point3D p1 = get3DCoordinates(arc.getStartingPoint());
		Point3D q = p1.getTranslated(center.opp());
		double a0 = Math.atan2(q.y, q.x);
		double radius = q.abs();
		r.drawArc2D(center, radius, a0, a0+arc.getAngle(), arc.getColor(), arc.getThickness());
	}

	
	public void fill(GTPolygon poly, GTRendererI r) {
		GTPath path = ((GTPolygon2D) poly).getShape();
		r.fill(path, poly.getColor());
	}

	@Override
	public GTPolygon createPolygon(GTPoint position, TurtlePen pen) {
		return new GTPolygon2D(this, position, pen);
	}

	public GTPoint getArcEnd(GTPoint startingPoint, GTPoint center, double angle) {
		double cx = center.getU1();
		double cy = center.getU2();
		GTPoint p = startingPoint.getTranslated(-cx, -cy, 0);
		p = p.getTransformed(QRotation.getZRotation(angle));
		p = p.getTranslated(cx, cy, 0);
		return p;
	}
	
	
	@Override
	public void init(RendererI<GTPoint> r) {
		super.init(r);
		r.setBackground(backgroundColor.getValue());
	}

	@Override
	public void paintBackground(RendererI<GTPoint> r) { 
		super.paintBackground(r);

		if (gridOn.getValue())
			r.drawGrid(gridColor.getValue());
		
		if (axisOn.getValue()) 
			r.drawAxis(axisColor.getValue(), font);
	}
	
	public void toggleAxis() {
		axisOn.toggleValue();
	}

	
	public void toggleGrid() {
		gridOn.toggleValue();
	}
	
	public FrameSupport getFrameSupport() {
		return this;
	}

	/*
	 * XML
	 */
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = super.getXMLProperties();
		backgroundColor.storeValue(e);
		axisOn.storeValue(e);
		axisColor.storeValue(e);
		gridOn.storeValue(e);
		gridColor.storeValue(e);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = super.loadXMLProperties(e);
		backgroundColor.fetchValue(child, Color.WHITE);
		axisOn.fetchValue(child, false);
		axisColor.fetchValue(child, Color.GRAY);
		gridOn.fetchValue(child, false);
		gridColor.fetchValue(child, Color.LIGHT_GRAY);
		return child;
	}
	
	/*
	 * FWS
	 */
	
	FWColorBox axisCB;
	
	@Override
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		FWColorBox backgroundCB = backgroundColor.getComponent(new FWParameterListener<Color>() {
			@Override
			public void settingsChanged(Color value) {
				actions.fire(GTActionSettingsPuller.UPDATE_GEOMETRY);
			}
		});
		
		axisCB = axisColor.getComponent(new FWParameterListener<Color>() {
			@Override
			public void settingsChanged(Color value) {
				actions.fire(FWSettingsActionPuller.REPAINT);
			}
		});
		
		FWColorBox gridCB = gridColor.getComponent(new FWParameterListener<Color>() {
			@Override
			public void settingsChanged(Color value) {
				actions.fire(FWSettingsActionPuller.REPAINT);
			}
		});
		
		JCheckBox axisCheckBox = axisOn.getComponent(new FWParameterListener<Boolean>() {
			@Override
			public void settingsChanged(Boolean value) {
				actions.fire(FWSettingsActionPuller.REPAINT);
			}
		});
		
		JCheckBox gridCheckBox = gridOn.getComponent(new FWParameterListener<Boolean>() {
			@Override
			public void settingsChanged(Boolean value) {
				actions.fire(FWSettingsActionPuller.REPAINT);
			}
		});
		
		return VerticalPairingLayout.createPanel(10, 10, 
						new FWLabel(BACKGROUND, SwingConstants.RIGHT), backgroundCB,
						new FWLabel(AXIS_ON, SwingConstants.RIGHT), axisCheckBox,
						new FWLabel(AXIS_COLOR, SwingConstants.RIGHT), axisCB,
						new FWLabel(GRID_ON, SwingConstants.RIGHT), gridCheckBox,
						new FWLabel(GRID_COLOR, SwingConstants.RIGHT), gridCB);
	}
}