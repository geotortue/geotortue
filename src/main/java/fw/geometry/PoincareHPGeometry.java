package fw.geometry;

import java.awt.Color;

import javax.swing.JPanel;

import fw.app.Translator.TKey;
import fw.geometry.obj.GDot;
import fw.geometry.obj.GPoint;
import fw.geometry.obj.GSegment;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;
import fw.gui.FWSettingsActionPuller;
import fw.renderer.core.RendererI;
import fw.renderer.mesh.FVMesh;
import fw.renderer.mesh.Polyhedron;
import fw.renderer.mesh.Vertex;
import fw.renderer.shader.ConstantShader;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class PoincareHPGeometry<T extends GPoint> extends HyperbolicGeometry<T> {

	private static final TKey NAME = new TKey(PoincareHPGeometry.class, "name");

	@Override
	public TKey getTitle() {
		return NAME;
	}
	


	@Override
	public Point3D get3DCoordinates(T gp) {
		Point3D p = super.get3DCoordinates(gp);
		return new Point3D(p.y/(p.x + p.z), 1/(p.x + p.z), 0);
	}
	
	public void draw(GSegment<T> s, RendererI<T> r){
		Point3D p = get3DCoordinates(s.getStart());
		Point3D q = get3DCoordinates(s.getEnd());
		
		if (Math.abs((p.x-q.x))*r.getUnit() < 1) {
			r.drawLine(p, q, s.getColor(), s.getThickness());
			return;
		}
		
		final double omega = (p.x + q.x + (p.y-q.y)*(p.y+q.y)/(p.x-q.x))/2;
		final double R = MathUtils.abs(p.x-omega, p.y);
		
		double tp = Math.atan2(p.y, p.x-omega);
		double tq = Math.atan2(q.y, q.x-omega);
		r.drawArc2D(new Point3D(omega, 0, 0), R, tp, tq, s.getColor(), s.getThickness());
	}
	
	@Override
	public void draw(GDot<T> d, RendererI<T> r){
		Point3D p = get3DCoordinates(d.getPosition());
		r.drawPixel(p, d.getColor(), d.getThickness());
	}
	
	public void init(RendererI<T> r) {
		r.setBackground(Color.WHITE);
		r.reset();
		Point3D p = r.liftTo3DSpace(new Pixel(0, r.getHeight()));
		r.setOrigin(new Point3D(0, p.y*r.getUnit(), 0));
		r.setUnit(r.getHeight()/2);
	}
	
	public void paintBackground(RendererI<T> r) {
		Point3D q00 = r.liftTo3DSpace(new Pixel(0, r.getHeight()));
		Point3D q10 = r.liftTo3DSpace(new Pixel(r.getWidth(), r.getHeight()));
		
		double x0 = q00.x;
		double x1 = q10.x;
		
		Vertex p00 = new Vertex(x0, 0, 0);
		Vertex p10 = new Vertex(x1, 0, 0);
		
		Polyhedron poly = new Polyhedron(q00, p00, p10, q10);
		//poly.addFace(0, 1, 2, 3);
		
		FVMesh mesh = new FVMesh(poly, Color.DARK_GRAY, new ConstantShader());
		
		r.draw(mesh);
	}
	
	
	@Override
	public void paintForeground(RendererI<T> r) {
		r.drawLineFog(new Point3D(0, 0, 0), 12, Color.DARK_GRAY);
	}

	@Override
	public int getDimensionCount() {
		return 2;
	}


	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return "PoincareHPGeometry";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		return new XMLWriter(this);
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		return e.popChild(this);
	}
	
	/*
	 * FWS
	 */
	
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		return new JPanel();
	}

}