package geotortue.renderer;


import java.awt.Color;
import java.awt.image.BufferedImage;

import fw.geometry.util.Point3D;
import fw.renderer.core.RendererI;
import geotortue.core.Turtle;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.geometry.obj.GTPath;


public interface GTRendererI extends RendererI<GTPoint> {

	public void draw(Turtle t, GTGeometryI g);
	
	public void drawTick(Point3D p, double size);
	
	public BufferedImage getHDImage(double f);

	public void drawAxis(GTGeometryI g, Turtle t);
	
	public void fill(GTPath shape, Color c);

}