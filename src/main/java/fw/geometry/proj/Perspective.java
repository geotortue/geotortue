package fw.geometry.proj;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import fw.geometry.util.MathException;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;

public abstract class Perspective implements PerspectiveI {
	
	private Dimension screenSize = new Dimension(640, 480);
	
	public abstract ZPoint toZSpace(Point3D p) throws InvisibleZPointException;
	
	@Override
	public double getMaximumZDepth() {
		return Double.MAX_VALUE;
	}

	public final Pixel toScreen(ZPoint p) throws MathException {
		Point2D.Double p2d = toScreen2D(p);
		return getClosestPixel(p2d.x, p2d.y);
	}
	
	public final Point2D.Double toScreen2D(ZPoint p) {
		return new Point2D.Double(screenSize.width/2. + p.x, screenSize.height/2. - p.y);
	}
	
	private static Pixel getClosestPixel(double x, double y) throws MathException {
		return new Pixel(MathUtils.round(x), MathUtils.round(y));
	}
	
	public final Point3D liftTo3DSpace(Pixel p) {
		return liftTo3DSpace(liftToZSpace(p));
	}
	
	public abstract Point3D liftTo3DSpace(ZPoint p);

	public final ZPoint liftToZSpace(Pixel p){
		int ex =   p.i - screenSize.width/2;
		int ey = - p.j + screenSize.height/2;
		return new ZPoint(ex, ey, 0);
	}

	public void setScreenSize(Dimension d) {
		this.screenSize = d;
	}
	
	protected Dimension getScreenSize() {
		return screenSize;
	}
}

