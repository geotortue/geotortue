package fw.svg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

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
import fw.renderer.fwre.FWRenderer3D;
import fw.renderer.light.LightingContext;
import fw.renderer.mesh.CurvePlotter;
import fw.renderer.mesh.Face;
import fw.renderer.mesh.Mesh;
import fw.renderer.mesh.Vertex;
import fw.renderer.shader.FaceShader;


public class PSTRenderer<T extends GPoint> implements RendererI<T>{

	private String code ="";
	private final RenderJob<T> job;
	private final RendererI<T> renderer;
	private boolean is3D = false;
	
	// TODO : zzz // PSTRenderer
	
	private LightingContext lightingContext;
	
	public PSTRenderer(RendererI<T> r, RenderJob<T> job) {
		this.job = job;
		this.renderer = r;
		if (renderer instanceof FWRenderer3D<?>)
			lightingContext = ((FWRenderer3D<?>) r).getLightingContext();
	}
	
	public String getCode() {
		code = 	"\\psset{unit=.5pt,algebraic=true,linewidth=0.8pt}\n"
				+ "\\begin{pspicture*}";
		
		int w = getWidth();
		int h = getHeight();
		code += is3D? getCode(-w/2, 3*h/2)+getCode(w/2, h/2) : getCode(0, h)+getCode(w, 0);
		code +="\n";
		job.display(this);
		
		code += "\\end{pspicture*}";
		return code;
	}
	
	protected void appendCode(String c) {
		code += c;
	}
	

	
	private String getCode(double x, double y){
		return "("+ round(x) +", "+ round(getHeight()-y) +")";
	}
	
	private String getCode(double x, double y, double z){
		return "("+ round(x) +", "+ round(y) +", "+round(z)+")";
	}

	
	protected String getCode(Point2D.Double p){
		return getCode(p.x, p.y);
	}
	
	private String getCode(Vertex p){
		try {
			return getCode(toZSpace(new Point3D(p.x, p.y, p.z)));
		} catch (InvisibleZPointException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private String getCode(ZPoint p){
		return getCode(p.x, p.y, p.z);
	}
	
	protected Point2D.Double toPSTScreen(Point3D p) throws InvisibleZPointException {
		ZPoint zp = toZSpace(p);
		return getPerspective().toScreen2D(zp);
	}
	
	protected double round(double x){
		return ((int) Math.round(1000*x))/1000.;
	}
	
	protected String getCode(Color c){
		int r=c.getRed();
		int g=c.getGreen();
		int b=c.getBlue();
		if (r==0 && g==0 && b==0)
			return "black";
		return "{[RGB]{"+r+" "+g+" "+b+"}}";
	}

	@Override
	public void drawLine(Point3D p, Point3D q, Color c, int thickness) {
		if (is3D) {
			try {
				ZPoint zp = toZSpace(p);
				ZPoint zq = toZSpace(q);
				String col = getCode(c);
				code += "\\pstThreeDLine[linecolor="+col+"]"+getCode(zp)+getCode(zq)+"\n";

				
			} catch (InvisibleZPointException e) {
				e.printStackTrace();
			}
			
		} else {
			try {
				final Point2D.Double sp = toPSTScreen(p);
				final Point2D.Double sq = toPSTScreen(q);
				String col = getCode(c);
				code += "\\psline[linecolor="+col+"]"+getCode(sp)+getCode(sq)+"\n";
			} catch (InvisibleZPointException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void draw(CurvePlotter<T> curve, Color color, int thickness) {
	}

	@Override
	public void draw(Face f, FaceShader fShader) {
		int[] rgb = fShader.getRGBA(1, 1, 1);
		String color = "{[RGB]{"+rgb[0]+" "+rgb[1]+" "+rgb[2]+"}}";
		code+= "\\pstThreeDTriangle[linecolor="+color+", fillstyle=solid, fillcolor="+color+"]"
				+getCode(f.p0)+getCode(f.p1)+getCode(f.p2)+"\n";
	}

	@Override
	public void draw(Mesh mesh) {
		if (is3D) {
			Face[] faces = mesh.getFaces();
			for (int idx = 0; idx < faces.length; idx++) {
				FaceShader fs = mesh.getShader().getFaceShader(lightingContext, faces[idx]);
				draw(faces[idx], fs);
			}
		} else {
			Vertex[] vertices = mesh.getVertices();
			String points ="";   
			for (int idx = 0; idx < vertices.length; idx++) 
				try {
					Point2D.Double p = toPSTScreen(vertices[idx]);
					points += getCode(p);
				} catch (InvisibleZPointException e) {
					return;
				}
			String col = getCode(mesh.getColor());
				if (col!=null)
					code += "\\pspolygon*[linecolor="+col+"]"+points+"\n";
				else
					code += "\\pspolygon*"+points+"\n";
			}
	}

	@Override
	public void drawArc2D(Point3D p, double r, double a0, double a1, Color c, int thickness) {
		
	}

	@Override
	public void drawImage(BufferedImage img, Point3D p) {
		
	}

	@Override
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
	}
}