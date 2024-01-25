package fw.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fw.app.Translator;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TranslatorKeyTest {

	@DisplayName("Test Create TKey with only a key")
    @Test	
	public void createTranslatorKey() {
        final String key = "dummy";

        Translator.TKey tKey = new Translator.TKey(key);

        // no translate messages setup
        assertTrue(key.equals(tKey.translate()));
    }
}


