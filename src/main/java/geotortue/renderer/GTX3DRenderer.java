package geotortue.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;

import fw.geometry.util.Point3D;
import fw.renderer.core.RenderJob;
import fw.renderer.core.RendererI;
import fw.svg.X3DRenderer;
import geotortue.core.Turtle;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.geometry.obj.GTPath;


public class GTX3DRenderer extends X3DRenderer<GTPoint> implements GTRendererI {

	public GTX3DRenderer(RendererI<GTPoint> r, RenderJob<GTPoint> job) {
		super(r, job);
	}

	@Override
	public void draw(Turtle t, GTGeometryI g) {
	}

	@Override
	public void drawTick(Point3D p, double size) {
	}

	@Override
	public BufferedImage getHDImage(double f) {
		return null;
	}

	@Override
	public void drawAxis(GTGeometryI g, Turtle t) {
	}
	
	@Override
	public void fill(GTPath shape, Color c) {
	}
	
}
