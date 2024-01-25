package fw.app;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 * Can be used with:
 *     ResourceBundle bundle = ResourceBundle.getBundle("com.example.i18n.text", new UTF8Control());
 * 
 * @see https://stackoverflow.com/a/4660195
 * @see ResourceBundle UTF-8 Control class https://gist.github.com/DemkaAge/8999236
 */
public class UTF8Control extends Control {

    @Override
    @SuppressWarnings("java:S2093") // try-with-resources: ResourceBundle cannot be converted to AutoCloseable
    public ResourceBundle newBundle(
        final String baseName,
        final Locale locale,
        final String format,
        final ClassLoader loader,
        final boolean reload)
            throws IllegalAccessException, InstantiationException, IOException
    {
        // The below is a copy of the default implementation.
        final String bundleName = toBundleName(baseName, locale);
        final String resourceName = toResourceName(bundleName, "properties");

        InputStream stream = null;
        if (reload) {
            final URL url = loader.getResource(resourceName);
            if (url != null) {
                final URLConnection connection = url.openConnection();
                if (connection != null) {
                    connection.setUseCaches(false);
                    stream = connection.getInputStream();
                }
            }
        } else {
            stream = loader.getResourceAsStream(resourceName);
        }
        
        if (stream == null) {
            return null;
        }
      
        try {
            return new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } finally {
            stream.close();
        }
    }
}
