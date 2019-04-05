package geotortue.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import fw.geometry.util.Pixel;

public class TurtleAvatar2D {
	
	// TODO : zz custom avatar ?

	private BufferedImage img;
	private static final Pixel hotSpot = new Pixel(15, 15);
	public static enum AVATAR_TYPE {TURTLE, PLANE, BOAT, CAR, BUTTERFLY, DISC10, DISC20, SQUARE20};
	private static Hashtable<AVATAR_TYPE, BufferedImage> avatars = new Hashtable<>(); 

	private static boolean isInitialized = false;
	
	public static void init(Object o) throws IOException {
		if (isInitialized)
			return;
		for (AVATAR_TYPE type : AVATAR_TYPE.values() ) {
			String filename = "";
			switch (type) {
			case TURTLE:
				filename = "turtle";
				break;
			case PLANE:
				filename = "plane";
				break;
			case BOAT:
				filename = "boat";
				break;
			case CAR:
				filename = "car";
				break;
			case BUTTERFLY:
				filename = "butterfly";
				break;
			case DISC10:
				filename = "disc10";
				break;
			case DISC20:
				filename = "disc20";
				break;
			case SQUARE20:
				filename = "square20";
				break;
			default:
				filename = "turtle";
			}

			BufferedImage img = ImageIO.read(o.getClass().getResource("/cfg/avatar/"+filename+".png"));
			avatars.put(type, img);
		}
		isInitialized = true;
	}
	
	public TurtleAvatar2D(Color color, AVATAR_TYPE type) {
		BufferedImage bi = avatars.get(type);
		WritableRaster raster = bi.copyData(null);
		ColorModel cm = bi.getColorModel(); 
		boolean iap = cm.isAlphaPremultiplied();
		this.img =  new BufferedImage(cm, raster, iap, null);
		
		for (int i = 0; i < img.getWidth(); i++) {
			for (int j = 0; j < img.getHeight(); j++) {
				int rgb = img.getRGB(i, j);
				if (rgb == -1) {
					img.setRGB(i, j, color.getRGB());
				}
			}
		}
	}
	
	public TurtleAvatar2D(Color color) {
		this(color, AVATAR_TYPE.TURTLE);
	}
	
	public BufferedImage getImg() {
		return img;
	}

	public Pixel getHotSpot() {
		return hotSpot;
	}

	public static ListCellRenderer<? super Object> getCellRenderer() {
		return new ListCellRenderer<Object>() {
			@Override
			public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				final BufferedImage img = avatars.get(value);
				
				final int iw = img.getWidth();
				final int ih = img.getHeight();	
				
				JPanel pane = new JPanel() {
					private static final long serialVersionUID = 2953847253999989605L;

					public void paint(Graphics g){
						super.paint(g);
						int w = getWidth();
						int x = Math.max(0, (w-iw)/2); 
						g.drawImage(img, x, 0, this);
					}
				};
				if (isSelected)
					pane.setBackground(new Color(250, 255, 250));
				
				pane.setPreferredSize(new Dimension(iw, ih));
				return pane;
			}
		};
	}
	
	public static Collection<BufferedImage> getAvatars() {
		return avatars.values();
	}
}
