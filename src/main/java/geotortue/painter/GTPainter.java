package geotortue.painter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.EventListener;
import java.util.Stack;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;

import fw.app.FWToolKit;
import fw.gui.FWMouseListener;
import fw.gui.params.FWInteger;



public class GTPainter extends JPanel  {
	private static final long serialVersionUID = 9176325746799351391L;
	private final GTColorChooserPane colorChooser = new GTColorChooserPane();
	private final DrawingPane drawingPane;
	private final JScrollPane scrollDrawingPane;
	
	public enum MOUSE_MODE {FLOOD, PICK, MAGIC_WAND};
	private MOUSE_MODE mouseMode = MOUSE_MODE.FLOOD;
	
	private FWInteger floodTolerance = new FWInteger("floodTolerance", 40, 1, 255);
	private FWInteger magicWandTolerance = new FWInteger("magicWandTolerance", 100, 1, 255);
	private FWInteger undoDepth = new FWInteger("undoDepth", 20, 1, 64);
	
	private Stack<Raster> backupRasters = new Stack<Raster>();;

	public GTPainter(){
		super(new BorderLayout());
		this.drawingPane = new DrawingPane();
		this.scrollDrawingPane = new JScrollPane(drawingPane);
		scrollDrawingPane.setWheelScrollingEnabled(false);
		
		
		FWToolKit.createCursor("flood.gif", 12, 14);
		FWToolKit.createCursor("picker.gif", 0, 8);
		FWToolKit.createCursor("magic-wand.gif", 3, 28);
		
		drawingPane.setAutoscrolls(true);
		
		FWMouseListener listener = new FWMouseListener() {
			public void mouseClicked(MouseEvent e, int mode) {
				if (getMouseModifiers(e) != LEFT)
					return;
				
				Point p = drawingPane.getImageHit();
				if (p==null)
					return;
				
				final int x = p.x;
				final int y = p.y;
				
				switch (mouseMode) {
					case FLOOD:
						final int c = colorChooser.getCurrentColor().getRGB();
						new Thread() {
							@Override
							public void run() {
								drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
								backup();
								drawingPane.floodFill(x, y, c, floodTolerance.getValue());
								setMode(mouseMode);
							}
						}.start();
						break;
					case PICK:
						int col = drawingPane.getImage().getRGB(x, y);
						colorChooser.setColor(new Color(col));
						break;
					case MAGIC_WAND:
						final int c1 = colorChooser.getCurrentColor().getRGB();
						new Thread() {
							@Override
							public void run() {
								drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
								backup();
								drawingPane.replaceColor(x, y, c1, magicWandTolerance.getValue());
								setMode(mouseMode);
							}
						}.start();
						break;
					default:
						break;
				}
			}
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (!drawingPane.zoom(e.getWheelRotation()<0))
					return;

				drawingPane.updateScaledImage();
				drawingPane.invalidate();
				validate();
				Dimension d = drawingPane.getSize();
				drawingPane.scrollRectToVisible(new Rectangle((d.width-getWidth())/2, (d.height-getHeight())/2, getWidth(), getHeight()));
			}
			
			private final Rectangle rect = new Rectangle();
			
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if (getMouseModifiers()!=RIGHT)
					return;
				drawingPane.setCursor(FWToolKit.getCursor("move.gif"));
				rect.setBounds(drawingPane.getVisibleRect());
			}

			@Override
			public void mouseDragged(int x, int y, int mode) {
				if (mode!=RIGHT)
					return;
				drawingPane.scrollRectToVisible(new Rectangle(rect.x-x, rect.y-y, rect.width, rect.height));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				setMode(mouseMode);
			}
			
			
		};
		
		drawingPane.addMouseListener(listener);
		drawingPane.addMouseMotionListener(listener);
		drawingPane.addMouseWheelListener(listener);
		
		add(colorChooser, BorderLayout.EAST);
		add(scrollDrawingPane, BorderLayout.CENTER);
		
		setMode(MOUSE_MODE.FLOOD);
	}
	
	public boolean hasNotBeenModified() {
		return backupRasters.isEmpty();
	}

	
	private void backup() {
		if (backupRasters.size()>=undoDepth.getValue())
			backupRasters.remove(0);
		backupRasters.push(drawingPane.getData());
		fireUndoStatusUpdated(true);
	}
	
	public void undo(){
		if (!backupRasters.isEmpty())
			 drawingPane.setData(backupRasters.pop());
		fireUndoStatusUpdated(!backupRasters.isEmpty());
		repaint();
	}
	
	public BufferedImage getImage(){
		return  drawingPane.getImage();
	}
	
	public void setImage(BufferedImage im){
		if (im==null)
			return;
		drawingPane.setImage(im);
		backupRasters.clear();
		fireUndoStatusUpdated(false);
		scrollDrawingPane.doLayout();
	}

	public void setMode(MOUSE_MODE mode) {
		this.mouseMode = mode;
		switch (mode) {
		case FLOOD:
			drawingPane.setCursor(FWToolKit.getCursor("flood.gif"));
			break;
		case PICK:
			drawingPane.setCursor(FWToolKit.getCursor("picker.gif"));
			break;
		case MAGIC_WAND:
			drawingPane.setCursor(FWToolKit.getCursor("magic-wand.gif"));
			break;
		default:
			break;
		}
	}
	
	public void crop() {
		drawingPane.crop();
	}
	
	/*
	 * Listener
	 */
	private final EventListenerList listeners = new EventListenerList();
	
    public void addListener(Listener listener) {
        listeners.add(Listener.class, listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(Listener.class, listener);
    }

    public Listener[] getListeners() {
        return (Listener[]) listeners.getListeners(Listener.class);
    }

    protected synchronized void fireUndoStatusUpdated(boolean b){
    	Listener[] table = getListeners();
        for (int idx = 0; idx < table.length; idx++)
            table[idx].undoStatusUpdated(b);
    }
    
	public static interface Listener extends EventListener {
		public void undoStatusUpdated(boolean b);
	}

	
	/*
//	 * XML
//	 */
//	
//	@Override
//	public String getXMLTag() {
//		return "GTPainter";
//	}
//
//	/*
//	 * FWS
//	 */
//
//	@Override
//	public JPanel getSettingsPane(FWSettingsListeners l) {
//		return VerticalPairingLayout.createPanel(10, 10, 
//				new FWLabel(this, "floodTolerance", SwingConstants.RIGHT), floodTolerance.getComponent(l), 
//				new FWLabel(this, "magicWoodTolerance", SwingConstants.RIGHT), magicWandTolerance.getComponent(l),
//				new FWLabel(this, "undoDepth", SwingConstants.RIGHT), undoDepth.getComponent(l));
//	}
}