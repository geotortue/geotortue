package fw.geometry;

import fw.app.Translator.TKey;
import fw.geometry.obj.GPoint4D;


public class Euclidean4DGeometry<T extends GPoint4D> extends Euclidean3DGeometry<T> {

	private static final TKey NAME = new TKey(Euclidean4DGeometry.class, "name");

	@Override
	public TKey getTitle() {
		return NAME;
	}

	
	@Override
	public double distance(T p, T q) {
		double dx = q.getU1()- p.getU1();
		double dy = q.getU2()- p.getU2();
		double dz = q.getU3()- p.getU3();
		double dt = q.getU4()- p.getU4();
		return Math.sqrt(dx*dx + dy*dy + dz*dz + dt*dt);
	}
	
	@Override
	public int getDimensionCount() {
		return 4;
	}

	
	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return "Euclidean4DGeometry";
	}
}