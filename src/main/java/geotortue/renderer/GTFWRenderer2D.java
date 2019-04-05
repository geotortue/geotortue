package geotortue.renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

import fw.geometry.proj.PerspectiveI.InvisibleZPointException;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.renderer.core.RenderJob;
import fw.renderer.core.RendererSettingsI;
import fw.renderer.fwre.FWRenderer2D;
import geotortue.core.Turtle;
import geotortue.core.TurtleAvatar2D;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.geometry.obj.GTPath;
import geotortue.geometry.obj.GTPath.ArcElement;
import geotortue.geometry.obj.GTPath.PathElement;
import geotortue.geometry.obj.GTPath.SegmentElement;


public class GTFWRenderer2D extends FWRenderer2D<GTPoint> implements GTRendererI {

	public GTFWRenderer2D(RendererSettingsI s, RenderJob<GTPoint> r){
		super(s, r);
	}
	
	@Override
	public void setSize(Dimension d){
		super.setSize(d);
		Graphics2D g = getGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	}

	@Override
	public void drawTick(Point3D p, double size) {
		double u = size/getUnit();
		Point3D p00 = new Point3D(p.x-u, p.y-u, p.z);
		Point3D p01 = new Point3D(p.x-u, p.y+u, p.z);
		Point3D p10 = new Point3D(p.x+u, p.y-u, p.z);
		Point3D p11 = new Point3D(p.x+u, p.y+u, p.z);
		drawLine(p00, p11, Color.RED, 1);
		drawLine(p10, p01, Color.RED, 1);
	}
	
	@Override
	public void drawAxis(GTGeometryI g, Turtle t) {
		Point3D p = g.get3DCoordinates(t.getPosition());
		QRotation r0 = g.getOrientationAt(t.getPosition());
		QRotation r = r0.apply(t.getRotation());
		double u = 100 / getUnit();
		double uc = (t.getOrientation() > 0) ? u : -u;
		Point3D px = r.apply(new Point3D(uc, 0, 0)).getTranslated(p);
		Point3D py = r.apply(new Point3D(0, u, 0)).getTranslated(p);
		drawLine(p, px, Color.RED, 1);
		drawLine(p, py, Color.BLUE, 1);
	}


	@Override
	public void draw(Turtle t, GTGeometryI gc) {
		QRotation r = gc.getOrientationAt(t.getPosition());
		r = r.apply(t.getRotation());
		r = spaceTransform.apply(r);
		double angle = r.getXYAngle() - Math.PI /2;
		TurtleAvatar2D avatar = t.getAvatar2D();
		Pixel hotSpot = avatar.getHotSpot();
		BufferedImage img = avatar.getImg();

		Graphics2D g = getGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		if (Double.isNaN(angle) || Double.isInfinite(angle)) {
			setErrorMessage("display error // angle : "+r);
			new Exception().printStackTrace();
		}
		
		Point3D p = gc.get3DCoordinates(t.getPosition());
		
		try {
			Pixel pixel = toScreen(p);
			g.translate(pixel.i - hotSpot.i, pixel.j - hotSpot.j);
			g.rotate(-angle, hotSpot.i, hotSpot.j);
			g.drawImage(img, 0, 0, null);
			g.rotate(angle, hotSpot.i, hotSpot.j);
			g.translate(hotSpot.i - pixel.i, hotSpot.j - pixel.j);
		} catch (InvisibleZPointException e) {
			return;
		}
	}
	
	public BufferedImage  getHDImage(double f){
		int w = offscreenImage.getWidth();
		int h = offscreenImage.getHeight();
		
		zoom(f, new Point(w/2, h/2));
		setSize(new Dimension((int) (f*w), (int) (f*h)));
		BufferedImage im = getImage();
		setSize(new Dimension(w, h));
		zoom(1/f, new Point(w/2, h/2));
		return im;
	}
	
	@Override
	public void fill(GTPath path, Color c) {
		GeneralPath shape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		try {
			Pixel p = toScreen(path.getStartingPoint());
			shape.moveTo(p.i, p.j);
		} catch (InvisibleZPointException e) {
		}
		
		for (PathElement element : path.getElements()) {
			switch (element.getId()) {
			case segment:
				SegmentElement s = (SegmentElement) element;
				try {
					Pixel p = toScreen(s.getEndPoint());
					shape.lineTo(p.i, p.j);
				} catch (InvisibleZPointException e) {
				}
				break;
			case arc:
				ArcElement a = (ArcElement) element;
				double r = a.getRadius();
				double start = a.getStartAngle();
				double end = a.getEndAngle();
				Point3D center = a.getCenter();
				try {
					Arc2D.Double arc = getArc2D(center, r, start, end);
					shape.append(arc, true);
				} catch (InvisibleZPointException e) {
				}
				break;
			default:
				break;
			}
		}
		shape.closePath();
		
		Graphics2D g = getGraphics(c, path.getThickness()); 
		g.fill(shape);
		g.draw(shape);
	}
}