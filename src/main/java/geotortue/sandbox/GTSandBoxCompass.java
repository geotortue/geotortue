package geotortue.sandbox;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import fw.app.Translator.TKey;
import fw.geometry.util.Pixel;
import fw.gui.FWMouseListener;
import geotortue.core.TurtleAvatar2D;
import geotortue.core.TurtleAvatar2D.AVATAR_TYPE;
import sun.swing.SwingUtilities2;


public class GTSandBoxCompass extends JPanel {

	private static final TKey RIGHT = new TKey(GTSandBoxCompass.class, "right");
	private static final TKey LEFT = new TKey(GTSandBoxCompass.class, "left");

	private static final long serialVersionUID = 5559372025173539409L;
	
	private static final  Color COLOR = new Color(160, 220, 160);
	
	private static final  TurtleAvatar2D[] AVATARS = new TurtleAvatar2D[]{
			new TurtleAvatar2D(COLOR, AVATAR_TYPE.TURTLE),
			new TurtleAvatar2D(COLOR, AVATAR_TYPE.PLANE),
			new TurtleAvatar2D(COLOR, AVATAR_TYPE.BOAT),
			new TurtleAvatar2D(COLOR, AVATAR_TYPE.CAR),
			new TurtleAvatar2D(COLOR, AVATAR_TYPE.BUTTERFLY),
			new TurtleAvatar2D(COLOR, AVATAR_TYPE.DISC10),
			new TurtleAvatar2D(COLOR, AVATAR_TYPE.DISC20),
			new TurtleAvatar2D(COLOR, AVATAR_TYPE.SQUARE20)
			};
	
	private TurtleAvatar2D avatar2d = AVATARS[0];
	double angle = 0;
	private int buttonY = 85;
	private int buttonR = 6;
	private Shape shape;
	private boolean highlightButton = false;
	
	public GTSandBoxCompass() {
		super();
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		setBackground(Color.WHITE);
		Hashtable<Integer, JLabel> dictionary = new Hashtable<Integer, JLabel>();
		dictionary.put(0, new JLabel("0"));
		dictionary.put(90, new JLabel("90"));
		dictionary.put(180, new JLabel("180"));
		dictionary.put(270, new JLabel("270"));
		dictionary.put(360, new JLabel("360"));
		FWMouseListener l = new FWMouseListener() {
			
			@Override
			public void mouseClicked(MouseEvent e, int mode) {
				if (mode == LEFT_DOUBLE) {
					angle = 0;
					updateShape();
					repaint();
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				if (shape==null)
					return;
				boolean b = shape.contains(e.getPoint());
				if (highlightButton != b) {
					highlightButton = b;
					if (highlightButton)
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					else
						setCursor(Cursor.getDefaultCursor());
					repaint();
				}
			}
	
			@Override
			public void mouseDragged(MouseEvent e) {
				if (highlightButton) {
					int x = e.getX() - getWidth()/2;
					int y = e.getY() - getHeight()/2;
					angle = Math.atan2(-x, -y);//Math.round(Math.atan2(-x, -y)*180/Math.PI)*Math.PI/180;
					updateShape();
					repaint();
				}
			}
	};
	
	addMouseListener(l);
	addMouseMotionListener(l);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		updateShape();
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		int ox = getWidth()/2;
		int oy = getHeight()/2;
		Pixel hotSpot = avatar2d.getHotSpot();
		g.translate(ox, oy);
		g2.rotate(-angle, 0, 0);

		
		drawCircle(g2, 37);
		drawCircle(g2, 40);
		
		
		int N = 24;
		for (int idx = 0; idx < N; idx++) {
			int x0 = (int) (38 * Math.cos(idx * 2*Math.PI/N));
			int y0 = (int) (38 * Math.sin(idx * 2*Math.PI/N));
			int x1 = (int) (40 * Math.cos(idx * 2*Math.PI/N));
			int y1 = (int) (40 * Math.sin(idx * 2*Math.PI/N));
			g.drawLine(x0, y0, x1, y1);
		}
		
		N = 8;
		for (int idx = 0; idx < N; idx++) {
			int x0 = (int) (40 * Math.cos(idx * 2*Math.PI/N));
			int y0 = (int) (40 * Math.sin(idx * 2*Math.PI/N));
			int x1 = (int) (60 * Math.cos(idx * 2*Math.PI/N));
			int y1 = (int) (60 * Math.sin(idx * 2*Math.PI/N));
			g.drawLine(x0, y0, x1, y1);
		}
		
		paintButton(g2);
		
		g.translate(- hotSpot.i, - hotSpot.j);
		g.drawImage(avatar2d.getImg(), 0, 0, null);
		g.translate(hotSpot.i, hotSpot.j);
		
		g2.rotate(angle, 0, 0);
		
		g2.setFont(UIManager.getFont("FWfont.font12"));


		drawString(g, RIGHT.translate(),  -20, 75);
		drawString(g, LEFT.translate(), 20, 75);
		
		g2.setFont(UIManager.getFont("FWfont.font10"));
		drawString(g, 45);
		drawString(g, -45);
		drawString(g, 90);
		drawString(g, -90);
		drawString(g, 135);
		drawString(g, -135);
		drawString(g, 0);
		drawString(g, 180);
		
		g.translate(-ox, -oy);
	}
	
	private void drawCircle(Graphics g, int r) {
		g.drawOval(-r, -r, 2*r, 2*r);
	}

	
	private void drawString(Graphics g, int a) {
		drawString(g, a, 70);
	}
	
	private void drawString(Graphics g, int a, int r) {
		drawString(g, Math.abs(a) + "", a, r);
	}
	
	private void drawString(Graphics g, String str, int a, int r) {
		double alpha = angle + (a+90)*Math.PI/180;
		double c = Math.cos(alpha) ;
		double s = Math.sin(alpha) ;
		int x0 = (int)  (c*r);
		int y0 = (int)  (-s*r);
		FontMetrics fm = g.getFontMetrics();
		int w = SwingUtilities2.stringWidth(this, fm, str);
		x0 -= w/2;
		y0 += fm.getAscent()/2;
		g.drawString(str, x0, y0);
	}
	
	private void paintButton(Graphics2D g) {
		g.setColor(Color.DARK_GRAY);
		g.drawArc(-buttonY, -buttonY, 2*buttonY, 2*buttonY, 70, 20);
		g.drawArc(-buttonY, -buttonY, 2*buttonY, 2*buttonY, 90, 20);
		g.drawLine(29, -80, 25, -78);
		g.drawLine(29, -80, 27, -84);
		g.drawLine(-29, -80, -25, -78);
		g.drawLine(-29, -80, -27, -84);
		
		g.setColor(COLOR);
		g.fillOval(-buttonR, -buttonY-buttonR, 2*buttonR - 1, 2*buttonR - 1);
		
		g.setColor(Color.BLACK);
		g.drawOval(-buttonR, -buttonY-buttonR, 2*buttonR - 1, 2*buttonR - 1);
	}
	
	private void updateShape() {
		int ox = getWidth()/2 ;
		int oy = getHeight()/2;
		int x = (int) (buttonY*(Math.sin(angle)));
		int y = (int) (buttonY*(Math.cos(angle)));
		shape = new Ellipse2D.Float(ox-x-buttonR, oy-y-buttonR, 2*buttonR - 1, 2*buttonR - 1);
	}
	
	public JPopupMenu getComponentPopupMenu() {
		JPopupMenu menu = new JPopupMenu();
		
		for (final TurtleAvatar2D a : AVATARS) {
			BufferedImage im = a.getImg();
			Icon icon = new ImageIcon(im);
			Action action = new AbstractAction("", icon) {
				private static final long serialVersionUID = -7254749333215105470L;

				@Override
				public void actionPerformed(ActionEvent e) {
					avatar2d = a;
					repaint();
				}
			};
			menu.add(new JMenuItem(action));
		}
		
		return menu;
	}
	
}