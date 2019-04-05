/**
 * 
 */
package jep2;

import fw.app.Translator.TKey;

/**
 * @author Salvatore Tummarello
 *
 */
public class JKey extends TKey {

	private final TKey description;
	
	public JKey(Class<?> c, String key) {
		super(c, key+".name");
		this.description = new TKey(c, key+".desc");
	}
	
	public String getDescription() {
		return description.translate();
	}
	
}
