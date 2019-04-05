package fw.renderer.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import fw.geometry.obj.GPoint;
import fw.geometry.proj.PerspectiveI;
import fw.geometry.proj.PerspectiveI.InvisibleZPointException;
import fw.geometry.proj.ZPoint;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.renderer.mesh.CurvePlotter;
import fw.renderer.mesh.Face;
import fw.renderer.mesh.Mesh;
import fw.renderer.shader.FaceShader;



public interface RendererI<T extends GPoint> {

	/*
	 * 
	 */

	public JPanel getPane();
	
	public BufferedImage getImage();
	
	/*
	 * 
	 */
	
	public void setPerspective(PerspectiveI p);
	
	public PerspectiveI getPerspective();
	
	public Point3D liftTo3DSpace(Pixel p);
	
	public boolean contains(Pixel p);
	
	/**
	 * Check if the triangle p0 p1 p2 intersects graphicSpace.
	 */
	public boolean intersects(Pixel p0, Pixel p1, Pixel p2);
	
	/*
	 * 
	 */
	
	public Pixel toScreen(Point3D p) throws InvisibleZPointException;
	
	public void drawLine(Point3D p, Point3D q, Color c, int thickness);
	
	public void drawPixel(Point3D p, Color color, int thickness);
	
	public void drawCircle(Point3D p, double radius, Color color, int thickness);
	
	public void drawArc2D(Point3D center, double r, double start, double end, Color c, int thickness);

	public void draw(CurvePlotter<T> curve, Color color, int thickness);
	
	public void draw(Face f, FaceShader fShader);
	
	public void draw(Mesh mesh);
	
	public void fillCircle(Point3D p, double r, Color c);
	
	public void drawCircleFog(Point3D p, double r, Color c);
	
	public void drawLineFog(Point3D p, int w, Color c);
		
	public void drawImage(BufferedImage img, Point3D p);
	
	public void drawString(Point3D p, String str, Font font, Color color);
	
	
	/*
	 * 
	 */
	
	public Dimension getSize();
	
	public int getWidth();

	public int getHeight();
	
	public void setSize(Dimension d);

	/*
	 * 
	 */
	
	public Point3D getOrigin();
	
	public void setOrigin(Point3D p);
	
	public double getUnit();
	
	public void setUnit(double u);
	
	public void zoom(double f, Point p);
	
	public QRotation getSpaceTransform();

	public void setSpaceTransform(QRotation r);
	
	public ZPoint toZSpace(Point3D p) throws InvisibleZPointException;
	
	/*
	 * 
	 */

	public void reset();
	
	public void updateSettings();
	
	public void setBackground(Color c);
	
	/*
	 * 
	 */
	public void setErrorMessage(String msg);
	
	public void drawAxis(Color c, Font font);
	
	public void drawGrid(Color c);
	

}