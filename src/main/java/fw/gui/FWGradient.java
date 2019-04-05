package fw.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.gui.layout.BasicLayoutAdapter;
import fw.gui.layout.VerticalFlowLayout;
import fw.gui.params.FWDouble;
import fw.gui.params.FWParameterListener;


public class FWGradient  extends JPanel {
	private static final long serialVersionUID = -2718081319095180805L;

	private static final ActionKey DELETE_MARK = new ActionKey(FWGradient.class, "deleteMark");
	private static final ActionKey DUPLICATE_MARK = new ActionKey(FWGradient.class, "duplicateMark");
	public final Mark infMark = new Mark(-1, Color.WHITE);
	public final Mark supMark = new Mark(1, Color.BLACK);
		
	private Vector<Mark> marks = new Vector<Mark>();
	
	private final Bar bar = new Bar();
	private final MarksPanel marksPanel = new MarksPanel();
	private FWDouble wrapCoeff = new FWDouble("wrapCoeff", 1, 0.0001, 1000, 1);
	
	public FWGradient(){
		super();
		setLayout(new Layout());
		add(new Head());
		add(bar);
		add(marksPanel);
		add(getSettingsPane());
		setPreferredSize(new Dimension(200, 40));
		marks.add(infMark);
		marks.add(supMark);
		marks.add(new Mark(0.5, Color.BLUE));
		marks.add(new Mark(0, Color.BLACK));
		marks.add(new Mark(-0.5, Color.GREEN));
	}

	public Color getColor(double v){
		v = wrapCoeff.getValue()*v;
		if (v>1 || v<-1)
			v = v%1;
		
		
		Mark m0 = infMark; 
		for (Mark m : marks) {
			if ((m0.value<m.value) && (m.value<=v))
				m0=m;
		}
		
		Mark m1 = supMark;
		for (Mark m : marks) {
			if ((v<=m.value) && (m.value<m1.value))
				m1=m;
		}

		double v0 = m0.value;
		double v1 = m1.value;
		double k =(v-v0)/(v1-v0);
		
		int r0 = (m0.color >> 16) & 0xff; 
		int g0 = (m0.color >>  8) & 0xff; 
		int b0 = (m0.color) & 0xff;
		int r1 = (m1.color >> 16) & 0xff; 
		int g1 = (m1.color >>  8) & 0xff; 
		int b1 = (m1.color) & 0xff;

		int r = r0+(int) (k*(r1-r0));
		int g = g0+(int) (k*(g1-g0));
		int b = b0+(int) (k*(b1-b0));
		
		return new Color(r, g, b);
	}
	
	private class Head extends JPanel {
		private static final long serialVersionUID = 3724629192677995511L;

		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			int w = getWidth();
			int h =getHeight();
			g2.drawString("-1", 0, h);
			g2.drawString("0", w/2-3, h);
			g2.drawString("+1", w-16, h);
		}
	}
	
	private class Bar extends JPanel {
		private static final long serialVersionUID = -7345133146985700L;

		private Bar(){
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					int w = getWidth();
					int x = e.getPoint().x;
					double v=-1+2*x/(double) w;
					addMark(v);
				}
			});
		}
		
		protected void paintComponent(Graphics g) {
			int w = getWidth();
			int h = getHeight()-1;
			
			for (int i = 1; i < w; i++) {
				g.setColor(getColor(-1+2*i/(double) w));
				g.drawLine(i, 0, i, h);	
			}
		}
	}
	
	public void addMark(double v){
		Color c = FWColorChooser.showDialog(getTopLevelAncestor(), Color.BLACK);
		if (c==null)
			return;
		addMark(v, c);
	}

	public void addMark(double v, Color c){
		marks.add(new Mark(v, c));
		repaint();
	}
	
	int gap = 8;
	
	private class Mark {
		double value;
		int color;
		
		public Mark(double v, Color c){
			this.value=v;
			this.color=c.getRGB();
		}
		
		public boolean isDeletable(){
			return (this != infMark) && (this != supMark);
		}
	}

	private class MarksPanel extends JPanel {
		private static final long serialVersionUID = 8850977642108247654L;
		
		Mark selectedMark;
		
		public MarksPanel(){
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					selectedMark = getMarkAt(e);
					if (selectedMark == null)
						return;
					
					if (e.getClickCount()==2){
						Color c = FWColorChooser.showDialog(getTopLevelAncestor(), new Color(selectedMark.color));
						if (c==null)
							return;
						selectedMark.color = c.getRGB();
						repaint();
					}
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					selectedMark = getMarkAt(e);
				}
			});
			
			addMouseMotionListener(new MouseMotionListener() {
				
				@Override
				public void mouseMoved(MouseEvent e) {
					selectedMark = getMarkAt(e);
					if (selectedMark!=null && selectedMark.isDeletable())
						((JFrame) getTopLevelAncestor()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					else 
						((JFrame) getTopLevelAncestor()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
				
				@Override
				public void mouseDragged(MouseEvent e) {
					if (selectedMark==null  || !selectedMark.isDeletable())
						return;
					double v=getValue(e.getX());
					selectedMark.value=v;
					repaint();
				}
			});
		}

		
		private Mark getMarkAt(MouseEvent e){
			Mark mark = null;
			double v=getValue(e.getX());
			double tolerance = gap/((double) getWidth()-2*gap);
			for (Mark m : marks) {
				if (Math.abs(m.value-v)<tolerance)
					mark = m;
			}
			return mark;
		}
		

		@Override
		public JPopupMenu getComponentPopupMenu() {
			if (selectedMark==null)
				return null;
			
			final JPopupMenu menu = new JPopupMenu();
			menu.add(new FWAction(DUPLICATE_MARK, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addMark(selectedMark.value, new Color(selectedMark.color));
					repaint();
				}
			}));
			
			if (selectedMark.isDeletable()) {
				menu.add(new FWAction(DELETE_MARK, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						marks.remove(selectedMark);
						repaint();
					}
				}));
			}
			return menu;
		}
		
		public int getLocation(double v){
			return gap+(int) ((v+1)/2*(getWidth()-2*gap));
		}
		
		public double getValue(int x){
			double v=2*(x-gap)/((double) getWidth()-2*gap)-1;
			if (v<-1)
				return -1;
			if (v>1)
				return 1;
			return v;
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int c = this.getHeight()/3;
			
			for (Mark m : marks) {
				int x= getLocation(m.value);
				g2.setColor(Color.BLACK);
				g2.drawPolygon(new int[]{x-c, x, x+c}, new int[]{c, 0, c}, 3);
				g2.drawRect(x-c, c, 2*c, 2*c-1);
				g2.setColor(new Color(m.color));
				g2.fillRect(x-c+1, c+1, 2*c-1, 2*c-2);
			}
		}

		@Override
		public void repaint() {
			super.repaint();
			bar.repaint();
		}
	}
	
	private JPanel getSettingsPane() {
		return VerticalFlowLayout.createPanel(4, wrapCoeff.getComponent(new FWParameterListener<Double>() {
			@Override
			public void settingsChanged(Double v) {
				bar.repaint();	
			}
		}));
	}
	
	private class Layout extends BasicLayoutAdapter {
		
		
		private final int headHeight = 10, marksHeight = 12, settingsPaneW = 50;
		private int barHeight, lgap, barWidth;
		
		@Override
		protected void init(Container parent) {
			super.init(parent);
			barHeight = parentH-headHeight-marksHeight-3*gap; 
			lgap = insets.left+2*gap;
			barWidth = parentW-settingsPaneW-5*gap;
		}

		@Override
		public void layoutComponent(Component c, int idx) {
			if (idx==0) // head
				c.setBounds(lgap, gap+insets.top, barWidth, headHeight);
			if (idx==1) // bar
				c.setBounds(lgap, 2*gap+insets.top+headHeight, barWidth, barHeight);
			if (idx==2) // mark
				c.setBounds(lgap-gap, 2*gap+insets.top+headHeight+barHeight, 2*gap+barWidth, marksHeight);
			if (idx==3)	// settings
				c.setBounds(lgap+barWidth+2*gap, insets.top+gap, settingsPaneW, parentH-2*gap);
		}
	}
	

}
