package geotortue.painter;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import fw.gui.FWColorChooser;
import fw.gui.layout.FixGridLayout;


class GTColorChooserPane extends JPanel {

	private static final long serialVersionUID = 1153939479672185781L;
	
	private int cellSize=20;
	private ColorCase headerCase;
	private Color currentColor = new Color(1, 1, 1);

	GTColorChooserPane(){
		JPanel pane=new JPanel();
		pane.setLayout(new FixGridLayout(2, 14, 2, 2, cellSize, cellSize));
		pane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		pane.add(new ColorCase(0, 0, 0));
		pane.add(new ColorCase(255, 255, 255));
		pane.add(new ColorCase(189, 189, 189));
		pane.add(new ColorCase(123, 123, 123));
		
		pane.add(new ColorCase(255, 0, 0));
		pane.add(new ColorCase(123, 0, 0));
		pane.add(new ColorCase(255, 255, 0));
		pane.add(new ColorCase(123, 123, 0));

		pane.add(new ColorCase(0, 255, 0));
		pane.add(new ColorCase(0, 123, 0));
		pane.add(new ColorCase(0, 255, 255));
		pane.add(new ColorCase(0, 123, 123));
		
		pane.add(new ColorCase(0, 0, 255));
		pane.add(new ColorCase(0, 0, 123));
		pane.add(new ColorCase(255, 0, 255));
		pane.add(new ColorCase(123, 0, 123));
		
		pane.add(new ColorCase(255, 255, 123));
		pane.add(new ColorCase(123, 123, 57));
		pane.add(new ColorCase(0, 255, 123));
		pane.add(new ColorCase(0, 57, 57));
		
		pane.add(new ColorCase(123, 255, 255));
		pane.add(new ColorCase(0, 123, 255));
		pane.add(new ColorCase(123, 123, 255));
		pane.add(new ColorCase(0, 57, 123));
		
		pane.add(new ColorCase(255, 0, 123));
		pane.add(new ColorCase(57, 0, 255));
		pane.add(new ColorCase(255, 123, 57));
		pane.add(new ColorCase(123, 57, 0));
		
		JPanel head=new JPanel();
		head.setLayout(new FixGridLayout(1, 1, 2, 10, 2*cellSize+1, 2*cellSize+1));
		this.headerCase = new ColorCase(0, 0, 0); 
		head.add(headerCase);
		headerCase.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				Color c = FWColorChooser.showDialog(getTopLevelAncestor(), headerCase.color);
				if (c==null)
					return;
				setColor(c);
			}
		});
		
		setLayout(new BorderLayout());
		add(pane, BorderLayout.CENTER);
		add(head, BorderLayout.NORTH);
	}
	
	void setColor(Color c){
		currentColor = c;
		headerCase.color = c;
		headerCase.repaint();
	}
	
	private class ColorCase extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private Color color;
		
		private ColorCase(int r, int g, int b){
			super();
			this.color=new Color(r, g, b);
			
			addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent arg0) {
					setColor(color);
				}

				public void mouseEntered(MouseEvent e) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}

				public void mouseExited(MouseEvent e) {
					setCursor(Cursor.getDefaultCursor());
				}
			});
			
			setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		}
		
		protected void paintComponent(Graphics g){
			g.setColor(color);
			g.fillRect(2, 2, 2*cellSize, 2*cellSize);
		}
	}
	
	Color getCurrentColor() {
		return currentColor;
	}
}
