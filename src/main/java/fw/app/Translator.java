package fw.app;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.text.MessageFormat;

import fw.files.FileUtilities.HTTPException;

/**
 * Singleton pour gérer une langue.
 */
public class Translator {

	private static final String FILES_ROOT_PATH = "cfg/lang/lang";

	private static Translator translator;

	private final ResourceBundle messages;

	/**
	 * Obtenir une instance de la tables des messages
	 * 
	 * @param locale
	 * @return
	 */
	public static Translator setLocale(final Locale locale) throws NullPointerException {
		if(Translator.translator == null && locale == null) {
			throw new NullPointerException("A local must be provided.");
		}

		if (Translator.translator == null || Translator.getLocale() == null
			|| !locale.equals(Translator.getLocale())) {
			Translator.translator = new Translator(locale);
		}
		
		return Translator.translator;
	}

	private Translator() {
		throw new UnsupportedOperationException("Translator ctor without argument should not have been called.");
	}

    private Translator(final Locale locale) {
		messages = ResourceBundle.getBundle(FILES_ROOT_PATH, locale, new UTF8Control());
	}

	/**
 	 * Référencer un libellé internationalisé et paramétrisé par une clé
	 *
	 * La clé de référence est soit une clé simple soit une clé construite à partir :
	 * - du nom simple d'une classe Java,
	 * - d'une clé partielle, totalement arbitraire.
	 * 
     */
	public static class TKey {

		private final String code;


		/**
		 * Référence un libellé à partir d'une clé simple
		 * 
 		 * @param String key  Clé
		 */
		public TKey(final String key) {
			this(null, key);
		}
		
		/**
		 * Référence un libellé à partir d'une classe et d'une clé partielle
		 * 
		 * @param Class c     Si aucune classe n'est indiquée ({@code null}) seule la clé est prise en compte.
		 * @param String key  Clé partielle
		 */
		public TKey(final Class<?> c, final String key) {
			final String prefix = c != null ? c.getSimpleName() + "." : "";
			code = prefix + key;
		}

		/**
		 * Si la traduction d'une clé n'est pas trouvée, alors la clé elle-même est utilisée comme libellé.
		 * 
		 * Plusieurs occurrences d'un même argument de substitution peuvent être présentes dans le pattern
		 * 
		 * L'ordre d'occurrence des substitutions peut être quelconque.  
		 * Seul l'ordre dans lequel les arguments de substitutions sont placés lors de l'appel de la fonction est pris en compte.
		 * 
		 * @param args
		 * @return la chaîne traduite et complétée des paramètres
		 */
		@SuppressWarnings("java:2209")
		public final String translate(final String... args) {
			if (Translator.translator == null || Translator.translator.messages == null) {
				System.err.println("Aucun dictionnaire n'est disponible");
				return code;				
			}

			try {
				final String value = Translator.translator.messages.getString(code);
				return MessageFormat.format(value, (Object[]) args);
			} catch (NullPointerException | MissingResourceException | ClassCastException e) {
				System.err.printf("Aucun libellé en %s du mot-clef : %s%n", Translator.getLanguage(), code);
				return code;
			}
		}
	}
	
	public static Locale getLocale() {
		return translator.messages != null ? translator.messages.getLocale() : null;
	}
	
	public static String getLanguage() {
		try { 
			final Locale l = getLocale();
			return l != null ? l.getLanguage() : null;
		}
		catch(NullPointerException npe) {
			npe.printStackTrace();
			return null;
		}
	}

}
