package fw.svg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JPanel;

import fw.geometry.obj.GPoint;
import fw.geometry.proj.PerspectiveI;
import fw.geometry.proj.PerspectiveI.InvisibleZPointException;
import fw.geometry.proj.ZPoint;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.renderer.core.RenderJob;
import fw.renderer.core.RendererI;
import fw.renderer.mesh.CurvePlotter;
import fw.renderer.mesh.Face;
import fw.renderer.mesh.Mesh;
import fw.renderer.mesh.Vertex;
import fw.renderer.shader.FaceShader;
import fw.xml.XMLAdapter;
import fw.xml.XMLCapabilities;
import fw.xml.XMLFile;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;


public class SVGRenderer<T extends GPoint> implements RendererI<T>, XMLCapabilities {
	
	private XMLWriter gWriter;
	
	// TODO : zzz // SVGRenderer
	
	private boolean hide = false; 
	
	private final RenderJob<T> job;
	private final RendererI<T> renderer;
	Vector<Face> faces = new Vector<Face>();
	
	public SVGRenderer(RendererI<T> r, RenderJob<T> job) {
		this.job = job;
		this.renderer = r;
	}
	
	public void export(File f) throws IOException {
		XMLWriter svgWriter = new XMLWriter(XMLTagged.Factory.create("svg"));
		svgWriter.setAttribute("width", getWidth());
		svgWriter.setAttribute("height", getHeight());
		svgWriter.setAttribute("xmlns", "http://www.w3.org/2000/svg");
		svgWriter.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
		svgWriter.put(this);
		new XMLFile(svgWriter).write(f);
	}
	
	/*
	 * 
	 */
	
	public Point2D.Double toSVGScreen(Point3D p) throws InvisibleZPointException {
		ZPoint zp = toZSpace(p);
		return getPerspective().toScreen2D(zp);
	}
	
	private String getRGB(Color c) {
		return "#"+ Integer.toHexString(c.getRGB()).substring(2);
	}
	
	@Override
	public void drawLine(final Point3D p, final Point3D q, final Color c, int thickness) {
		try {
			final Point2D.Double sp = toSVGScreen(p);
			final Point2D.Double sq = toSVGScreen(q);
			
			if (hide) {
				ZPoint zp = toZSpace(p);
				ZPoint zq = toZSpace(p);				
				if (!isVisible(zp) && !isVisible(zq))
					return;
			} 
			
			XMLCapabilities lineWriter = new XMLAdapter("line") {
				@Override
				public XMLWriter getXMLProperties() {
					XMLWriter e = new XMLWriter(this);
					e.setAttribute("x1", sp.x);
					e.setAttribute("y1", sp.y);
					e.setAttribute("x2", sq.x);
					e.setAttribute("y2", sq.y);
					e.setAttribute("stroke", getRGB(c));
					return e;
				}
			};
			gWriter.put(lineWriter);
		} catch (InvisibleZPointException e) {
			return;
		}
	}

	private boolean isVisible(ZPoint p){
		for (Face f : faces) {
			try {
				ZPoint p0 = toZSpace(f.p0);
				ZPoint p1 = toZSpace(f.p1);
				ZPoint p2 = toZSpace(f.p2);
				if (isInterior(p, p0, p1, p2))
						return false;
			} catch (InvisibleZPointException ex) {
			}
		}
		return true;
	}
	
	private boolean isInterior(ZPoint p, ZPoint a, ZPoint b, ZPoint c) throws InvisibleZPointException {
			// Compute vectors        
			Point2D.Double v0 = translate(a, c);
			Point2D.Double v1 = translate(b, c);
			Point2D.Double v2 = translate(p, c);

			// Compute dot products
			double dot00 = dot(v0, v0);
			double dot01 = dot(v0, v1);
			double dot02 = dot(v0, v2);
			double dot11 = dot(v1, v1);
			double dot12 = dot(v1, v2);

			// Compute barycentric coordinates
			double invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
			double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
			double v = (dot00 * dot12 - dot01 * dot02) * invDenom;
			
			// Check if point is in triangle
			return (u > 0.) && (v > 0.) && (u + v < 1) && (p.z< u*a.z+v*b.z+(1-u-v)*c.z);
	}
	
	private static Point2D.Double translate(ZPoint b, ZPoint a) {
		return new Point2D.Double(b.x-a.x, b.y-a.y);
	}
	
	private static double dot(Point2D.Double b, Point2D.Double a) {
		return a.x*b.x+a.y*b.y;
	}
	
	@Override
	public void draw(Face f, FaceShader fShader) {
		faces.add(f);
	}

	@Override
	public void draw(final Mesh mesh) {
		Vertex[] vertices = mesh.getVertices();
		for (Face f : mesh.getFaces()) {
			draw(f, null);
		}
		String points ="";   
		for (int idx = 0; idx < vertices.length; idx++) 
			try {
				Point2D.Double p = toSVGScreen(vertices[idx]);
				points += p.x+","+p.y+" ";
				
			} catch (InvisibleZPointException e) {
				return;
			}
			
		if (hide)
			return;
		
		final String fpoints = points;
		XMLCapabilities polygonWriter = new XMLAdapter("polygon") {
			@Override
			public XMLWriter getXMLProperties() {
				XMLWriter e = new XMLWriter(this);
				e.setAttribute("points", fpoints);
				e.setAttribute("style", "fill:"+getRGB(mesh.getColor())+";stroke-width:0; fill-rule:nonzero;");
				return e;
			}
		};
		gWriter.put(polygonWriter);	
			
	}

	@Override
	public void drawArc2D(Point3D p, double r, double a0, double a1, Color c, int thickness) {
		
	}

	@Override
	public void drawImage(BufferedImage img, Point3D p) {
		
	}
	

	public void draw(CurvePlotter<T> curve, Color color, int thickness) {
	}

	
	public void fillCircle(Point3D p, double r, Color c) {
	}

	/*
	 * 
	 * Delegate to renderer
	 * 
	 */
	
	public boolean contains(Pixel p) {
		return renderer.contains(p);
	}

	public int getHeight() {
		return renderer.getHeight();
	}

	public BufferedImage getImage() {
		return renderer.getImage();
	}

	public Point3D getOrigin() {
		return renderer.getOrigin();
	}

	public JPanel getPane() {
		return renderer.getPane();
	}

	public PerspectiveI getPerspective() {
		return renderer.getPerspective();
	}

	public Dimension getSize() {
		return renderer.getSize();
	}

	public QRotation getSpaceTransform() {
		return renderer.getSpaceTransform();
	}

	public double getUnit() {
		return renderer.getUnit();
	}

	public int getWidth() {
		return renderer.getWidth();
	}

	public boolean intersects(Pixel p0, Pixel p1, Pixel p2) {
		return renderer.intersects(p0, p1, p2);
	}

	public Point3D liftTo3DSpace(Pixel p) {
		return renderer.liftTo3DSpace(p);
	}

	public void reset() {
		renderer.reset();
	}

	public void setBackground(Color c) {
		renderer.setBackground(c);
	}

	public void setErrorMessage(String msg) {
		renderer.setErrorMessage(msg);
	}

	public void setOrigin(Point3D p) {
		renderer.setOrigin(p);
	}

	public void setPerspective(PerspectiveI p) {
		renderer.setPerspective(p);
	}

	public void setSize(Dimension d) {
		renderer.setSize(d);
	}

	public void setSpaceTransform(QRotation r) {
		renderer.setSpaceTransform(r);
	}

	public void setUnit(double u) {
		renderer.setUnit(u);
	}
	
	public Pixel toScreen(Point3D p) throws InvisibleZPointException {
		return renderer.toScreen(p);
	}
	
	@Override
	public ZPoint toZSpace(Point3D p) throws InvisibleZPointException {
		return renderer.toZSpace(p);
	}


	public void updateSettings() {
		renderer.updateSettings();
	}

	public void zoom(double f, Point p) {
		renderer.zoom(f, p);
	}
	
	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return "g";
	}

	@Override
	public XMLWriter getXMLProperties() {
		gWriter = new XMLWriter(this);
		gWriter.setAttribute("stroke-linecap", "round");
		//gWriter.setAttribute("fill", "none");
		
		job.display(this);
		return gWriter;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		return null;
	}

	@Override
	public void drawCircleFog(Point3D p, double r, Color c) {
	}

	@Override
	public void drawLineFog(Point3D p, int w, Color c) {
	}
	
	@Override
	public void drawPixel(Point3D p, Color color, int thickness) {
	}
	
	@Override
	public void drawCircle(Point3D p, double radius, Color color, int thickness) {
	}
	
	@Override
	public void drawString(Point3D p, String str, Font font, Color color) {
	}
	
	public void drawAxis(Color c, Font font) {
	}
	
	public void drawGrid(Color c) {
	}}
