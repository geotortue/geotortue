package fw.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;


public class FWImagePane extends JPanel {
	private static final long serialVersionUID = 5043867723980002676L;
	
	protected Image image;
	
	public FWImagePane(Image im){
		if (im!=null)
			image=im;
		setPreferredSize(new Dimension(im.getWidth(this), im.getHeight(this)));
	}
	
	public void paint(Graphics g){
		super.paint(g);
		int w=getWidth();
		//int h=getHeight();
		int iw=image.getWidth(this);
		//int ih=image.getHeight(this);
		//int y = Math.max((h-ih)/2, 0);
		
		g.drawImage(image, (w-iw)/2, 0, this);
	}
}
