/**
 * 
 */
package geotortue.gui;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fw.gui.FWMouseListener;

public abstract class Divider extends JPanel {

	private static final long serialVersionUID = 3917533454219144465L;
	private final Cursor cursor;
	protected int ref;
	
	public enum DIRECTION {S, N, E, W};
	
	public Divider(final DIRECTION direction) {
		switch (direction) {
		case S:
			this.cursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
			break;
		case N:
			this.cursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
			break;
		case E:
			this.cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
			break;
		case W:
			this.cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
			break;
		default:
			this.cursor = null;
		}
				
		FWMouseListener mouseListener = new FWMouseListener() {
			
			public void mouseEntered(MouseEvent e) {
				setCursor(cursor);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				ref = captureRef();
			}

			public void mouseDragged(int x, int y, int mode) {
				switch (direction) {
				case S:
					resize(ref+y);
					break;
				case N:
					resize(ref-y);
					break;
				case E:
					resize(ref+x);
					break;
				case W:
					resize(ref-x);
					break;
				}
			}
		};
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
	}
	
	protected void update(final JPanel owner) {
		invalidate();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				owner.validate();
			}
		});
}
	
	public abstract int captureRef();
	
	public void resize(int x) {};
}