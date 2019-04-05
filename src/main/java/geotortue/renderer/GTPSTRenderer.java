package geotortue.renderer;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import fw.geometry.proj.PerspectiveI.InvisibleZPointException;
import fw.geometry.util.Point3D;
import fw.renderer.core.RenderJob;
import fw.renderer.core.RendererI;
import fw.svg.PSTRenderer;
import geotortue.core.Turtle;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.geometry.obj.GTPath;


public class GTPSTRenderer extends PSTRenderer<GTPoint> implements GTRendererI {

	public GTPSTRenderer(RendererI<GTPoint> r, RenderJob<GTPoint> job) {
		super(r, job);
	}

	@Override
	public void draw(Turtle t, GTGeometryI g) {
	}

	@Override
	public void drawAxis(GTGeometryI g, Turtle t) {
	}

	@Override
	public void drawTick(Point3D p, double size) {
	}

	@Override
	public BufferedImage getHDImage(double f) {
		return null;
	}
	
	public void drawCircle(Point3D p, double radius, Color c, int thickness) {
		try {
			final Point2D.Double sp = toPSTScreen(p);
			String col = getCode(c);
			appendCode("\\pscircle[linecolor="+col+"]"+getCode(sp)+getCode(radius)+"\n");
		} catch (InvisibleZPointException ex) {
			ex.printStackTrace();
		}
	}
	
	private String getCode(double x){
		return "{"+ round(x) +"}";
	}
	
	@Override
	public void fill(GTPath shape, Color c) {
	}
	
}
