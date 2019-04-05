package fw.geometry.proj;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import fw.geometry.util.MathException;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;

public interface PerspectiveI {
	
	public ZPoint toZSpace(Point3D p) throws InvisibleZPointException;
	
	public Pixel toScreen(ZPoint p) throws MathException;
	
	public Point2D.Double toScreen2D(ZPoint p);

	public Point3D liftTo3DSpace(Pixel p);
	
	public void setScreenSize(Dimension d);
	
	public double getMaximumZDepth();
	
	public static class InvisibleZPointException extends Exception {
		private static final long serialVersionUID = 8064157745774514558L;

		public InvisibleZPointException(PerspectiveI p, double z) {
			super(z+" > " +p.getMaximumZDepth());
		}
	}
}