/**
 * 
 */
package geotortue.gui;

import java.awt.Window;

import fw.HelpI;
import fw.app.Translator.TKey;
import geotortue.core.GTCommandDescTable;

/**
 *
 */
public class GTHelp {
	
	private static final TKey HELP = new TKey(GTHelp.class, "help");
	
	public static void displayHelp(Window owner) {
		GTDialog.show(owner, HELP, GTCommandDescTable.getGeneralHelp(), false);
	}

	public static void displayHelp(Window owner, HelpI h) {
		GTDialog.show(owner, HELP, h.getDescription(), false);
	}
	
//	public static void displayHelp(Window owner, JFunction fun) {
//		String msg = "<html><h3>"+fun.getName()+"</h3>";
//		msg +="<p>"+fun.getName()+fun.getDescription()+"</p></html>";
//		msg = FWParsingTools.replaceTokens(msg, new String[]{
//				fun.getName(), "#t", "#t'"
//		}, new String[]{
//				"<span class=\"function\">"+fun.getName()+"</span>", 
//				"<span class=\"turtle\">t</span>",
//				"<span class=\"turtle\">t'</span>"
//		});
//		GTDialog.show(owner, HELP, msg, false);
//	}
}