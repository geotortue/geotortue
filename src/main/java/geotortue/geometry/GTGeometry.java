package geotortue.geometry;

import javax.swing.JPanel;

import fw.app.Translator.TKey;
import fw.geometry.GeometryI;
import fw.geometry.obj.GDot;
import fw.geometry.obj.GSegment;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.gui.FWSettingsActionPuller;
import fw.renderer.MouseManager;
import fw.renderer.core.RendererI;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.core.GTEnhancedJEP;
import geotortue.core.GTJEPFunctionFactory.GFunctionI;
import geotortue.core.Turtle;
import geotortue.geometry.obj.GTArc;
import geotortue.geometry.obj.GTCircle;
import geotortue.geometry.obj.GTPolygon.NonFlatPolygonException;
import geotortue.geometry.obj.GTString;
import geotortue.renderer.GTRendererI;
import jep2.JKey;


public abstract class GTGeometry implements GTGeometryI {
	
	private static final JKey DIST = new JKey(GTGeometry.class, "dist");
	private static final JKey DISTANCE = new JKey(GTGeometry.class, "distance");

	protected abstract GeometryI<GTPoint> getDelegateGeometry();
	
	
	public void teleport(Turtle t, GTPoint p) throws GeometryException, NonFlatPolygonException {
		t.setPosition(p);
		resetToNorthOrientation(t);
	}
	
	@Override
	public void resetToNorthOrientation(Turtle t) {
		t.setRotation(new GTRotation());
	}
	
	@Override
	public void setParallelOrientation(Turtle ref, Turtle t) throws GeometryException {
		t.setRotation(ref.getRotation4D());
	}

	@Override
	public void update(MouseManager m) {
		m.setAllAbilitiesAvailable(true);
		m.xRotationAbility.setAvailable(false);
		m.yRotationAbility.setAvailable(false);
	}
	
	
	private final static QRotation identity = new QRotation();
	
	public QRotation getOrientationAt(GTPoint gp) {
		return identity;
	}
	
	public void centerWorldOn(GTPoint gp, GTRendererI r) {
		Point3D p = get3DCoordinates(gp);
		r.setSpaceTransform(new QRotation());
		double u = r.getUnit();
		r.setOrigin(new Point3D(-p.x * u, -p.y * u, -p.z * u));
	}
	
	public void draw(GTCircle circle, GTRendererI r) {
	}
	
	public void fill(GTCircle circle, GTRendererI r) {
	}
	
	public void draw(GTArc arc, GTRendererI r) {
	}
	
	public void draw(GTString str, GTRendererI r) {
		Point3D p = get3DCoordinates(str.getPosition());
		r.drawString(p, str.getText(), str.getFont(), str.getColor());
	}
	
	
	/*
	 * DELEGATION
	 */
	
	@Override
	public final Point3D get3DCoordinates(GTPoint p) {
		return getDelegateGeometry().get3DCoordinates(p);
	}
	
	@Override
	public void draw(GSegment<GTPoint> s, RendererI<GTPoint> r) {
		getDelegateGeometry().draw(s, r);
	}
	
	@Override
	public void draw(GDot<GTPoint> d, RendererI<GTPoint> r) {
		getDelegateGeometry().draw(d, r);
	}
	
	public double distance(GTPoint p, GTPoint q) {
		return getDelegateGeometry().distance(p, q);
	}
	
	@Override
	public void init(RendererI<GTPoint> r) {
		getDelegateGeometry().init(r);
	}
	
	@Override
	public void paintBackground(RendererI<GTPoint> rc) {
		getDelegateGeometry().paintBackground(rc);
	}
	
	@Override
	public void paintForeground(RendererI<GTPoint> rc) {
		getDelegateGeometry().paintForeground(rc);
	}
	
	@Override
	public int getDimensionCount() {
		return getDelegateGeometry().getDimensionCount();
	}
	
	public FrameSupport getFrameSupport() {
		return null;
	}
	

	/*
	 * XML
	 */
	


	@Override
	public String getXMLTag() {
		return getDelegateGeometry().getXMLTag();
	}

	
	@Override
	public XMLWriter getXMLProperties() {
		return getDelegateGeometry().getXMLProperties();
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		return getDelegateGeometry().loadXMLProperties(e);
	}
	
	/*
	 * FWS
	 */
	public JPanel getSettingsPane(FWSettingsActionPuller actions) {
		return getDelegateGeometry().getSettingsPane(actions);
	}

	/*
	 * 
	 */
	
	public String toString() {
		return getTitle().translate();
	}
	
	public TKey getTitle() {
		return getDelegateGeometry().getTitle();
	}
	
	/*
	 * GTJEP
	 */
	
	public void addFunctions(GTEnhancedJEP jep) {
		GFunctionI f = new GFunctionI() {
			@Override
			public double getValue(GTPoint... ps) {
				return distance(ps[0], ps[1]);
			}
		};
		jep.addGFunction(DIST, 2, f); // backward compatibility
		jep.addGFunction(DISTANCE, 2, f);
	}
}