/**
 * 
 */
package fw.app;

import java.util.Locale;

import fw.app.Translator.TKey;

public class Language implements Comparable<Language> {

	private final LKey key;

	private Language(LKey key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return key.translate();
	}
	
	public Locale getLocale() {
		return key.loc;
	}

	@Override
	public int compareTo(Language l) {
		return this.toString().compareTo(l.toString());
	}

	private static class LKey extends TKey {
		private final Locale loc;
		
		public LKey(Class<?> c, Locale key) {
			super(c, key.toString());
			this.loc = key;
		}
	}
	
	private static final LKey FR = new LKey(Language.class, Locale.FRENCH);
	//private static final LKey EN = new LKey(Language.class, Locale.ENGLISH);
	
	// TODO : english translation
	
	public static Language[] getAvailableLanguages() {
		return AVAILABLE_LANGUAGES;
	}
	
	private static final Language[] AVAILABLE_LANGUAGES = new Language[]{
			new Language(FR)
			//new Language(EN)
			};
}