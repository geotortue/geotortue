package fw.geometry;

import java.awt.Color;

import javax.swing.JPanel;

import fw.app.Translator.TKey;
import fw.geometry.obj.GDot;
import fw.geometry.obj.GPoint;
import fw.geometry.obj.GSegment;
import fw.geometry.util.MathException.ZeroVectorException;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Point3D;
import fw.gui.FWSettingsActionPuller;
import fw.renderer.core.RendererI;
import fw.renderer.mesh.CurvePlotter;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class SphericalGeometry<T extends GPoint> implements GeometryI<T> {

	private static final TKey NAME = new TKey(SphericalGeometry.class, "name");

	@Override
	public TKey getTitle() {
		return NAME;
	}
	


	public Point3D get3DCoordinates(T p) {
		double theta = p.getU1(); 
		double phi = p.getU2();
		double cosPhi  = Math.cos(phi);
		double sinPhi  = Math.sin(phi);
		double cosTheta = Math.cos(theta);
		double sinTheta = Math.sin(theta);
		return new Point3D(cosPhi * sinTheta, sinPhi, cosPhi * cosTheta);
	}
	
	@Override
	public double distance(T p, T q) {
		Point3D p1 = get3DCoordinates(p);
		Point3D q1 = get3DCoordinates(q);
		return acos(p1.x*q1.x+p1.y*q1.y+p1.z*q1.z);
	}
	
	private final static double cosBound = 1 + 1E-15; 
	private static double acos(double d) {
		if (1<d && d<cosBound)
			return 0;
		if (-cosBound<d && d<-1)
			return Math.PI;
		return Math.acos(d);
	}

	@Override
	public void draw(GSegment<T> s, RendererI<T> r) {
		Point3D q0 = get3DCoordinates(s.getStart());
		Point3D q1 = get3DCoordinates(s.getEnd());

		Point3D n = MathUtils.crossProduct(q0, q1);
		try {
			n = MathUtils.getNormalized(n);
		} catch (ZeroVectorException ex) {
			// q0=q1
		}
		
		final double nx = n.x;
		final double ny = n.y;
		final double nz = n.z;
		final double nxy = MathUtils.abs(nx, ny);
		final double R1 = 1/nxy;
		final double R2 = nxy;
		
		Point3D u = new Point3D(R1*ny, -R1*nx, 0);
		
		double t0 = Math.atan2(MathUtils.dotProduct(MathUtils.crossProduct(u, q0), n), MathUtils.dotProduct(u, q0));
		double t1 = Math.atan2(MathUtils.dotProduct(MathUtils.crossProduct(u, q1), n), MathUtils.dotProduct(u, q1));
		double dt = Math.abs(t1-t0);
		if (dt>Math.PI)
			t1 += 2*Math.PI;

		int nDiv = (int) (dt/Math.PI*r.getUnit());
		nDiv = (nDiv>32)? 32 : nDiv;

		r.draw(new CurvePlotter<T>(t0, t1, nDiv){
			@Override
			public Point3D getPoint(double t) {
				double cost = Math.cos(t);
				double sint = Math.sin(t);
				return new Point3D(
						 R1*( ny*cost + nx*nz*sint), 
						 R1*(-nx*cost + ny*nz*sint),
						-R2*sint);
			}
		}, s.getColor(), s.getThickness());
	}
	
	@Override
	public void draw(GDot<T> d, RendererI<T> r){
		Point3D p = get3DCoordinates(d.getPosition());
		r.drawPixel(p, d.getColor(), d.getThickness());
	}
	
	public void init(RendererI<T> r) {
		r.setBackground(Color.WHITE);
		r.reset();
		r.setUnit(Math.min(r.getWidth(), r.getHeight())/2.1);
	}
	
	public void paintBackground(RendererI<T> r) {
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
		return "SphericalGeometry";
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


	@Override
	public void paintForeground(RendererI<T> r) {
	}
}