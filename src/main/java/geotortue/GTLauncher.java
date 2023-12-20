package geotortue;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import fw.app.FWConsole;
import fw.app.FWLauncher;
import fw.app.FWManager;
import fw.app.Language;
import fw.app.LanguageNotSupportedException;
import fw.app.Translator.TKey;
import fw.files.CSSFile;
import fw.files.FileUtilities.HTTPException;
import geotortue.GTUpdateChecker.GTVersion;
import geotortue.core.GTCommandDescTable;
import geotortue.core.GTDisplayManager;
import geotortue.core.GTMessageFactory;
import geotortue.core.GeoTortue;
import geotortue.gui.GTSplash;


/**
 *
 */
public class GTLauncher extends FWLauncher {
	
	public static final GTSplash SPLASH = new GTSplash();
	
	public static GTVersion VERSION;
	
	public static final boolean IS_BETA = true;

	private static final TKey LOADING = new TKey(GTLauncher.class, "loading");
	private static final TKey READY = new TKey(GTLauncher.class, "ready");
	
	private static final Color LIGHT_GREEN = new Color(0, 128, 32);
	private static final boolean DEBUG_ON = true;

	private static GeoTortue geoTortue;
	
	static {
		UIManager.put("ProgressBar.background", Color.WHITE);
		UIManager.put("ProgressBar.foreground", LIGHT_GREEN);
		UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
		UIManager.put("ProgressBar.selectionBackground", LIGHT_GREEN);
		UIManager.put("ProgressBar.border", BorderFactory.createEmptyBorder());
		UIManager.put("Slider.foreground", LIGHT_GREEN);
                
        try { // TUR
            Enumeration<URL> resources = GTLauncher.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            if (!resources.hasMoreElements()) {
                throw new Exception("Manifest not found");  // NOSONAR
			}
            while (resources.hasMoreElements()) {
                    Manifest manifest = new Manifest(resources.nextElement().openStream());
                    String configurationVersion = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
                    if (configurationVersion == null) {
						// throw new Exception("Version not found");  // NOSONAR
						VERSION = new GTVersion("0.0.0.0");
						System.out.println("Version not found: 0.0.0.0 will be used.");
					}
                    VERSION = new GTVersion(configurationVersion);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	
	@Override
	public String getName() {
		return "GéoTortue";
	}

	@Override
	public String getVersion() {
		return VERSION + (IS_BETA ? "-beta" : "");
	}
	

	@Override
	public String getDefaultConfigDirName() {
		return ".geotortue";
	}

	private GTLauncher(Locale locale) {
		super(locale);
		
		//super("GéoTortue", ".geotortue", VERSION + (IS_BETA ? "-beta" : ""), lang, DEBUG_ON, IS_BETA);
		
		// Header xml file
		if (IS_BETA)
			try {
				FWManager.setHeader("/cfg/header.beta.xml");
			} catch (HTTPException | IOException ex) {
				ex.printStackTrace();
			}

		
		try {
			SPLASH.setMessage(LOADING);
			SPLASH.setValue(0);

			// CSS
			URL url = FWManager.getResource("/cfg/html/java_style.css");
			StyleSheet styles = new CSSFile(url).getStyleSheet();
			new HTMLEditorKit().setStyleSheet(styles);
			SPLASH.setValue(10);
			
			String code = locale.getLanguage();
			GTCommandDescTable.build(FWManager.getResource("/cfg/lang/"+code+"/cmds.xml"));
			
			SPLASH.setValue(40);
			
			// Messages
			GTMessageFactory.build(FWManager.getResource("/cfg/lang/"+code+"/messages.xml"));
			SPLASH.setValue(50);
			
			//
			geoTortue = new GeoTortue();
			
			SPLASH.setMessage(READY);
			
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					SPLASH.dispose();
					GTUpdateChecker.checkUpdateAtStartUp(geoTortue.getFrame());
				}
			}, 500);
			
			
		} catch (Error | Exception ex){
			System.err.println("!!! ERROR : APPLICATION FAILED TO START !!!\n");
			ex.printStackTrace();
			SPLASH.dispose();
		}
		
		// Console
		FWConsole.setDebugModeEnabled(DEBUG_ON);
		FWConsole.beautifySharedInstance();
	}
	
	public static void launch(Locale lang) throws LanguageNotSupportedException {
		final Language[] availableLanguages = Language.getAvailableLanguages(); 
		for (int idx = 0; idx < availableLanguages.length; idx++)
			if (lang.getLanguage().equals(availableLanguages[idx].getLocale().getLanguage())) {
				new GTLauncher(lang);
				return ;
			}
		throw new LanguageNotSupportedException(lang);
	}
	
	public static void main(final String[] args) {
		
		ArrayList<String> argsList = new ArrayList<>();
		for (int idx = 0; idx < args.length; idx++)
			argsList.add(idx, args[idx]);
		
		FWManager.setAccess(argsList);
		configBackground(argsList);
		Locale lang = getLang(argsList);
		
		try {
			// SPLASH = new GTSplash();
			launch(lang);
		} catch (LanguageNotSupportedException ex) {
			String msg = String.join("\n",
				"Your system is running with language \"" + lang + "\"",
			    "Sorry, but this one is not supported yet.",
				"Launching GéoTortue in french..."
			);
			JOptionPane.showMessageDialog(null, msg, "Language Not Supported", JOptionPane.INFORMATION_MESSAGE);
			new GTLauncher(Locale.FRANCE);
		}
		
		if (!argsList.isEmpty()) {
			String filename = argsList.get(0);
			geoTortue.loadInBackground(new File(filename));
		}
	}
	
	private static void configBackground(ArrayList<String> args) {
		String configTag = "--background";
		int idx = args.indexOf(configTag);
		if (idx<0) 
			return;
		if (idx+1<args.size()) {
			String fileName = args.get(idx+1);
			if (fileName != null) {
					File f = new File(fileName);
					try {
						GTDisplayManager.setDefaultBackground(ImageIO.read(f));
					} catch (IOException ex) {
						ex.printStackTrace();
					}
			}
					
			System.out.println("Custom background file : "+fileName);
			args.remove(fileName);
		}
		args.remove(configTag);
	}

	private static Locale getLang(ArrayList<String> args) {
		String langTag = "--lang";
		int idx = args.indexOf(langTag);
		if (idx<0)
			return Locale.getDefault();
		if (idx+1<args.size()) {
			String lang = args.get(idx+1);
			System.out.println("Custom lang : "+lang);
			args.remove(langTag);
			args.remove(lang);
			return Locale.forLanguageTag(lang);
		}
		args.remove(langTag);
		return Locale.getDefault();
	}
}