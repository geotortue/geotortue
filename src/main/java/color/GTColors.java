package color;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Set;

import fw.app.Translator.TKey;
import geotortue.core.GTJEP;

public class GTColors {

	private static final Hashtable<String, GTColor> TABLE = new Hashtable<String, GTColor>();

	private static final TKey WHITE = new TKey(GTColors.class, "WHITE");
	private static final TKey SILVER = new TKey(GTColors.class, "SILVER");
	private static final TKey GRAY = new TKey(GTColors.class, "GRAY");
	private static final TKey BLACK = new TKey(GTColors.class, "BLACK");
	private static final TKey RED = new TKey(GTColors.class, "RED");
	private static final TKey MAROON = new TKey(GTColors.class, "MAROON");
	private static final TKey YELLOW = new TKey(GTColors.class, "YELLOW");
	private static final TKey OLIVE = new TKey(GTColors.class, "OLIVE");
	private static final TKey LIME = new TKey(GTColors.class, "LIME");
	private static final TKey GREEN = new TKey(GTColors.class, "GREEN");
	private static final TKey AQUA = new TKey(GTColors.class, "AQUA");
	private static final TKey TEAL = new TKey(GTColors.class, "TEAL");
	private static final TKey BLUE = new TKey(GTColors.class, "BLUE");
	private static final TKey NAVY = new TKey(GTColors.class, "NAVY");
	private static final TKey FUCHSIA = new TKey(GTColors.class, "FUCHSIA");
	private static final TKey PURPLE = new TKey(GTColors.class, "PURPLE");
	private static final TKey PINK = new TKey(GTColors.class, "PINK");
	private static final TKey ORANGE = new TKey(GTColors.class, "ORANGE");

	static {
		add(WHITE, new GTColor(Color.WHITE));
		add(SILVER, new GTColor(Color.LIGHT_GRAY));
		add(GRAY, new GTColor(Color.GRAY));
		add(BLACK, new GTColor(Color.BLACK));
		add(RED, new GTColor(Color.RED));
		add(MAROON, new GTColor(new Color(128, 0, 0)));
		add(YELLOW, new GTColor(Color.YELLOW));
		add(OLIVE, new GTColor(new Color(128, 128, 0)));
		add(LIME, new GTColor(Color.GREEN));
		add(GREEN, new GTColor(new Color(0, 128, 0)));
		add(AQUA, new GTColor(Color.CYAN));
		add(TEAL, new GTColor(new Color(0, 128, 128)));
		add(BLUE, new GTColor(Color.BLUE));
		add(NAVY, new GTColor(new Color(0, 0, 128)));
		add(FUCHSIA, new GTColor(Color.MAGENTA));
		add(PURPLE, new GTColor(new Color(128, 0, 128)));
		add(PINK, new GTColor(Color.PINK));
		add(ORANGE, new GTColor(Color.ORANGE));
	}

	private static void add(TKey key, GTColor gtColor) {
		TABLE.put(key.translate(), gtColor);
	}

	public static Set<String> getColorNames() {
		return TABLE.keySet();
	}

	public static Color getHexColor(String str_) {
		String str = str_;
		if (str.startsWith("#")) {
			if (str.length() == 4) {
				char r = str.charAt(1);
				char g = str.charAt(2);
				char b = str.charAt(3);
				str = new String(new char[] { '#', r, r, g, g, b, b });
			}
			str = str.replace("#", "0x");
		}

		try {
			return Color.decode(str.startsWith("0x") ? str : "0x" + str);
		} catch (NumberFormatException ex) {
		}
		return null;
	}

	/**
	 * @param jep
	 */
	public static void addColors(GTJEP jep) {
		for (String name  : TABLE.keySet()) 
			jep.addConstant(name, TABLE.get(name));	
	}


}