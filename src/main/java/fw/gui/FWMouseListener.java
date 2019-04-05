package fw.gui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;


public abstract class FWMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {

	private final Point click = new Point();
	
	private int mouseModifiers = -1;

	protected final static short NONE = -1;
	
	private static final short LEFT_BUTTON = 0;
	private static final short RIGHT_BUTTON  = 1;
	
	private static final short NO_MODIFIER = 0;
	private static final short CTRL_DOWN = 2;
	private static final short SHIFT_DOWN = 4;
	private static final short CTRL_SHIFT_DOWN = 6;
	
	private static final short SIMPLE = 0;
	private static final short DOUBLE = 8;
	
	public final static short LEFT = LEFT_BUTTON; // 0
	public final static short LEFT_CTRL = LEFT_BUTTON + CTRL_DOWN; // 2
	public final static short LEFT_SHIFT = LEFT_BUTTON + SHIFT_DOWN; // 4
	public final static short LEFT_CTRL_SHIFT = LEFT_BUTTON + CTRL_SHIFT_DOWN; // 6
	
	public final static short RIGHT = RIGHT_BUTTON; // 1
	public final static short RIGHT_CTRL= RIGHT_BUTTON + CTRL_DOWN; // 3
	public final static short RIGHT_SHIFT = RIGHT_BUTTON + SHIFT_DOWN; // 5
	public final static short RIGHT_CTRL_SHIFT = RIGHT_BUTTON + CTRL_SHIFT_DOWN; //7

	public final static short LEFT_DOUBLE = LEFT_BUTTON + DOUBLE; // 8
	public final static short LEFT_CTRL_DOUBLE= LEFT_BUTTON + CTRL_DOWN + DOUBLE; // 10
	public final static short LEFT_SHIFT_DOUBLE = LEFT_BUTTON + SHIFT_DOWN + DOUBLE; // 12
	public final static short LEFT_CTRL_SHIFT_DOUBLE = LEFT_BUTTON + CTRL_SHIFT_DOWN + DOUBLE; //16
	
	public final static short RIGHT_DOUBLE = RIGHT_BUTTON + DOUBLE; // 9
	public final static short RIGHT_CTRL_DOUBLE = RIGHT_BUTTON + CTRL_DOWN+DOUBLE; // 11
	public final static short RIGHT_SHIFT_DOUBLE = RIGHT_BUTTON + SHIFT_DOWN+DOUBLE; // 13
	public final static short RIGHT_CTRL_SHIFT_DOUBLE = RIGHT_BUTTON + CTRL_SHIFT_DOWN + DOUBLE; // 15
	

	public void mousePressed(MouseEvent e) {
		click.setLocation(e.getLocationOnScreen());
		mouseModifiers = getMouseModifiers(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseReleased(e, mouseModifiers);
	}

	public final void mouseClicked(MouseEvent e) {
		mouseClicked(e, getMouseModifiers(e));
	}
	
	public void mouseClicked(MouseEvent e, int mode) {
	}
	
	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
	
	public void mouseMoved(MouseEvent e) {
	}
	
	public void mouseDragged(MouseEvent e) {
		mouseDragged(e.getXOnScreen()-click.x, e.getYOnScreen()-click.y, mouseModifiers);
	}
	
	public void mouseDragged(int x, int y, int mode) {
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
	}
	
	public void mouseReleased(MouseEvent e, int mode) {
	}
	
	/*
	 * 
	 */
	
	public int getMouseModifiers() {
		return mouseModifiers;
	}
	
	public static int getMouseModifiers(MouseEvent e){
		short button = LEFT_BUTTON;
		if (e.getButton()==MouseEvent.BUTTON3){
			button=RIGHT_BUTTON;
		}

		short mod = 0;
		if (!e.isShiftDown()&& !e.isControlDown())
			mod=NO_MODIFIER;
		else if (e.isControlDown())
			if (e.isShiftDown())
				mod=CTRL_SHIFT_DOWN;
			else mod=CTRL_DOWN;
		else mod=SHIFT_DOWN;

		short click = SIMPLE;
		if (e.getClickCount()==2)
			click=DOUBLE;

		return button+mod+click;
	}
}