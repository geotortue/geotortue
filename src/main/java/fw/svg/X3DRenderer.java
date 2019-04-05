package fw.svg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

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


public class X3DRenderer<T extends GPoint> implements RendererI<T>, XMLCapabilities {

	//TODO : zzz // X3DRenderer
	private XMLWriter writer;
	private final RendererI<T> renderer;
	private final RenderJob<T> job;
	
	public X3DRenderer(RendererI<T> r, RenderJob<T> job) {
		this.job = job;
		this.renderer = r;
	}
	
	public void export(File f) throws IOException {
		XMLWriter writer = new XMLWriter(XMLTagged.Factory.create("X3D"));
		writer.put(this);
		writer.put(new XMLAdapter("viewpoint") {
			
			@Override
			public XMLWriter getXMLProperties() {
				XMLWriter e = new XMLWriter(this);
				e.setAttribute("position", "0 0 100");
				return e;
			}
		});
		new XMLFile(writer).write(f);
	}
	

	@Override
	public void drawLine(Point3D p, Point3D q, Color c, int thickness) {
		
	}


	@Override
	public void drawArc2D(Point3D p, double r, double a0, double a1, Color c, int thickness) {
		
	}


	@Override
	public void draw(CurvePlotter<T> curve, Color color, int thickness) {
		
	}


	@Override
	public void draw(Face f, FaceShader fShader) {
	}


	@Override
	public void draw(Mesh mesh) {
		final Vertex[] vertices = mesh.getVertices();
		final HashSet<int[]> faceScheme = mesh.getFaceScheme();
		final int verticesCount = vertices.length;
		final Color color = mesh.getColor();
		
		XMLCapabilities xmlMesh = new XMLAdapter("Shape") {
			
			@Override
			public XMLWriter getXMLProperties() {
				XMLWriter e = new XMLWriter(this);
				e.put(getAppearance());
				
				
				
				e.put(getXMLFace(faceScheme));
				return e;
			}
			
			private XMLCapabilities getXMLVertices(final Vertex[] vs) {
				return  new XMLAdapter("Coordinate") {
					@Override
					public XMLWriter getXMLProperties() {
						XMLWriter e = new XMLWriter(this);
						String coords = "";
						for (int idx = 0; idx < verticesCount-1; idx++) {
							Vertex v = vs[idx];
							coords += v.x+" "+v.y+" "+v.z+", ";
						}
						e.setAttribute("point", coords);
						return e;
					}
				};
			}
			
			private XMLCapabilities getXMLFace(HashSet<int[]> fs) {
				return new XMLAdapter("IndexedFaceSet") {
					@Override
					public XMLWriter getXMLProperties() {
						XMLWriter e = new XMLWriter(this);
						String scheme = "";
						for (int[] face : faceScheme) {
							for (int idx = 0; idx < face.length; idx++)
								scheme += face[idx] +" ";
							scheme += "-1 ";
						}
						e.setAttribute("coordIndex", scheme);
						e.put(getXMLVertices(vertices));
						return e;
					}
				};
			}
			
			private XMLCapabilities getAppearance() {
				return new XMLAdapter("Appearance") {
					@Override
					public XMLWriter getXMLProperties() {
						XMLWriter e = new XMLWriter(this);
						e.put(new XMLAdapter("Material") {
							@Override
							public XMLWriter getXMLProperties() {
								XMLWriter e = new XMLWriter(this);
								float[] c = color.getRGBColorComponents(null);
								e.setAttribute("diffuseColor", c[0]+" "+c[1]+" "+c[2]);
								return e;
							}
						});
						return e;
					}
				};
			}
		};

		writer.put(xmlMesh);
	}


	@Override
	public void fillCircle(Point3D p, double r, Color c) {
		
	}


	@Override
	public void drawImage(BufferedImage img, Point3D p) {
		
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
		return "Scene";
	}

	@Override
	public XMLWriter getXMLProperties() {
		writer = new XMLWriter(this);
		job.display(this);
		return writer;
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