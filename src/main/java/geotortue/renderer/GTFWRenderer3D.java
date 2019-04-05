package geotortue.renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;

import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.renderer.core.RenderJob;
import fw.renderer.core.RendererSettingsI;
import fw.renderer.fwre.FWRenderer3D;
import fw.renderer.mesh.Mesh;
import geotortue.core.Turtle;
import geotortue.core.TurtleAvatar3D;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.geometry.obj.GTPath;


public class GTFWRenderer3D extends FWRenderer3D<GTPoint> implements GTRendererI {

	private final GTLightingContext lightingContext;

	public GTFWRenderer3D(RendererSettingsI s, RenderJob<GTPoint> r, GTLightingContext gtlc){
		super(s, r, gtlc);
		this.lightingContext = gtlc;
	}
	
	
	@Override
	public GTLightingContext getLightingContext() {
		return lightingContext;
	}

	public void reset() {
		super.reset();
		lightingContext.reset();
	}
	
	@Override
	public void updateSettings() { // no blur
		super.updateSettings();
		float x = ((GTRendererSettings) settings).getBlurAmount();
		setBlurAmount(x);
	}


	@Override
	public void draw(Turtle t, GTGeometryI g) {
		TurtleAvatar3D avatar = t.getAvatar3D();
		Point3D p = g.get3DCoordinates(t.getPosition());
		
		QRotation r = g.getOrientationAt(t.getPosition()).apply(t.getRotation());
		for (Mesh m : avatar.getMeshes(p, r, 1/getUnit()))
			draw(m);			
	}
	
	public void drawTick(Point3D p, double size) {
		double u = size/getUnit();
		Point3D p00 = new Point3D(p.x-u, p.y-u, p.z);
		Point3D p01 = new Point3D(p.x-u, p.y+u, p.z);
		Point3D p10 = new Point3D(p.x+u, p.y-u, p.z);
		Point3D p11 = new Point3D(p.x+u, p.y+u, p.z);
		Point3D z1 = new Point3D(p.x, p.y, p.z-u);
		Point3D z2 = new Point3D(p.x, p.y, p.z+u);
		
		drawLine(p00, p11, Color.RED, 1);
		drawLine(p10, p01, Color.RED, 1);
		drawLine(z1, z2, Color.RED, 1);
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
		Point3D pz = r.apply(new Point3D(0, 0, u)).getTranslated(p);
		drawLine(p, px, Color.RED, 1);
		drawLine(p, py, Color.BLUE, 1);
		drawLine(p, pz, Color.GREEN, 1);
	}


	public void setSpaceTransform(QRotation r) {
		super.setSpaceTransform(r);
		lightingContext.setOrientation(r);
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
	public void drawCircle(Point3D p, double r, Color c, int thickness) {
	}
	
	@Override
	public void fill(GTPath shape, Color c) {
	}
	
}