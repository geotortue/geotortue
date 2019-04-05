package fw.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import fw.gui.layout.BasicLayoutAdapter;


public class FWColorBox extends JPanel implements MouseListener {
	private static final long serialVersionUID = -3972209252520806259L;
	
	private Color color;

	private int cellW, cellH;
	private final FWColorChooserListener listener;
	
	public FWColorBox(Color c, int cellW, int cellH, FWColorChooserListener l){
		super(true);
		this.listener = l;
		this.color= c ;
		this.cellW=cellW;
		this.cellH=cellH;
		setLayout(new Layout());
		JPanel cell = new JPanel(){
			private static final long serialVersionUID = -6193994137580128796L;

			protected void paintComponent(Graphics g){
				g.setColor(color);
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
			}
		};
		add(cell);
		cell.addMouseListener(this);
		cell.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		setPreferredSize(new Dimension(cellW, cellH));
	}
	
	public FWColorBox(Color c, FWColorChooserListener l){
		this(c, 36, 24, l);
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color c) {
		this.color = c;
	}

	// MouseListener
	public void mouseClicked(MouseEvent e) {
		Color c = FWColorChooser.showDialog(getTopLevelAncestor(), color);
		if (c==null)
			return;
		setColor(c);
		listener.colorSelected(c);
		repaint();
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
		getTopLevelAncestor().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	public void mouseExited(MouseEvent e) {
		getTopLevelAncestor().setCursor(Cursor.getDefaultCursor());
	}
	
	public interface FWColorChooserListener {
		public void colorSelected(Color c);
	}
	
    // Layout
    private class Layout extends BasicLayoutAdapter{

		@Override
		public void layoutComponent(Component c, int idx) {
			int x = (getWidth()-cellW)/2;
			int y = (getHeight()-cellH)/2;
			c.setBounds(x, y, cellW, cellH);
		}
		
		@Override
		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(cellW, cellH);
		}
    }
}