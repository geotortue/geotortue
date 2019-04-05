/**
 * 
 */
package geotortue.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;

import fw.text.FWSyntaxDocument;
import fw.text.FWSyntaxTextPane;

/**
 *
 */
public class GTTextPane extends FWSyntaxTextPane {
	private static final long serialVersionUID = 1284074830011953052L;
	
	public final static Color LIGHT_RED = new Color(255, 225, 225);
    public final static Color LIGHT_GREEN = new Color(200, 255, 200);
	private final HighlightPainter greenPainter = new DefaultHighlighter.DefaultHighlightPainter(LIGHT_GREEN);
	private final HighlightPainter redPainter = new JaggedHighlightPainter();
	private Object currentHL;
	
	public GTTextPane(FWSyntaxDocument doc) {
		super(doc);
		
		addCaretListener(new CaretListener() {
			public void caretUpdate(final CaretEvent e) {
				if (!hasFocus())
					return;
				removeHighlight();
			}
		});
	}
	
	public final void highlight(int offset, int len, boolean err) {
		removeHighlight();
		HighlightPainter painter = (err) ? redPainter : greenPainter;
		currentHL = highlight(offset, offset+len, painter);
	}
	
	public void removeHighlight() {
		if (currentHL!=null)
			getHighlighter().removeHighlight(currentHL);
		currentHL = null;
	}
	
	private static class JaggedHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

		private JaggedHighlightPainter() {
			super(GTTextPane.LIGHT_RED);
		}

		@Override
		public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
			Shape shape =  super.paintLayer(g, offs0, offs1, bounds, c, view);
			Rectangle rect = shape.getBounds();
			
	    	int x = rect.x;
	    	int y_ = rect.y;
	    	int w = rect.width;
	    	int h = rect.height;
	        int y = y_ + h;
	        int x1 = x;
	    	
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Color.RED);
			
			int w1 = 6*(w/6);
			int x3 = x+w1+1;
	        for (int i = x1; i <= x1+w1; i += 6) {
	        	if (i+3<=x3)
	        		g2.drawLine(i, y-3, i+3, y);
	        	if (i+6<=x3)
	        		g2.drawLine(i+3, y, i+6, y-3);
	        }
	        
	        int i =x1+w1;
	        if (w-w1>3) 
	        	g2.drawLine(i, y-3, i+3, y);
	        
	        return shape;
		}

	}
}