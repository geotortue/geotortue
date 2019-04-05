package fw.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import fw.app.Translator.TKey;
import fw.gui.layout.BasicLayoutAdapter;
import fw.text.TextStyle;



public class FWTitledPane extends JPanel  implements FWAccessible {
	private static final long serialVersionUID = -3909560839530491790L;

	protected String title;
	
	protected int headHeight = 24;
	protected int headRightInset = 10;
	protected Head head = new Head();
	protected JPanel componentPane = new JPanel(new GridLayout(1, 1));

	private Font font = UIManager.getFont("MenuItem.font");
	
	public FWTitledPane(TKey title, JComponent c){
		setLayout(new Layout());
		this.title = title.translate();
		
		componentPane.add(c);
		relayout();
		FWAccessibilityManager.register(this);
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		int w = d.width;
		int h = d.height + headHeight;
		return new Dimension(w, h);
	}



	protected void relayout(){
		removeAll();
		add(head);
		add(componentPane);
	}

	protected class Head extends JPanel {
		private static final long serialVersionUID = 9177285962597457925L;

		public Head(){
			super();
			setBackground(Color.WHITE);
		}
		
		public void paint(Graphics g){
			super.paint(g);
			decorate(g);
		}
		
		public void decorate(Graphics g){
			int w = getWidth();
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
			g2.setColor(Color.BLACK);
			g2.setFont(font);
			g2.drawString(title, 6, headHeight-12+5);
			g2.setColor(UIManager.getColor("FWTitledPane.lineColor"));
			int titleLen = title.length();
			char[] chars = new char[titleLen];
			title.getChars(0, titleLen, chars, 0);
			int len = g2.getFontMetrics().charsWidth(chars, 0, title.length());
			g2.drawLine(len+12, headHeight/2, w-headRightInset, headHeight/2);
			g2.setColor(Color.GRAY);
			g2.drawRect(0, 0, w-2, headHeight);
		}
	}
	
	
	private class Layout extends BasicLayoutAdapter {

		
		@Override
		public void layoutComponent(Component c, int idx) {
			if (idx==0) // head
				c.setBounds(0, 0, parentW, headHeight);
			if (idx==1)
				c.setBounds(0, headHeight, parentW, parentH-headHeight);
		}
	}

	@Override
	public void setFont(TextStyle s) {
		this.font = s.getFont();
		FontMetrics fm = getFontMetrics(font);
		int h = fm.getHeight();
		int newHeight = (h>20)? headHeight = h : 24;
		if (newHeight != headHeight) {
			headHeight = newHeight;
			doLayout();
		}
		repaint();
	}
	
	
}
