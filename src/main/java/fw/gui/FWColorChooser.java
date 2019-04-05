package fw.gui;

import java.awt.Color;
import java.awt.Container;

import javax.swing.JColorChooser;

import fw.app.Translator.TKey;


/**
 * 
 * A basic extension of JColorChooser.
 *
 */
public class FWColorChooser {
	private static final TKey SELECT_COLOR = new TKey(FWColorChooser.class, "selectColor");
	
	public static Color showDialog(Container owner, Color c) {
		return JColorChooser.showDialog(owner, SELECT_COLOR.translate(), c);
	}
}
