package fw.geometry;

import fw.app.Translator.TKey;
import fw.geometry.obj.GPoint3D;
import fw.geometry.util.Point3D;


public class Euclidean3DGeometry<T extends GPoint3D> extends Euclidean2DGeometry<T> {

	private static final TKey NAME = new TKey(Euclidean3DGeometry.class, "name");

	@Override
	public TKey getTitle() {
		return NAME;
	}
	
	@Override
	public Point3D get3DCoordinates(T p) {
		return new Point3D(p.getU1(), p.getU2(), p.getU3());
	}
	
	
	@Override
	public double distance(T p, T q) {
		double dx = q.getU1()- p.getU1();
		double dy = q.getU2()- p.getU2();
		double dz = q.getU3()- p.getU3();
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
	
	@Override
	public int getDimensionCount() {
		return 3;
	}
	
	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return "Euclidean3DGeometry";
	}
}