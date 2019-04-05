package fw.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;


public abstract class FWSelectionListener extends FWMouseListener {
	private Rectangle area = new Rectangle(0, 0, 0, 0); // The selection area
	private boolean paintSelection=false;

	public abstract void manageSelection(Rectangle a);
	public abstract void repaint(Rectangle area);

	public void paint(Graphics g) {
		if (paintSelection){
			g.setColor(Color.BLACK);
			g.drawRect(area.x, area.y, area.width-1, area.height-1);
		}
	}
	
	private final Point click = new Point();
	
	@Override
	public void mousePressed(MouseEvent e){
		super.mousePressed(e);
		if (getMouseModifiers()!=LEFT)
			return;
		area = new Rectangle(0, 0, 0, 0);
		paintSelection=true;
		click.setLocation(e.getPoint());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
		manageSelection(area);
		paintSelection=false;
		repaint(area);
	}

	@Override
	public void mouseDragged(int x, int y, int mode){
		if (mode!=LEFT)
			return;
		int xmin = Math.min(click.x, click.x+x);
		int ymin = Math.min(click.y, click.y+y);
		int width = Math.abs(x);
		int height = Math.abs(y);
		repaint(area);
		area.setBounds(xmin, ymin, width, height);
		repaint(area);
	}
}