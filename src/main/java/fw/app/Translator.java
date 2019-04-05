package fw.app;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Locale;

import fw.files.CSVException;
import fw.files.CSVFile;
import fw.files.CSVPool;
import fw.files.FileUtilities.HTTPException;

public class Translator {

	private static Hashtable<String, String> TABLE = new Hashtable<String, String>(800);
	private static Locale LOCALE;

	public static void buildTable(Locale l) throws IOException, CSVException, HTTPException {
		LOCALE = l;		
		URL url = FWManager.getResource("/cfg/lang/"+LOCALE.getLanguage()+"/lang.csv");
		
		CSVFile file = new CSVFile(url);
		CSVPool pool = file.getCSVPool();
		TABLE = pool.getTable("key", getLanguage());
	}
	

	public static class TKey {

		private final String code;
		
		public TKey(Class<?> c, String key) {
			this.code = c.getSimpleName() +"."+ key;
			register(this);
		}

		public final String translate(String... args) {
			String value = TABLE.get(code);
			if (value==null) {
				System.err.println("Il manque la traduction d'un mot-clef : " + code);
				return code;
			}
			for (int idx = 0; idx < args.length; idx++) 
				value = value.replace("#" + (idx + 1), args[idx]);
			return value;			
		}
	}
	
	private static void register(TKey key) {
		if (!TABLE.isEmpty()) 
			key.translate();
	}
	
	public static Locale getLocale() {
		return LOCALE;
	}
	
	public static String getLanguage() {
		return LOCALE.getLanguage();
	}
	
	public static class TranslationException extends Exception {
		private static final long serialVersionUID = 3868321883226619997L;

		public TranslationException(String message) {
			super(message);
		}
	}
}