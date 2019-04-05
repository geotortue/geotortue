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
	
	private static final TKey LOADING = new TKey(GTLauncher.class, "loading");
	private static final TKey READY = new TKey(GTLauncher.class, "ready");
	
	
	// TODO : z nouveautés sur le site
	// TODO : z lien vers interviews
	// TODO : z lang -> english
	// TODO : (done) cercle _arc + point
	// TODO : (done) ne pas demander de validation à la sortie du coloriage si rien n'a été fait.
	// TODO : (done) définir une image de fond en ligne de commande avec l'option --background chemin_vers_l_imag
	// TODO : (done) épaisseur du crayon + point
	// TODO : (done) proxy
	// TODO : export html : "boucle i de 1 à 10 [...]", le i n'est pas colorié, idem pour la boucle pour_chaque
	// TODO : export pstricks : j'imagine que la gestion des commandes "point" et "mine" est dans ta liste de "todo"...
	
	// TODO : (done) g.fill(Shape) +g.draw(Shape) En ce qui concerne les artefacts entre les triangles, j'ai remarqué que ceux-ci disparaissent si on ôte l'anticrénelage, mais dans ce cas les bords de la figure deviennent crénelés. Je ne sais pas si ça peut vous aider.

	// TODO : (done) J'ai aussi remarqué que les liens hypertextes de l'aide ne semblent pas fonctionner : ci-dessous un exemple avec bloc de commande dans l'aide de la commande "rep".
	// TODO : (done) commande  "stop" -> sort des boucles
	// TODO : test_remplis_poly_non_convexe.trt
	// TODO : (done) connecteurls logiques et / ou / non
	// TODO : (done) placer les nombres à gauche de l'axe des ordonnées plutôt qu'à droite, ce qui serait plus conforme à ce qu'on rencontre habituellement ?
	// TODO : (done) j'ai remarqué que les signes - des ordonnées se confondent avec les pointillés de la grille. Peut-être faut-il foncer les nombres ?
	// TODO : (done) accès plus rapide à la grille et aux axes (préalablement réglés dans les préférences), 
	// TODO : (done) invisible : il est possible qu'un fichier xrt contienne une procédure utilisable par les élèves mais impossible à lire?
	// TODO : (done) select turtles to be imported in models 
	// TODO : (done) boucle = pour_chaque
	// TODO : (done) L.remove(value)  L.insert(index, object) L.append(object) L.count(value) remove
	// TODO : (done) bac à sable ! taille des caractères + icône dans bouton
	// TODO : (done) bac à sable texte sous les icônes
	
	// TODO : (done) que l’ouverture d’une session n’entraine pas automatiquement celle d’un dossier GeoTortue, est-ce possible ?
	// TODO : (done) définir où geotortue enregistre ses fichiers de configuration
	// TODO : (?) définir le dossier ouvert par défaut par geotortue pour l'ouverture et la sauvegarde de fichiers
	// TODO : (done) Bref, vivement une commande "chante" qui jouerait en direct (mais c'est pas terrible, j'avais déjà essayé avec Java, il y a des problèmes de régularité du tempo) ou mieux : qui produirait un fichier Midi.
	// TODO : global -> vérifier comportement
	// TODO : optionnally include sandbox in trt file (cf. hanoi)
	
	// TODO : quadrillage trop grand (100 par 100) il faudrait pouvoir choisir la taille des carreaux
	// TODO : patte avant droite de la tortue en rouge 

	
	private final static Color LIGHT_GREEN = new Color(0, 128, 32);
	public static GTSplash SPLASH;
	
	private static GeoTortue geoTortue;
	
	public static GTVersion VERSION;
	
	private static final boolean DEBUG_ON = !false;
	public static final boolean IS_BETA = !false;
	
	static {
		UIManager.put("ProgressBar.background", Color.WHITE);
		UIManager.put("ProgressBar.foreground", LIGHT_GREEN);
		UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
		UIManager.put("ProgressBar.selectionBackground", LIGHT_GREEN);
		UIManager.put("ProgressBar.border", BorderFactory.createEmptyBorder());
		UIManager.put("Slider.foreground", LIGHT_GREEN);
                
                try { // TUR
                  Enumeration<URL> resources = GTLauncher.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
                  if (!resources.hasMoreElements())
                      throw new Exception("Manifest not found");
                  while (resources.hasMoreElements()) {
                    Manifest manifest = new Manifest(resources.nextElement().openStream());
                    String configurationVersion = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
                    if (configurationVersion == null)
                      throw new Exception("Version not found");
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
		Language[] AVAILABLE_LANGUAGES = Language.getAvailableLanguages(); 
		for (int idx = 0; idx < AVAILABLE_LANGUAGES.length; idx++)
			if (lang.getLanguage().equals(AVAILABLE_LANGUAGES[idx].getLocale().getLanguage())) {
				new GTLauncher(lang);
				return ;
			}
		throw new LanguageNotSupportedException(lang);
	}
	
	public static void main(final String[] args) {
		//System.exit(0);
		
		ArrayList<String> argsList = new ArrayList<>();
		for (int idx = 0; idx < args.length; idx++)
			argsList.add(idx, args[idx]);
		
		FWManager.setAccess(argsList);
		configBackground(argsList);
		Locale lang = getLang(argsList);
		
		try {
			SPLASH = new GTSplash();
			launch(lang);
		} catch (LanguageNotSupportedException ex) {
			String msg = "Sorry, but the language \""+lang+"\" is not yet supported.";
			msg += "\nLaunching it in french...";
			JOptionPane.showMessageDialog(null, msg, "Language Not Supported", JOptionPane.INFORMATION_MESSAGE);
			new GTLauncher(Locale.FRANCE);
		}
		
		if (argsList.size()>0) {
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