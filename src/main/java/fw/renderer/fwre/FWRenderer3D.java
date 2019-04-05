package fw.renderer.fwre;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.util.HashSet;

import fw.geometry.obj.GPoint;
import fw.geometry.proj.PerspectiveI.InvisibleZPointException;
import fw.geometry.proj.ZPoint;
import fw.geometry.util.MathException.ZeroVectorException;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.renderer.core.RenderJob;
import fw.renderer.core.RendererSettingsI;
import fw.renderer.fwre.LineDrawer.LineDrawerFactory;
import fw.renderer.fwre.LineDrawer.ZColorI;
import fw.renderer.light.LightingContext;
import fw.renderer.mesh.FVMesh;
import fw.renderer.mesh.Face;
import fw.renderer.mesh.Mesh;
import fw.renderer.mesh.Polyhedron;
import fw.renderer.mesh.Sphere3D;
import fw.renderer.shader.FaceShader;
import fw.renderer.shader.GouraudShader;
import fw.renderer.shader.ShaderI;


public abstract class FWRenderer3D<T extends GPoint> extends FWRenderer2D<T> {

	protected double[] zBuffer;
	
	protected LightingContext lightingContext;
	
	public FWRenderer3D(RendererSettingsI s, RenderJob<T> r, LightingContext lc){
		super(s, r);
		this.zBuffer = new double[getWidth() * getHeight()];
		this.lightingContext = lc;
		setBlurAmount(.2f); 
	}
	
	public void setSize(Dimension d){
		super.setSize(d);
		this.zBuffer = new double[getWidth() * getHeight()];
	}
	
	/*
	 * RendererContext 
	 */

	@Override
	public void initBackground() {
		super.initBackground();
		for (int idx = 0; idx < zBuffer.length; idx++)
			zBuffer[idx] = Double.NEGATIVE_INFINITY;
	}

	@Override
	public void drawLine(Point3D p, Point3D q, Color c, int thickness) {
		if (thickness>1) {
			FVMesh mesh = getCylinder(p, q, c, thickness);
			//draw(mesh);
			mesh.reverseOrientation();
			draw(mesh);
			return;
		}
		try {
			final ZPoint p1 = toZSpace(p);
			final ZPoint q1 = toZSpace(q);
			Pixel px = toScreen(p1);
			Pixel qx = toScreen(q1);

			final int r = c.getRed();
			final int g = c.getGreen();
			final int b = c.getBlue();
			final int[] cA = new int[]{r, g, b, 255};

			ZColorI zColor = new ZColorI() {
				
				@Override
				public int[] getRGBColor(int i, int j, double t) {
					double z = t * p1.z + (1 - t) * q1.z;
					int idx = j * getWidth() + i;
					double zB = zBuffer[idx];
					if (z >= zB) {
						zBuffer[idx] = z;
						return cA;
					}
					return null;
				}
			};

			getLineDrawer(getRaster(), zColor).drawLine(px, qx);
		} catch (InvisibleZPointException e) {
			return;
		}
	}
	
	
	@Override
	public void drawPixel(Point3D p, Color c, int thickness) {
		if (thickness>1) {
			double f = thickness/getUnit()/2;
			int prec = Math.max(4, 1+thickness/2);
			FVMesh mesh = new Sphere3D(p, f, prec).getMesh(c, new GouraudShader());
			draw(mesh);
			return;
		}
		try {
			final ZPoint p1 = toZSpace(p);
			Pixel px = toScreen(p1);
			if (getRaster().getBounds().contains(px.i, px.j)) {
				int idx = px.j * getWidth() + px.i;
				double zB = zBuffer[idx];
				if (p1.z >= zB) {
					zBuffer[idx] = p1.z;
					getGraphics(c).drawLine(px.i, px.j, px.i, px.j);
				}
			}
		} catch (InvisibleZPointException e) {
			return;
		}
	}
	
	protected LineDrawerFactory lineDrawerFactory = LineDrawer.createXiaolinWuLineDrawerFactory();
	
	private LineDrawer getLineDrawer(WritableRaster raster, ZColorI zColor) {
		return lineDrawerFactory.getLineDrawer(raster, zColor);
	}
	
	@Override
	public void draw(Face f, final FaceShader fShader) {
		try {
			final ZPoint q0 = toZSpace(f.p0);
			final ZPoint q1 = toZSpace(f.p1);
			final ZPoint q2 = toZSpace(f.p2);
			if (MathUtils.getNormalZComponent(q0, q1, q2) <= 0) 
				return;
			
			Pixel p0 = toScreen(q0);
			Pixel p1 = toScreen(q1);
			Pixel p2 = toScreen(q2);

			if (!(contains(p0) || contains(p1) || contains(p2) ))
				return;

			
			
			TriangleRasterizer bRaster = new TriangleRasterizer(p0, p1, p2, getWidth(), getHeight()){
				public int[] getRGBAColor(int i, int j, int a0, int a1, int a2) {
					if (contains(new Pixel(i, j))) {
						double z = (a0 * q0.z + a1 * q1.z + a2 * q2.z)/(a0 + a1 + a2);
						int idx = j * getWidth() + i;
						double zB = zBuffer[idx];
						if (z >= zB) { // TODO : pb de ZBUFFER
							int[] rgba = fShader.getRGBA(a0, a1, a2);
							if (rgba == null)
								return null;
							zBuffer[idx] = z;
							return rgba;
						}
					}
					return null;//new int[]{255, 0, 0, 255};
				}
			};
			
			
			getGraphics().drawImage(bRaster.getImage(), bRaster.xmin, bRaster.ymin, null);
		} catch (InvisibleZPointException e) {
			return;
		}
	}

	@Override
	public void draw(Mesh mesh) {
		ShaderI shader = mesh.getShader();
		for (Face f : mesh.getFaces())
			draw(f, shader.getFaceShader(getLightingContext(), f));
	}
	
	public void updateSettings() {
		if (settings.isAntiAliasOn())
			lineDrawerFactory = LineDrawer.createXiaolinWuLineDrawerFactory();
		else
			lineDrawerFactory = LineDrawer.createBresenhamLineDrawerFactory();
	}
	
	
	private float blurAmount;
	private float[] blurMatrix;
	
	protected void setBlurAmount(float a) {
		blurAmount = a;
		float bf = a/8f;
		blurMatrix = new float[]{ bf, bf, bf, 
							bf, 1-a, bf, 
							bf, bf, bf};
	}
	
	
	protected void paintOffscreenImage(Graphics g){
		if (blurAmount!=0)
			g.drawImage(getBluredImg(offscreenImage, blurMatrix), 0, 0, null);
		else 
			super.paintOffscreenImage(g);
	}

	private static BufferedImage getBluredImg(BufferedImage img, float[] matrix) {
		Kernel kernel = new Kernel(3, 3, matrix);
		Graphics2D g2 = (Graphics2D) img.getGraphics();
		ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, g2.getRenderingHints());
		BufferedImage imf = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		op.filter(img.getRaster(), imf.getRaster());
		return imf;
	}
	
	public abstract LightingContext getLightingContext();
	
	private FVMesh getCylinder(Point3D p, Point3D q, Color c, int thickness) {
		Point3D pq = new Point3D(q.x-p.x, q.y-p.y, q.z-p.z);
		
		double f = thickness/getUnit()/2;
		int prec = Math.max(3, 1+thickness/3);
		
		int nCap = Math.min(prec-1, 20);
		
		Point3D normal;
		try {
			normal = new Point3D(q.y-p.y, p.x-q.x, 0).getNormalized().getScaled(f);
		} catch (ZeroVectorException e) {
			normal = new Point3D(1,0,0);
		}

		Point3D[] ps = new Point3D[2*prec+2*(nCap)*prec];
		final double angle = 2*Math.PI/prec;
		final double dh = f/nCap;
		try {
			Point3D capVq = pq.getNormalized().getScaled(dh);
			Point3D capVp = capVq.opp();
			
			QRotation r = new QRotation(pq, angle);
			for (int idx = 0; idx < prec; idx++) {
				normal = r.apply(normal);
				ps[2*idx] = p.getTranslated(normal); // poly surronding p
				ps[2*idx+1] = q.getTranslated(normal); // poly surronding q
				
			}
			
			Point3D ph = p;
			Point3D qh = q;
			for (int k = 0; k < nCap; k++) { 
				double l = (.5+k)*dh/f; 
				double x = Math.sqrt(1-l*l);
				ph = ph.getTranslated(capVp);
				qh = qh.getTranslated(capVq);
				for (int idx = 0; idx < prec; idx++) {
					normal = r.apply(normal);
					ps[2*(k+1)*prec+2*idx] =  ph.getTranslated(normal.getScaled(x)); // cap p
					ps[2*(k+1)*prec+2*idx+1] =  qh.getTranslated(normal.getScaled(x)); // cap q
				}
			}
			
		} catch (ZeroVectorException ex) {
			ex.printStackTrace();
		}
				
		HashSet<int[]> scheme = new HashSet<>();
		for (int idx = 0; idx < prec-1; idx++) { // cylinder
			scheme.add(new int[]{2*idx, 2*idx+1, 2*idx+3, 2*idx+2});
		}
		scheme.add(new int[]{2*prec-2, 2*prec-1, 1, 0});
		
		for (int k = 0; k < nCap; k++) {
			int r = 2*k*prec;
			int s = 2*(k+1)*prec;
			for (int idx = 0; idx < prec-1; idx++) { // halph spheres
				scheme.add(new int[]{r+2*idx+2, s+2*idx+2, s+2*idx, r+2*idx});
				scheme.add(new int[]{r+2*idx+1, s+2*idx+1, s+2*idx+3, r+2*idx+3});
			}
			scheme.add(new int[]{r, s, s+2*prec-2, r+2*prec-2});
			scheme.add(new int[]{r+2*prec-1, s+2*prec-1, s+1, r+1});
			
		}

		int[] pCap = new int[prec];
		for (int idx = 0; idx < prec; idx++)  // cap at p
			pCap[idx] = 2*nCap*prec+2*idx;
		scheme.add(pCap);
		
		int[] qCap = new int[prec];
		for (int idx = 0; idx < prec; idx++)  // cap at q
			qCap[prec-1-idx] = 2*nCap*prec+2*idx+1;
		scheme.add(qCap);
		
		Polyhedron poly = new Polyhedron(ps, scheme); 
		FVMesh mesh = new FVMesh(poly, c, new GouraudShader());
		return mesh;
	}
}