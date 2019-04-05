package fw.app;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.ImageIcon;



public class FWToolKit {

	private static final IconFactory iconFactory = new IconFactory();
	private static final CustomCursorFactory cursorFactory = new CustomCursorFactory();
	
	public static ImageIcon getIcon(String name){
		return iconFactory.get(name); 	
	}
	
	private static class IconFactory {
		
		private static final Hashtable<String, ImageIcon> table = new Hashtable<String, ImageIcon>();
		
		/**
		 * 
		 */
		private ImageIcon get(String name) {
			if ((table.keySet()).contains(name))
				return table.get(name);

			ImageIcon icon=create(name);
			if (icon == null)
				System.err.println("Icon not found : "+name);
			else
				table.put(name, icon);
			return icon;
		}
		
		private ImageIcon create(String name){
	        URL url = FWManager.getResource("/cfg/icon/"+name);
	        if (url!=null)
	        	return new ImageIcon(url, name);
	        return null;
		}
	}

	/*
	 * 
	 */
	
	
	public static Cursor getCursor(String fileName) {
		return cursorFactory.get(fileName);
	}
	
	public static void createCursor(String fileName, int x, int y) {
		cursorFactory.createCursor(fileName, x, y);
	}
	
	private static class CustomCursorFactory {
		private static Toolkit toolkit = Toolkit.getDefaultToolkit();

		private final Hashtable<String, Cursor> table = new Hashtable<String, Cursor>();
		
		private Cursor get(String fileName) {
			if (table.get(fileName) == null) {
				System.err.println("Cursor '" + fileName + "' not found.");
				return Cursor.getDefaultCursor();
			}
			return table.get(fileName);
		}
		
		private void createCursor(String fileName, int x, int y) {
			if (table.get(fileName)==null)
				table.put(fileName, createCursor_(fileName, x, y));
		}

		private Cursor createCursor_(String fileName, int x, int y) {
	        URL url = FWManager.getResource("/cfg/cursor/"+ fileName);
	        if (url != null){
	        	return toolkit.createCustomCursor(toolkit.getImage(url), new Point(x, y), fileName);
	        } else
	        	return null;
		}
	}
}