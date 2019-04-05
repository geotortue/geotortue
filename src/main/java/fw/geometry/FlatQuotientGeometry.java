package fw.geometry;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import fw.app.FWToolKit;
import fw.app.Translator.TKey;
import fw.geometry.obj.GDot;
import fw.geometry.obj.GPoint;
import fw.geometry.obj.GSegment;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;
import fw.gui.FWLabel;
import fw.gui.FWMouseListener;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.BasicLayoutAdapter;
import fw.gui.layout.VerticalFlowLayout;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWBoolean;
import fw.gui.params.FWInteger;
import fw.gui.params.FWParameterListener;
import fw.renderer.core.RendererI;
import fw.renderer.mesh.FVMesh;
import fw.renderer.mesh.Polyhedron;
import fw.renderer.mesh.Vertex;
import fw.renderer.shader.ConstantShader;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class FlatQuotientGeometry<T extends GPoint> implements GeometryI<T>, XMLCapabilities {


	private static final TKey NAME = new TKey(FlatQuotientGeometry.class, "name");

	@Override
	public TKey getTitle() {
		return NAME;
	}
	

	
	private static final TKey SHIFT = new TKey(FlatQuotientGeometry.class, "shift");
	private static final TKey LATTICE_H = new TKey(FlatQuotientGeometry.class, "latticeH");
	private static final TKey LATTICE_W = new TKey(FlatQuotientGeometry.class, "latticeW");
	
	private final FWInteger latticeW, latticeH;
	protected int xGluingMode, yGluingMode;
	protected FWBoolean shiftEnabled = new FWBoolean("shiftEnabled", true);
	private GPoint shiftOffset = new GPoint(0, 0);
	private static Image cylinderImg, mobiusImg, torusImg, kleinImg, boyImg;
	
	public FlatQuotientGeometry(int width, int height) {
		this.latticeW = new FWInteger("width", width, 1, 2048);
		this.latticeH = new FWInteger("height", height, 1, 2048);
		this.xGluingMode = 1;
		this.yGluingMode = 1;
		
		FWToolKit.createCursor("move.gif", 7, 7);
		
		try {
			cylinderImg = ImageIO.read(FlatQuotientGeometry.class.getResource("/cfg/geo/cylinder.png")); // TUR
			mobiusImg = ImageIO.read(FlatQuotientGeometry.class.getResource("/cfg/geo/mobius.png"));
			torusImg = ImageIO.read(FlatQuotientGeometry.class.getResource("/cfg/geo/torus.png"));
			kleinImg = ImageIO.read(FlatQuotientGeometry.class.getResource("/cfg/geo/klein.png"));
			boyImg = ImageIO.read(FlatQuotientGeometry.class.getResource("/cfg/geo/boy.png"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public GPoint getShiftOffset() {
		return shiftOffset;
	}

	public void setShiftOffset(GPoint shiftOffset) {
		this.shiftOffset = shiftOffset;
	}
	
	public Point3D get3DCoordinates(T p) {
		return getQuotient3DCoordinates(p);
	}
	
	private Point3D getQuotient3DCoordinates(GPoint p) {
		double w = latticeW.getValue();
		double h = latticeH.getValue();
		int kx = (int) Math.floor((p.getU1() - shiftOffset.getU1())/ w);
		int ky = (int) Math.floor((p.getU2() - shiftOffset.getU2())/ h);
		double x = p.getU1() - shiftOffset.getU1();
		double y = p.getU2() - shiftOffset.getU2();
		
		if (xGluingMode!=0)
			x -= w * kx;
		if (yGluingMode!=0)
			y -= h * ky;

		if (xGluingMode < 0 && (kx % 2 !=0))
			y = h - y;
		if (yGluingMode < 0 && (ky % 2 !=0))
			x = w - x;

		return new Point3D(x, y, 0);
	}
	
	public int getWPeriod(GPoint p) {
		double w = latticeW.getValue();
		return (int) Math.floor((p.getU1() - shiftOffset.getU1())/ w);
	}
	
	public int getHPeriod(GPoint p) {
		double h = latticeH.getValue();
		return (int) Math.floor((p.getU2() - shiftOffset.getU2())/ h);
	}

	@Override
	public double distance(T p, T q) {
		Point3D p1 = getQuotient3DCoordinates(p);
		Point3D q1 = getQuotient3DCoordinates(q);
		double dx = q1.x-p1.x;
		double dy = q1.y-p1.y;
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public void draw(GSegment<T> s, RendererI<T> r) {
		GPoint p = s.getStart();
		GPoint q = s.getEnd();
		Color c = s.getColor();
		int thickness = s.getThickness();
		
		double w = latticeW.getValue();
		double h = latticeH.getValue();
		
		int pkx = (int) Math.floor((p.getU1() - shiftOffset.getU1()) / w);
		int pky = (int) Math.floor((p.getU2() - shiftOffset.getU2()) / h);
		
		int qkx = (int) Math.floor((q.getU1() - shiftOffset.getU1()) / w);
		int qky = (int) Math.floor((q.getU2() - shiftOffset.getU2()) / h);

		double epsilon = 0.1/r.getUnit();
		while ((xGluingMode !=0 && pkx != qkx)|| (yGluingMode !=0 && pky != qky)) {
			double dx = q.getU1() - p.getU1();
			double dy = q.getU2() - p.getU2();
			
			if (dx > 0)
				pkx++;
			if (dy > 0)
				pky++;
			
			double ix = pkx * w + shiftOffset.getU1();
			double jy = pky * h + shiftOffset.getU2();

			double lambdaX = (ix - p.getU1())/dx;
			double lambdaY = (jy - p.getU2())/dy;

			double iy = p.getU2() + lambdaX * dy;
			double jx = p.getU1() + lambdaY * dx;

			GPoint start = null, stop = null;
			
			if (xGluingMode!=0 && dx!=0)
				if (yGluingMode == 0 || dy == 0 || 
						(yGluingMode != 0 && Math.abs(lambdaX) < Math.abs(lambdaY)) ) {
					if (dx > 0) {
						stop = new GPoint(ix-epsilon, iy);
						start = new GPoint(ix+epsilon, iy);
					} else {
						stop = new GPoint(ix+epsilon, iy);
						start = new GPoint(ix-epsilon, iy);
					}
				}
			if (yGluingMode!=0 && dy!=0)
				if (xGluingMode == 0 || dx == 0 || 
						(xGluingMode != 0 && Math.abs(lambdaY) < Math.abs(lambdaX)) ) {
					if (dy > 0) {
						stop = new GPoint(jx, jy-epsilon);
						start = new GPoint(jx, jy+epsilon);
					} else {
						stop = new GPoint(jx, jy+epsilon);
						start = new GPoint(jx, jy-epsilon);
					}
				}
			
			if (xGluingMode!=0 && yGluingMode!=0 && lambdaX==lambdaY) {
				int sdx = (dx>=0)? 1 : -1;
				int sdy = (dy>=0)? 1 : -1;
				if (sdx>0 || sdy>0) {
					stop = new GPoint(ix - sdx * epsilon, iy - sdy * epsilon);
					start = new GPoint(ix, iy);
				} else {
					stop = new GPoint(ix, iy);
					start = new GPoint(ix + sdx * epsilon, iy + sdy * epsilon);
				}
			}
				
			if (start == null) {
				new NullPointerException().printStackTrace();
				return;
			}

			drawSimpleLine(r, p, stop, c, thickness);

			p = start;
			pkx = (int) Math.floor( (p.getU1() - shiftOffset.getU1()) / w);
			pky = (int) Math.floor( (p.getU2() - shiftOffset.getU2()) / h);
		}
		
		drawSimpleLine(r, p, q, c, thickness);
	}
	
	@Override
	public void draw(GDot<T> d, RendererI<T> r){
		Point3D p = get3DCoordinates(d.getPosition());
		r.drawPixel(p, d.getColor(), d.getThickness());
	}
	
	private void drawSimpleLine(RendererI<T> r, GPoint p0, GPoint p1, Color c, int t) {
		Point3D p = getQuotient3DCoordinates(p0);
		Point3D q = getQuotient3DCoordinates(p1);
		r.drawLine(p, q, c, t);
	}
	
	public void init(final RendererI<T> r) {
		r.reset();
		r.setOrigin(new Point3D(-getLatticeW()/2, -getLatticeH()/2, 0));
		shiftOffset = new GPoint(getLatticeW()/2, getLatticeH()/2);		
		addShiftListener(r);
		r.setBackground(Color.DARK_GRAY);
	}
	
	private HashSet<RendererI<T>> registeredRenderers = new HashSet<RendererI<T>>();
	
	private void addShiftListener(final RendererI<T> r) {
		if (registeredRenderers.contains(r))
			return;
		FWMouseListener shiftListener = new FWMouseListener() {
			private GPoint offset;
				
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if (getMouseModifiers() == RIGHT_SHIFT && shiftEnabled.getValue()) {
					r.getPane().setCursor(FWToolKit.getCursor("move.gif"));
					offset = shiftOffset;
				}
			}
				
			@Override
			public void mouseDragged(int x, int y, int mode) {
				if (mode == RIGHT_SHIFT && shiftEnabled.getValue()) {
					shiftOffset = offset.getTranslated(-x, y);
					r.getPane().repaint();
				}
			}
		};
		
		JPanel pane = r.getPane();
		pane.addMouseListener(shiftListener);
		pane.addMouseMotionListener(shiftListener);
		registeredRenderers.add(r);
	}

	public void paintBackground(RendererI<T> r) {
		if (xGluingMode == 0 && yGluingMode == 0) {
			r.setBackground(Color.WHITE);
			return;
		}
		Point3D q00 = r.liftTo3DSpace(new Pixel(0, r.getHeight()));
		Point3D q11 = r.liftTo3DSpace(new Pixel(r.getWidth(), 0));
		
		double w = latticeW.getValue();
		double h = latticeH.getValue();
		
		double eps = 0.1/r.getUnit();
		double x0 = (xGluingMode == 0)? q00.x: eps;
		double x1 = (xGluingMode == 0)? q11.x: w - eps;
		double y0 = (yGluingMode == 0)? q00.y: eps;
		double y1 = (yGluingMode == 0)? q11.y: h - eps;
		
		Vertex p00 = new Vertex(x0, y0, 0);
		Vertex p01 = new Vertex(x0, y1, 0);
		Vertex p11 = new Vertex(x1, y1, 0);
		Vertex p10 = new Vertex(x1, y0, 0);
		
		Polyhedron poly = new Polyhedron(p00, p10, p11, p01);
		
		FVMesh mesh = new FVMesh(poly, Color.WHITE, new ConstantShader());
		r.draw(mesh);
	}
	
	@Override
	public int getDimensionCount() {
		return 2;
	}


	public double getLatticeW() {
		return latticeW.getValue();
	}

	public double getLatticeH() {
		return latticeH.getValue();
	}
	
	public int getxGluingMode() {
		return xGluingMode;
	}

	public int getyGluingMode() {
		return yGluingMode;
	}
	
	public void setxGluingMode(int n) {
		xGluingMode = n;
	}

	public void setyGluingMode(int n) {
		yGluingMode = n;
	}
	
	@Deprecated
	public boolean isShiftEnabled() {
		return shiftEnabled.getValue();
	}

	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return "FlatQuotientGeometry";
	}

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		latticeW.storeValue(e);
		latticeH.storeValue(e);
		e.setAttribute("xGluingMode", xGluingMode);
		e.setAttribute("yGluingMode", yGluingMode);
		shiftEnabled.storeValue(e);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		latticeW.fetchValue(child, 600);
		latticeH.fetchValue(child, 400);
		xGluingMode = child.getAttributeAsInteger("xGluingMode", 1);
		yGluingMode = child.getAttributeAsInteger("yGluingMode", 1);
		shiftEnabled.fetchValue(child, true);
		return child;
	}
	
	/*
	 * FWS
	 */

	@Override
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		JPanel gluingPane = new JPanel();
		gluingPane.add(new GluingPane(actions));
		
		FWParameterListener<Integer> l = new FWParameterListener<Integer>() {
			@Override
			public void settingsChanged(Integer value) {
				actions.fire(FWSettingsActionPuller.REPAINT);
			}
			
		};
		
		JPanel latticePane = VerticalPairingLayout.createPanel(10, 10, 
				new FWLabel(LATTICE_W), latticeW.getComponent(l), 
				new FWLabel(LATTICE_H), latticeH.getComponent(l));
		
		JPanel shiftPane = VerticalPairingLayout.createPanel(10, 10, 
				new FWLabel(SHIFT, SwingConstants.RIGHT), 
				shiftEnabled.getComponent());
		
		return VerticalFlowLayout.createPanel(10, gluingPane, latticePane, shiftPane);
	}
		
	private class GluingPane extends JPanel {
		private static final long serialVersionUID = -6949125054966351480L;

		private final int width = 240;
		private final int height = 160;

		public GluingPane(final FWSettingsActionPuller actions) {
			super(true);
			setLayout(new Layout());
			setPreferredSize(new Dimension(width, height));
			setBackground(Color.WHITE);
			setBorder(BorderFactory.createEtchedBorder());

			AbstractAction xToggleAction = new AbstractAction() {
				private static final long serialVersionUID = 5967905656907646532L;

				@Override
				public void actionPerformed(ActionEvent e) {
					int n = (xGluingMode != 1)? xGluingMode+1 : -1;
					setxGluingMode(n);
					repaint();
					actions.fire(FWSettingsActionPuller.REPAINT);
				}
			};


			AbstractAction yToggleAction = new AbstractAction() {
				private static final long serialVersionUID = 4044704687735445812L;

				@Override
				public void actionPerformed(ActionEvent e) {
					int n = (yGluingMode != 1)? yGluingMode+1 : -1;
					setyGluingMode(n);
					repaint();
					actions.fire(FWSettingsActionPuller.REPAINT); 
				}
			};

			add(new JButton(yToggleAction){
				private static final long serialVersionUID = 5576235575123614166L;

				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					int w = this.getWidth()/2;
					int h = this.getHeight()/2;
					g.drawLine(0, h, 2*w, h);
					Graphics2D g2 = (Graphics2D) g;
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2.setColor(Color.BLUE);
					if (yGluingMode == 1)
						drawArrow(g2, w, h, 0);
					if (yGluingMode == -1)
						drawArrow(g2, w, h, 2);
				}
			});

			add(new JButton(xToggleAction){
				private static final long serialVersionUID = -6114129694284006279L;

				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					int w = this.getWidth()/2;
					int h = this.getHeight()/2;
					g.drawLine(w, 0, w, 2*h);
					Graphics2D g2 = (Graphics2D) g;
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2.setColor(Color.RED);
					if (xGluingMode == 1)
						drawArrow(g2, w, h, 1);
					if (xGluingMode == -1)
						drawArrow(g2, w, h, 3);
				}
			});
		}


		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawRect(20, 20, 200, 120);

			if (xGluingMode != 0) {
				g2.setColor(Color.RED);
				drawArrow(g2, 20, height/2, 1);
			}

			if (yGluingMode != 0) {
				g2.setColor(Color.BLUE);
				drawArrow(g2, width/2, height-20, 0);
			}

			Image img = getImage();
			if (img != null)
				g2.drawImage(img, (width-80)/2, (height-80)/2, 80, 80, null);
		}

		private void drawArrow(Graphics2D g, int x, int y, int mode) {
			g.rotate(-Math.PI/2*mode, x, y);
			g.fillPolygon(new int[]{x+7, x-6, x-4, x-6}, new int[]{y, y-5, y, y+5}, 4);
			g.rotate(Math.PI/2*mode, x, y);
		}

		private class Layout extends BasicLayoutAdapter {
			
			@Override
			public void layoutComponent(Component c, int idx) {
				if (idx==0)
					c.setBounds(width/2-16, 8, 32, 24);
				else
					c.setBounds(width-32, height/2-16, 24, 32);
			}
		}
		
		private Image getImage() {
			if (xGluingMode * yGluingMode == 0) {
				if (xGluingMode > 0 || yGluingMode > 0)
					return cylinderImg;
				else if (xGluingMode < 0 || yGluingMode < 0)
					return mobiusImg;
			} else if (xGluingMode * yGluingMode == -1) {
				return kleinImg;
			} else {
				if (xGluingMode > 0)
					return torusImg;
				else
					return boyImg;
			}
			return null;
		}
	}


	@Override
	public void paintForeground(RendererI<T> r) {
	}
}