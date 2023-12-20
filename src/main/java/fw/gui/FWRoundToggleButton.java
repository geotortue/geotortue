/**
 * 
 */
package fw.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.swing.JToggleButton;
import javax.swing.UIManager;

import fw.text.TextStyle;
import fw.util.swing.SwingUtilities2;

/**
 *
 */
public class FWRoundToggleButton extends JToggleButton implements FWAccessible {

	private static final long serialVersionUID = -804370030606721765L;
	
	private Shape shape;
	
	public FWRoundToggleButton() {
		setContentAreaFilled(false);
		setRolloverEnabled(true);
		FWAccessibilityManager.register(this);
	}

	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (model.isArmed() || model.isSelected()) {
			g.setColor(UIManager.getColor("ToggleButton.select"));
		} else {
			g.setColor(getBackground());
		}

		g2.fillOval(0, 0, getSize().width - 1, getSize().height - 1);

		FontMetrics fm = g.getFontMetrics();
		
		String text = getText();
		int w = SwingUtilities2.stringWidth(this, fm, text);
        int h = fm.getHeight();
        int x = (getWidth()-w)/2;
        int y = (getHeight()-h)/2+fm.getMaxAscent();
        
        g2.setColor(Color.BLACK);
        
        Font f = UIManager.getFont("RoundToggleButton.font");
        g2.setFont(f);
        g2.drawString(text, x, y);
	}

	protected void paintBorder(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(getForeground());
		for (int s = 0; s < 1; s++)
			g2.drawOval(s, s, getSize().width -2*s-1, getSize().height -2*s-1);
		
	}

	public boolean contains(int x, int y) {
		if (shape == null || !shape.getBounds().equals(getBounds())) {
			shape = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
		}
		return shape.contains(x, y);
	}
	
	@Override
	public void setFont(TextStyle s) {
		setFont(s.deriveFont(TextStyle.PLAIN, 2));
	}
}