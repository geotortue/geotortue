/**
 * 
 */
package function;

import fw.HelpI;
import fw.text.FWParsingTools;
import jep2.JKey;

/**
 * @author Salvatore Tummarello
 *
 */
public abstract class JFunction extends PostfixMathCommand2 implements HelpI {

	private final JKey key;

	public JFunction(JKey key, int numberOfparam) {
		super(numberOfparam);
		this.key = key;
	}

	public String getName() {
		return key.translate();
	}

	public String getDescription() {
		String name = getName();
		String msg = "<html><h3>"+name+"</h3>";
		msg +="<p>"+name+key.getDescription()+"</p></html>";
		msg = FWParsingTools.replaceTokens(msg, 
				new String[]{name}, new String[]{"<span class=\"function\">"+name+"</span>"});
		return msg;
	}
}
