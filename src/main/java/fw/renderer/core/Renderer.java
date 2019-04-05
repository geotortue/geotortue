package fw.renderer.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JPanel;

import fw.app.Translator.TKey;
import fw.geometry.obj.GPoint;
import fw.geometry.proj.LinearPerspective;
import fw.geometry.proj.PerspectiveI;
import fw.geometry.proj.PerspectiveI.InvisibleZPointException;
import fw.geometry.proj.ZPoint;
import fw.geometry.util.MathException;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.renderer.mesh.CurvePlotter;


public abstract class Renderer<T extends GPoint> implements RendererI<T> {
	
	private static final TKey DISPLAY_ERROR = new TKey(Renderer.class, "displayError");
	
	protected QRotation spaceTransform = new QRotation();

	private Point3D origin = new Point3D(0, 0, 0);
	private double unit = 1;
	protected final RendererSettingsI settings;

	private PerspectiveI perspective = new LinearPerspective();
	
	protected final JPanel graphicPane;
	private final RenderJob<T> job;
	
	protected String errorMessage = null;
	
	public Renderer(RendererSettingsI s, RenderJob<T> r) {
		this.settings = s;
		this.job = r;
		this.graphicPane = getFinalGraphicPane();
		setSize(new Dimension(640, 480));
	}
	
	protected Renderer(RendererSettingsI s, RenderJob<T> r, JPanel graphicPane) {
		this.settings = s;
		this.job = r;
		this.graphicPane = graphicPane;
		setSize(new Dimension(640, 480));
	}
	
	protected synchronized  void doJob() {
		job.display(this);
		displayErrorMessage();
	}
	
	protected abstract JPanel getFinalGraphicPane();

	@Override
	public final JPanel getPane(){
		return graphicPane;
	}
	
	@Override
	public void setPerspective(PerspectiveI p) {
		this.perspective = p;
		perspective.setScreenSize(getSize());
	}
	
	public final PerspectiveI getPerspective() {
		return perspective;
	}

	/*
	 * Projection
	 */
	
	final public Pixel toScreen(Point3D p) throws InvisibleZPointException {
    	return toScreen(toZSpace(p));
	}
	
	final protected Pixel toScreen(ZPoint p) {
		try {
			return perspective.toScreen(p);
		} catch (MathException e) {
			setErrorMessage(DISPLAY_ERROR.translate());
			//System.out.println(e.getMessage());
			return new Pixel(getSize().width/2, getSize().height/2);
		}
	}
	
	final public ZPoint toZSpace(Point3D p) throws InvisibleZPointException {
		Point3D q = spaceTransform.apply(p);
		double x = origin.x + q.x * unit;
		double y = origin.y + q.y * unit;
		double z = origin.z + q.z * unit;
		return perspective.toZSpace(new Point3D(x, y, z));
	}



	public void setErrorMessage(String msg) {
		if (errorMessage == null)
			errorMessage = msg;
	}
	
	protected abstract void displayErrorMessage();
	
	@Override
	final public Point3D liftTo3DSpace(Pixel p){ 
		Point3D q = perspective.liftTo3DSpace(p);
		double px = (q.x - origin.x)/unit;
		double py = (q.y - origin.y)/unit;
		double pz = (q.z - origin.z)/unit;
		return spaceTransform.inv().apply(new Point3D(px, py, pz));
	}
	
	@Override
	public final int getHeight() {
		return getSize().height;
	}

	@Override
	public final int getWidth() {
		return getSize().width;
	}

	public void setSize(Dimension d){
		if (d.width<=0 || d.height<=0) {
			new IllegalArgumentException(
					"Width (" + d.width + ") and height (" + d.height + ") cannot be <= 0").printStackTrace();
			d.width = 640;
			d.height = 480;
		}
		graphicPane.setMinimumSize(d);
		graphicPane.setMaximumSize(d);
		graphicPane.setPreferredSize(d);
		graphicPane.invalidate();
		
		perspective.setScreenSize(d);
	}
	
	@Override
	public Dimension getSize() {
		return graphicPane.getPreferredSize();
	}
	
	
	/*
	 * Transform
	 */
    
	public final Point3D getOrigin() {
		return origin;
	}
	
	@Override
	public void setOrigin(Point3D p) {
		origin = p;
	}
	
	@Override
	public double getUnit(){
		return unit;
	}
	
	private final double maxUnit = 1E9;
	private final double minUnit = 1E-9;

	@Override
	public void setUnit(double u) {
		if (u > maxUnit)
			this.unit = maxUnit;
		else if (u < minUnit)
			this.unit = minUnit;
		else if (u > 0)
			this.unit = u;
	}
	
	public void zoom(double f, Point p){
		double newUnit = unit*f ;
		if (newUnit > maxUnit || newUnit < minUnit)
			return;
		int ex = -getWidth()/2 + p.x;
    	int ey = getHeight()/2 - p.y;
    	double x = f*(origin.x-ex) + ex;
    	double y = f*(origin.y-ey) + ey;
    	double z = f*origin.z;
    	origin = new Point3D(x, y, z);
    	setUnit(unit*f);
    }
	
	public void draw(CurvePlotter<T> curve, Color color, int thickness) {
		double t0 = curve.getT0();
		double dt = curve.getStep();
		double t = t0 + dt;
		Point3D p0 = curve.getPoint(t0);
		Point3D p1;
		for (int idx = 0; idx < curve.getCount(); idx++) {
			p1 = curve.getPoint(t);
			drawLine(p0, p1, color, 1);
			t0 = t;
			t += dt;
			p0 = p1;
		}
	}
	
	public QRotation getSpaceTransform() {
		return spaceTransform;
	}

	public void setSpaceTransform(QRotation r) {
		this.spaceTransform = r;
	}

	@Override
	public void reset() {
		setOrigin(new Point3D(0, 0, 0));
		setSpaceTransform(new QRotation());
		setUnit(1);
	//	setBackground(Color.WHITE);
	}
}