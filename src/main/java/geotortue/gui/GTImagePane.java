package geotortue.gui;

import java.awt.Color;
import java.awt.image.BufferedImage;

import fw.app.FWManager;
import fw.gui.FWImagePane;

public class GTImagePane extends FWImagePane {

	private static final long serialVersionUID = -6077932308118581040L;
	
	private static BufferedImage TURTLE_IMG = FWManager.getImage("/cfg/tortue-v4.png");

	public GTImagePane() {
		super(TURTLE_IMG);
		setBackground(Color.WHITE);
	}
}
