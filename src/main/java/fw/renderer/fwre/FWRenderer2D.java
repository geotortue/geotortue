package fw.renderer.fwre;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

import javax.swing.JPanel;

import fw.app.FWConsole;
import fw.geometry.obj.GPoint;
import fw.geometry.proj.PerspectiveI.InvisibleZPointException;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;
import fw.renderer.core.RenderJob;
import fw.renderer.core.Renderer;
import fw.renderer.core.RendererSettingsI;
import fw.renderer.mesh.Face;
import fw.renderer.mesh.Mesh;
import fw.renderer.mesh.Vertex;
import fw.renderer.shader.FaceShader;


public class FWRenderer2D<T extends GPoint> extends Renderer<T> {

	private final Stroke stroke;
	protected BufferedImage offscreenImage;
	private Graphics2D graphics;	
	
	
	public FWRenderer2D(RendererSettingsI s, RenderJob<T> r){
		super(s, r);
		this.stroke  = graphics.getStroke();
		
	}
	
	public int stringWidth(Font f, String str) {
		return graphics.getFontMetrics().stringWidth(str);
	}
	
	protected synchronized void doJob() {
		initBackground();
		super.doJob();
	}


	protected synchronized void paintOffscreenImage(Graphics g){
		g.drawImage(offscreenImage, 0, 0, null);
	}
	
	public BufferedImage getImage() {
		int w = offscreenImage.getWidth();
		int h = offscreenImage.getHeight();
		BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g = im.createGraphics();
		
		Runnable paintACopy = new Runnable() {
			@Override
			public void run() {
				doJob();
				paintOffscreenImage(g);					
			}
		};
		
		if (!EventQueue.isDispatchThread())
			try {
				EventQueue.invokeAndWait(paintACopy);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		else
			paintACopy.run();
		return im;
	}
	

	public boolean contains(Pixel p) {
		return getGraphics().hitClip(p.i, p.j, 1, 1);
	}
	
	
	public boolean intersects(Pixel p0, Pixel p1, Pixel p2) {
		int x = Math.min(Math.min(p0.i, p1.i), p2.i);
		int y = Math.min(Math.min(p0.j, p1.j), p2.j);
		int width = Math.max(Math.max(p0.i, p1.i), p2.i)-x;
		int height = Math.max(Math.max(p0.j, p1.j), p2.j)-y;
		return getGraphics().hitClip(x-1, y-1, width+2, height+2);
	}
	
	/*
	 * Renderer 
	 */
	
	@Override
	public void drawLine(Point3D p, Point3D q, Color c, int thickness) {
		try {
			Pixel p0 = toScreen(p);
			Pixel q0 = toScreen(q);
			getGraphics(c, thickness).drawLine(p0.i, p0.j, q0.i, q0.j);
		} catch (InvisibleZPointException e) {
			return;
		}
	}
	
	@Override
	public void drawPixel(Point3D p, Color c, int thickness) {
		try {
			Pixel p0 = toScreen(p);
			getGraphics(c, thickness).drawLine(p0.i, p0.j, p0.i, p0.j);
		} catch (InvisibleZPointException e) {
			return;
		}
	}
	
	@Override
	public void drawCircle(Point3D p, double r, Color c, int thickness) {
		if (r==0)
			return;
		Graphics2D g = getGraphics(c, thickness);
		try {
			Pixel o = toScreen(p);
			int radius = (int) (r*getUnit());
			g.drawOval(o.i-radius, o.j-radius, 2*radius, 2*radius);			
		} catch (InvisibleZPointException e) {
			return;
		}
	}
	
	@Override
	public void drawArc2D(Point3D center, double r, double start, double end, Color c, int thickness) {
		try {
			getGraphics(c, thickness).draw(getArc2D(center, r, start, end));
		} catch (InvisibleZPointException e) {
			e.printStackTrace();
		}
	}
	
	protected Arc2D.Double getArc2D(Point3D center, double r, double start, double end) throws InvisibleZPointException {
		Pixel p = toScreen(center);
		Pixel p1 = toScreen(center.getTranslated(r, 0, 0));
		double r1 = MathUtils.abs(p1.i-p.i, p1.j-p.j);
		double x = p.i - r1;
		double y = p.j - r1;
		double startAngle = (getSpaceTransform().get2DAngle()+start) * 180 / Math.PI ;
		double arcAngle = (end-start) * 180 / Math.PI;
		return new Arc2D.Double(x, y, 2*r1, 2*r1, startAngle, arcAngle, Arc2D.OPEN);
	}
	
	
	@Override
	public void drawString(Point3D p, String str, Font font, Color color) {
		try {
			Pixel px = toScreen(p);
			Graphics2D g = getGraphics(color);
			g.setFont(font);
			g.drawString(str, px.i, px.j);
		} catch (InvisibleZPointException e) {
		}
	}

	@Override
	public void draw(Face f, FaceShader fShader) {
		try {
			Pixel p0 = toScreen(f.p0);
			Pixel p1 = toScreen(f.p1);
			Pixel p2 = toScreen(f.p2);
			int[] xs = new int[]{p0.i, p1.i, p2.i};
			int[] ys = new int[]{p0.j, p1.j, p2.j};
			int[] c = fShader.getRGBA(1, 0, 0);
			getGraphics(new Color(c[0], c[1], c[2])).fillPolygon(xs, ys, 3);
		} catch (InvisibleZPointException ex) {
			return;
		}
	}

	@Override
	public void draw(Mesh mesh) {
		Vertex[] vertices = mesh.getVertices();
		int verticesCount = vertices.length;
		int[] xs = new int[verticesCount]; 
		int[] ys = new int[verticesCount];
		for (int idx = 0; idx < verticesCount; idx++) 
			try {
				Pixel p = toScreen(vertices[idx]);
				xs[idx] = p.i;
				ys[idx] = p.j;
			} catch (InvisibleZPointException e) {
				return;
			}
			try {
				getGraphics(mesh.getColor()).fillPolygon(xs, ys, verticesCount);
			} catch (IllegalPathStateException ex) {
				FWConsole.printWarning(this, ex+" [see FWRenderer2D.draw()]");
			}
	}
	
	public void fillCircle(Point3D p, double r, Color c) {
		Graphics2D g = getGraphics(c);
		try {
			Pixel o = toScreen(p);
			int radius = (int) (r*getUnit());
			g.fillOval(o.i-radius, o.j-radius, 2*radius, 2*radius);
		} catch (InvisibleZPointException e) {
			return;
		}
	}
	
	public void drawCircleFog(Point3D p, double r, Color c) {
		Graphics2D g = getGraphics(c);
		try {
			Pixel o = toScreen(p);
			int radius = (int) (r*getUnit())+1;
			Point2D center = new Point2D.Float(o.i, o.j);
			float[] dist = {0.0f, 0.9f, 1.0f};
			Color t_white = new Color(255, 255, 255, 0);
			Color[] colors = {t_white, t_white, c};
			RadialGradientPaint rp = new RadialGradientPaint(center, radius, dist, colors,CycleMethod.NO_CYCLE);
			g.setPaint(rp);
			g.fillOval(o.i-radius, o.j-radius, 2*radius, 2*radius);
		} catch (InvisibleZPointException e) {
			return;
		}
	}
	
	
	@Override
	public void drawLineFog(Point3D p, int w, Color c) {
		Graphics2D g = getGraphics(c);
		try {
			Pixel o = toScreen(p);
			Point2D top = new Point2D.Float(o.i, o.j-w);
			Point2D bottom = new Point2D.Float(o.i, o.j);
			float[] fractions = {0.0f, 1.0f};
			Color t_white = new Color(255, 255, 255, 0);
			Color[] colors = {t_white, c};
			LinearGradientPaint lp = new LinearGradientPaint(top, bottom, fractions, colors, CycleMethod.NO_CYCLE); 
			g.setPaint(lp);
			g.fillRect(0, o.j-w, getWidth(), w);
		} catch (InvisibleZPointException e) {
			e.printStackTrace();
		}
	}


	
	@Override
	public void drawImage(BufferedImage img, Point3D p) {
		Graphics2D g = getGraphics();
		try {
			Pixel px = toScreen(p);
			int x = px.i - img.getWidth()/2;
			int y = px.j - img.getHeight()/2;
			g.drawImage(img, x, y, null);
		} catch (InvisibleZPointException e) {
			return;
		}
	}

	@Override
	public Dimension getSize() {
		int w = offscreenImage.getWidth();
		int h = offscreenImage.getHeight();
		return new Dimension(w, h);
	}
	
	private Color backgroundColor = Color.WHITE;
	
	public void setBackground(Color c) {
		backgroundColor = c;
		getPane().setBackground(c);
	}
	
	public final void displayErrorMessage() {
		if (errorMessage != null) {
			Graphics2D g = getGraphics(Color.RED);
			g.setFont(new Font(null, Font.ITALIC, 14));
			g.drawString(errorMessage, 20, 100);
		}
		errorMessage = null;
	}
	
	protected void initBackground() {
		getGraphics(backgroundColor).fillRect(0, 0, getWidth(), getHeight());
	}
	
	protected JPanel getFinalGraphicPane() {
		return new JPanel() {
			private static final long serialVersionUID = 2621100552869544590L;
			
			@Override
			public void paintComponent(Graphics g){
				doJob();
				paintOffscreenImage(g);
			}
		};
	}

	protected final Graphics2D getGraphics() {
		return graphics;
	}
	
	protected final Graphics2D getGraphics(Color c) {
		graphics.setColor(c);
		return graphics;
	}
	
	protected final Graphics2D getGraphics(Color c, int thickness) {
		graphics.setColor(c);
		if (thickness>1)
			graphics.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		else
			graphics.setStroke(stroke);
		return graphics;
	}

	protected final WritableRaster getRaster() {
		return offscreenImage.getRaster();
	}
	
	/*
	 * Size
	 */
	
	public void setSize(Dimension d){
		super.setSize(d);
		this.offscreenImage = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
		this.graphics = (Graphics2D) offscreenImage.getGraphics();
		updateSettings();
	}

	
	public void updateSettings() {
		boolean isAntialiasOn = settings.isAntiAliasOn();
		Object hint = isAntialiasOn ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF;
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, hint);

		hint = isAntialiasOn ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, hint);
	}

	@Override
	public void drawAxis(Color c, Font font) {
		Bounds bounds = new Bounds();
		double prec = bounds.prec;
		DecimalFormat df;
		if (prec > 1) {
			String pattern = "#.";
			for (int idx = 0; idx < prec - 1; idx++)
				pattern += "#";
			df = new DecimalFormat(pattern);
		} else
			df = new DecimalFormat("#");
	
		drawLine(new Point3D(bounds.xmin, 0, 0), new Point3D(bounds.xmax, 0, 0), c, 1);
		drawLine(new Point3D(0, bounds.ymin, 0), new Point3D(0, bounds.ymax, 0), c, 1);

		double u = getUnit();
		double w = 3/u;
		
		double x = bounds.xmin;
		while (x<bounds.xmax) {
			drawLine(new Point3D(x, -w, 0), new Point3D(x, w, 0), c, 1);
			String str = df.format(x);
			double sw = stringWidth(font, str)/u;
			drawString(new Point3D(x-sw/2, -8*w, 0), str, font, c);
			x += bounds.step;
		}
		
		double y = bounds.ymin;
		while (y<bounds.ymax) {
			drawLine(new Point3D(-w, y, 0), new Point3D(w, y, 0), c, 1);
			String str = df.format(y);
			double sw = stringWidth(font, str)/u;
			drawString(new Point3D(-sw-3*w, y-2*w, 0), str, font, c);
			y += bounds.step;
		}
	}
	
	public void drawGrid(Color c) {
		Bounds b = new Bounds();
		
		double x = b.xmin;
		while (x<b.xmax) {
			drawDashedLine(new Point3D(x, b.ymin, 0), new Point3D(x, b.ymax, 0), c);
			x += b.step;
		}
		
		double y = b.ymin;
		while (y<b.ymax) {
			drawDashedLine(new Point3D(b.xmin, y, 0), new Point3D(b.xmax, y, 0), c);
			y += b.step;
		}
	}
	
	private double min(double x1, double x2, double x3, double x4) {
		return Math.min( Math.min(x1, x2), Math.min(x3, x4));
	}
	
	private double max(double x1, double x2, double x3, double x4) {
		return Math.max( Math.max(x1, x2), Math.max(x3, x4));
	}

	private void drawDashedLine(Point3D p, Point3D q, Color c) {
		try {
			Pixel p0 = toScreen(p);
			Pixel q0 = toScreen(q);
			Graphics2D graphics = getGraphics(c);
			graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{4,4}, 0f));
			graphics.drawLine(p0.i, p0.j, q0.i, q0.j);
		} catch (InvisibleZPointException e) {
			return;
		}
	}
	
	private class Bounds {
		final double xmin; 
		final double xmax; 
		final double ymin;
		final double ymax;
		final double step;
		final double prec;
		
		
		private Bounds() {
			int w = getWidth();
			int h = getHeight();
			Point3D p00 = liftTo3DSpace(new Pixel(0, 0)); 
			Point3D p10 = liftTo3DSpace(new Pixel(w, 0));
			Point3D p11 = liftTo3DSpace(new Pixel(w, h));
			Point3D p01 = liftTo3DSpace(new Pixel(0, h));
			this.xmax = max(p00.x, p01.x, p11.x, p10.x);
			this.ymax = max(p00.y, p01.y, p11.y, p10.y);
			
			double u = getUnit();
			this.prec = Math.floor(Math.log10(u));
			double k = Math.pow(10, -prec);
			
			double step_ = k ; // adapt step
			double uk = u*k;
			if (uk>7.5)
				step_ *= 10;
			else if (uk>3.5)
				step_ *= 20;
			else if (uk>1.5)
				step_ *= 50;
			else
				step_ *= 100;
			this.step = step_;

			double xmin_ = min(p00.x, p01.x, p11.x, p10.x);
			double ymin_ = min(p00.y, p01.y, p11.y, p10.y);
			this.xmin = Math.floor(xmin_/step-1)*step; // begin with a smart xmin
			this.ymin = Math.floor(ymin_/step-1)*step;
		}
	}

}