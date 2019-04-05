package geotortue.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import fw.app.FWAbstractApplication;
import fw.app.FWAction;
import fw.app.FWActionManager;
import fw.app.FWConsole;
import fw.app.FWManager;
import fw.app.FWWorker;
import fw.app.FWWorker.FWWorkerWaitingFrameSupplier;
import fw.app.FWWorker.WKey;
import fw.app.Translator.TKey;
import fw.app.header.FWMenuBar.FWMenuTitles;
import fw.app.header.FWMenuBar.MKey;
import fw.app.header.FWMenuHeader;
import fw.app.prefs.FWDirectoryEntry;
import fw.files.FWFileReader;
import fw.files.FWFileWriter;
import fw.files.NoMoreEntryAvailableException;
import fw.gui.FWButton;
import fw.gui.FWButton.BKey;
import fw.gui.FWButton.FWButtonListener;
import fw.gui.FWDialog;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.ANSWER;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.FWTabbedPane;
import fw.gui.layout.VerticalFlowLayout;
import fw.gui.params.FWFileAssistant;
import fw.gui.params.FWFileAssistant.FKey;
import fw.renderer.core.RendererListener;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.GTLauncher;
import geotortue.core.GTDocumentFactory.ProcedureDocument;
import geotortue.gallery.Gallery;
import geotortue.geometry.GTGeometryManager;
import geotortue.geometry.proj.GTPerspectiveManager;
import geotortue.gui.GTImagePane;
import geotortue.gui.GTMenuHeader;
import geotortue.gui.GTPanel;
import geotortue.gui.GTPanel.LAYOUT_TYPE;
import geotortue.gui.GTPreferences;
import geotortue.model.GTModelCatalog;
import geotortue.model.GTModelEditor;
import geotortue.model.GTModelManager;
import geotortue.model.SharedProcedures;
import geotortue.painter.GTPainter;
import geotortue.renderer.GTGraphicSpace;
import geotortue.renderer.GTRendererManager;
import geotortue.sandbox.GTSandBox;


public class GeoTortue extends FWAbstractApplication implements FWSettings {
	
	private static final MKey MENU_FILES = new MKey(GeoTortue.class, "files");
	private static final MKey MENU_EDIT = new MKey(GeoTortue.class, "edition");
	private static final MKey MENU_WINDOW = new MKey(GeoTortue.class, "window");
	private static final MKey MENU_PAINT = new MKey(GeoTortue.class, "paint");
	private static final MKey MENU_GALLERY = new MKey(GeoTortue.class, "gallery");
	private static final MKey MENU_LIBRARY = new MKey(GeoTortue.class, "library");
	private static final MKey MENU_MODEL = new MKey(GeoTortue.class, "model");
	private static final MKey MENU_TOOLS = new MKey(GeoTortue.class, "tools");
	private static final MKey MENU_HELP = new MKey(GeoTortue.class, "help");
	private static final BKey RESTORE_DEFAULTS = new BKey(GeoTortue.class, "restoreDefaultPreferences");
	private static final BKey STORE_USER_PREFS = new BKey(GeoTortue.class, "storeUserPreferences");
	private static final TKey ADVANCED = new TKey(GeoTortue.class, "advanced");

	private static final TKey PREFERENCES = new TKey(GeoTortue.class, "preferences");
	private static final TKey SETTINGS = new TKey(GeoTortue.class, "settings");
	private static final WKey LOAD_MODEL = new WKey(GeoTortue.class, "loadModel", false);
	private static final WKey MERGE_FILE = new WKey(GeoTortue.class, "merge", false);
	private static final OPTKey INCOMPATIBLE_FILE = new OPTKey(GeoTortue.class, "IncompatibleFileException");
	private static final OPTKey FILE_NOT_FOUND = new OPTKey(GeoTortue.class, "FileNotFoundException");
	private static final OPTKey OBSOLETE_FILE = new OPTKey(GeoTortue.class, "ObsoleteFileFormat");
	private static final OPTKey OVERWRITE_PROC = new OPTKey(GeoTortue.class, "overwriteProc");
	private static final OPTKey LOAD_MODEL_IN_NEW = new OPTKey(GeoTortue.class, "loadModelInNewWindow");
	private static final FKey TRT_XRT_EXT = new FKey(GeoTortue.class, new String[]{"trt", "xrt"});
	private static final FKey TRT_EXT = new FKey(GeoTortue.class, new String[]{"trt"});
	private static final FKey XRT_EXT = new FKey(GeoTortue.class, new String[]{"xrt"});
	
	@Override
	public TKey getTitle() {
		return ADVANCED;
	}
	// TODO : z bigdecimal ?
	// TODO : z zoom avec une image en fond 
	// TODO : z virer X3D // voir le logiciel utilisé par sage plot3d 
	// TODO : z anneaux borroméens
	// TODO : tours de Hanoi
	// TODO : web :  ajouter incosolata-g + deja vu sans dans les emprunts sur le site
	// TODO : z article sur les polyèdres (apmep ?)
	// TODO : web
	// www.maths-et-tiques.fr/telech/TortueLycee.pdf Yvan Monka – Académie de Strasbourg
	// https://www.pedagogie.ac-aix-marseille.fr/jcms/c_305927/fr/geotortue
	// https://www.pedagogie.ac-aix-marseille.fr/jcms/c_305927/fr/geotortue
	// http://www.icem-pedagogie-freinet.org/geotortue
	// https://www.ac-paris.fr/portail/jcms/p1_1009199/geometrie-et-programmation-au-college-avec-geotortue
	// http://www.cafepedagogique.net/lexpresso/Pages/2015/06/09062015Article635694304860718008.aspx
	// http://www.acamus.net/index.php?option=com_content&view=article&id=516:fractales-avec-geotortue&catid=41&Itemid=219
	// http://revue.sesamath.net/spip.php?article791
	// https://www.youtube.com/watch?v=CYz29Mwn8lw
	// http://www.irem.univ-paris-diderot.fr/articles/stage_algo/
	// http://www.sudouest.fr/2015/04/21/pau-un-coding-gouter-pour-que-les-enfants-apprennent-a-coder-1899180-4725.php
	// http://www.letelegramme.fr/finistere/plonevez-porzay/ecole-le-recteur-prend-une-lecon-d-informatique-04-09-2015-10761465.php
	// http://scenari.irem.univ-mrs.fr/batchGen/Colloques%20IREM/tice2014/Fr%C3%A9d%C3%A9ric%20Clerc/htmlPopNG/co/sequence.html
	// https://www.youtube.com/watch?v=vICng0RYN4E
	// https://www.youtube.com/watch?v=wQugqdgJXsE
	// https://www.youtube.com/watch?v=yYyBs1U85iA
	// https://www.youtube.com/watch?v=CYz29Mwn8lw
	// https://www.youtube.com/watch?v=vICng0RYN4E
	// http://maths4ever.blog4ever.com/pyramide-de-sierpinski-programmation-avec-geotortue
	// www.apmep.fr/IMG/pdf/13-KentzelV_C.pdf
	// TODO : Accents dans les commandes : il y a une incohérence à ce niveau puisque on a une commande nommée "écris" et une autre "rep" 
	// TODO : (done) concaténation des chaînes avec +
	// TODO : av fw vw AV (LOGO sans accent) 
	// TODO : web expliquer comment fabriquer la fonciton cap
	// TODO : inconsolata-g moche -> meilleure version ?
	// TODO : (done) taille de la police des boutons / menus / etc.
	// TODO : sandbox2.xml icônes
	// TODO : (done) bac à sable intégré dans les fichiers trt
	// TODO : (done) séparer prefs.xml (entries -> Préférences) et properties.xml (params -> Réglages)
	// TODO : (done) # comments
	// TODO : (done) compatibilité avec Java 7
	// TODO : (done) lire / écrire / importer fichiers en ligne de commande
	// TODO : j'ai modifié l'appel par défaut (sous linux) avec --directory ~/.local/share pour le stocker au même endroit que tous les logiciels récents... 
	// Et par conséquent cela donner .local/share/.geotortue, soit un dossier caché dans un autre dossier caché alors que l'intérêt de cette normalisation avec .local/share est justement d'avoir l'ensemble des dossiers de données des logiciels (dossiers non cachés donc) sous les yeux etc.
	//	Bref serait-il possible, par exemple, que dans la prochaine version
	// l'argument --directory permette de décider *complétement* de l'emplacement où stocker les données ? 
	// Cela donnerait en ce qui me concerne --directory ".local/share/geotortue" par exemple. 
	// Le nom .geotortue pourrait rester la valeur par défaut.
	
	// TODO : activités demi-cercle : activer cercle et arc

	private final KeywordManager keywordManager;
	private final TurtleManager turtleManager;
	
	private final GTGeometryManager geometryManager;
	private final GTDisplayManager displayManager;
	private final GTPerspectiveManager perspectiveManager;	
	private final GTRendererManager rendererManager;
	private final GTGraphicSpace graphicSpace;

	private final GTPainter painter;
	private final Gallery gallery;
	
	private final GTProcessingContext processingContext;
	
	private final GTDocumentFactory docFactory;
	private final GTSandBox sandBox;
	private final GTActions actions;
	private final GTModelManager modelManager;
	private final GTModelCatalog onlineCatalog;
	
	private final GTPanel mainPane;
	private LAYOUT_TYPE layout = LAYOUT_TYPE.SANDBOX;
	private final GTPreferences preferences;

	private final OldFileRefactor oldFileRefactor = new OldFileRefactor();
		
	public GeoTortue() {
		super(TRT_XRT_EXT);
		keywordManager = new KeywordManager();
		GTLauncher.SPLASH.setValue(60);
		turtleManager = new TurtleManager(keywordManager);
		geometryManager  = new GTGeometryManager();
		displayManager = new GTDisplayManager(geometryManager, turtleManager);
		perspectiveManager = new GTPerspectiveManager();
		rendererManager = new GTRendererManager(displayManager);
		graphicSpace = new GTGraphicSpace(geometryManager, rendererManager, perspectiveManager);

		painter = new GTPainter();
		gallery = new Gallery(getFrame());
		GTLauncher.SPLASH.setValue(70);
			
		processingContext = new GTProcessingContext(getFrame(), turtleManager, keywordManager, graphicSpace, geometryManager, displayManager);
		docFactory = new GTDocumentFactory(keywordManager, processingContext);
		sandBox = new GTSandBox(docFactory);
		actions = new GTActions(this, keywordManager, turtleManager, displayManager, rendererManager, graphicSpace, 
				painter, gallery, processingContext, docFactory, sandBox);
		GTLauncher.SPLASH.setValue(75);
		
		modelManager = new GTModelManager(getFrame());
		GTLauncher.SPLASH.setValue(85);
		onlineCatalog = new GTModelCatalog(this);
		modelManager.getModelPane().setWelcomeAssistant(new GTModelWelcomeAssistant());
		
		registerActions();
		
		rendererManager.addListener(new RendererListener() {
			@Override
			public void rendererChanged() {
				updateLayout(layout);
			}
		});
		
		JComponent monitor = processingContext.getMonitorPane();
		JComponent board = processingContext.getBoardPane();
		this.mainPane = new GTPanel(graphicSpace, modelManager, docFactory, 
				painter, sandBox, monitor, board, actions.getMakeProcedureAction());
		
		processingContext.setBoardAssistant(mainPane);
		
		initFrame(mainPane);
		
		//
		initWaitingFrameSupplier();
		
		
		// Preferences
		this.preferences = new GTPreferences(graphicSpace, turtleManager, processingContext);
		loadXMLProperties();
		GTLauncher.SPLASH.setValue(90);
		
		//
		updatePreferences();

		//
		displayFrame();
		GTLauncher.SPLASH.setValue(100);
		
		// DocumentListener
		listenTo(docFactory.getCommandDocument());
		listenTo(docFactory.getProcedureDocument());
	}
	
	private void initWaitingFrameSupplier() {
		FWWorker.setWaitingFrameSupplier(new FWWorkerWaitingFrameSupplier() {
			
			@Override
			public JPanel getContentPane(String title) {
				JProgressBar progressBar = new JProgressBar();
				progressBar.setIndeterminate(true);
				progressBar.setStringPainted(true);
				progressBar.setString(" "+title+" ");
				
				JPanel pane = new JPanel(new BorderLayout());
				pane.add(new GTImagePane(), BorderLayout.CENTER);
				pane.add(progressBar, BorderLayout.SOUTH);

				pane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
				return pane;
			}
		});
	}
	
	
	@Override
	protected FWMenuHeader getMenuHeader(XMLReader e, FWActionManager m) throws XMLException {
		FWMenuTitles titles = new FWMenuTitles();
		titles.add(MENU_FILES);
		titles.add(MENU_EDIT);
		titles.add(MENU_WINDOW);
		titles.add(MENU_PAINT);
		titles.add(MENU_GALLERY);
		titles.add(MENU_LIBRARY);
		titles.add(MENU_MODEL);
		titles.add(MENU_TOOLS);
		titles.add(MENU_HELP);
		return new GTMenuHeader(e, m, titles);
	}

	/*
	 * I/O
	 */

	private class ExceptionCollector {
		private Exception exception = null;

		private void setException(Exception ex) {
			if (exception == null)
				this.exception = ex;
		}
		
		private boolean hasError() {
			return exception != null;
		}
	}
	
	protected void inspectErrorOnLoading(Exception ex) throws Exception {
		try {
			super.inspectErrorOnLoading(ex);
		} catch (XMLException ex1) {
			FWOptionPane.showInformationMessage(getFrame(), OBSOLETE_FILE);
		} catch (FileNotFoundException ex1) {
			FWOptionPane.showInformationMessage(getFrame(), FILE_NOT_FOUND);
		} catch (NoMoreEntryAvailableException ex1) {
			FWOptionPane.showInformationMessage(getFrame(), INCOMPATIBLE_FILE);
		}
	}
	
	private final FWFileAssistant mergeFile = new FWFileAssistant(getFrame(), TRT_EXT);
	private FWTabbedPane preferencesPane, settingsPane;
	
	public void merge() {
		final File file = mergeFile.getFileForLoading();
		if (file==null)
			return;
		new FWWorker(MERGE_FILE, getFrame()) {
			@Override
			public void runInBackground() throws Exception {
				FWFileReader sReader = new FWFileReader(file);
				String str = "\n/*\n\t" + file.getName() + "\n*/\n\n";
				str += sReader.readText("procedures.txt");
				Document doc = docFactory.getProcedureDocument();
				doc.insertString(doc.getLength(), str, null);
			}
		};
	}

	@Override
	protected synchronized void load(final File file)  throws Exception {
		while (docFactory.isLocked()) {
			processingContext.interrupt();
			Thread.sleep(50);
		}
		
		if (file.getName().endsWith(".xrt")) {
			loadModel(file);
			return;
		}
		
		oldFileRefactor.init();
		
		final FWFileReader sReader = new FWFileReader(file);
		final ExceptionCollector collector = new ExceptionCollector();
		
		// procedures
		keywordManager.clearProcedures();
		//sReader.readText("procedures.txt");
		(docFactory.getProcedureDocument()).setText(sReader.readText("procedures.txt"));
		
		// commands
		try  {
			(docFactory.getCommandDocument()).setText(sReader.readText("commands.txt"));
			//sReader.readText("commands.txt");
		} catch (NoMoreEntryAvailableException ex) {
			sReader.close();
			updatePreferences();
			collector.setException(ex);
		}

		// properties
		XMLReader properties = null;
		try {
			properties = sReader.getXMLReader("properties.xml");
			if (properties != null) {
				XMLReader e = loadXMLProperties(properties);
				if (e.hasError())
					collector.setException(e.getException());
			}
		} catch (XMLException ex) {
			FWConsole.printWarning(this, ex.getMessage());
			collector.setException(ex);
		} catch (NoMoreEntryAvailableException ex) {
			collector.setException(ex);
		}
		

		// image
		try {
			BufferedImage image = sReader.readImage("background.png");
			if (image != null) {
				displayManager.setImage(image, file.getName());
			} else {
				displayManager.removeImage();
				sReader.reset();
				sReader.skip(3);
			}
			graphicSpace.repaint();
		} catch (NoMoreEntryAvailableException ex) {
			collector.setException(ex);
		}
		
		oldFileRefactor.update(docFactory);
		
		// library
		try {
			XMLReader libReader = sReader.getXMLReader("library.xml");
			Library lib = processingContext.getLibrary();
			lib.loadXMLProperties(libReader);
			oldFileRefactor.update(lib, keywordManager);
		} catch (XMLException | NoMoreEntryAvailableException ex) {
			collector.setException(ex);
		}

		sReader.close();
		
		// preferences
		updatePreferences();
		
		processingContext.init();
		actions.update(layout);
		
		if (collector.hasError())
			throw collector.exception;
	}

	@Override
	protected void save(File file) throws IOException {
		FWFileWriter sWriter = new FWFileWriter(file);
		// procedures
		sWriter.writeText(docFactory.getProcedureDocument().getText(), "procedures.txt");
		// commands
		sWriter.writeText(docFactory.getCommandDocument().getText(), "commands.txt");
		// properties
		try {
			sWriter.writeXML(this, "properties.xml");
		} catch (XMLException ex) {
			ex.printStackTrace();
		}
		// image
		BufferedImage bkgIm = displayManager.getImage();
		if (bkgIm != null)
			sWriter.writeImage(bkgIm, "background.png");
		// library
		try {
			sWriter.writeXML(processingContext.getLibrary(), "library.xml");
		} catch (XMLException ex) {
			ex.printStackTrace();
		}
		
		sWriter.close();
	}
	
	
	private void updatePreferences() {
		if (SwingUtilities.isEventDispatchThread())
			_updatePreferences_();
		else
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					_updatePreferences_();
					
				}
			});
		
	}
	
	private void _updatePreferences_() {
		this.settingsPane  = preferences.createPane(turtleManager, perspectiveManager, geometryManager, 
				displayManager, graphicSpace, rendererManager, processingContext, this, processingContext.getOptionalCommands());
		this.preferencesPane = preferences.createPane(keywordManager, displayManager, 
				processingContext, gallery);
	}

	public void showPreferencesPane() {
		FWDialog dial = new FWDialog(getFrame(), PREFERENCES, preferencesPane, false, false);
		dial.setModal(true);
		dial.setLocationRelativeTo(getFrame());
		dial.setVisible(true);
	}

	public void showSettingsPane() {
		FWDialog dial = new FWDialog(getFrame(), SETTINGS, settingsPane, false, false);
		dial.setModal(true);
		dial.setLocationRelativeTo(getFrame());
		dial.setVisible(true);
	}
	/*
	 * XML
	 */


	public String getXMLTag() {
		return "GeoTortue";
	}

	public XMLWriter getXMLProperties() {
		XMLWriter e = super.getXMLProperties();
		e.setAttribute("layout", layout.name().toLowerCase());
		e.put(processingContext);
		e.put(graphicSpace);
		e.put(geometryManager);
		e.put(perspectiveManager);
		e.put(displayManager);
		e.put(rendererManager);
		e.put(turtleManager);		
		return e;
	}

	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		
		try {
			String v = child.getAttribute("version");
			if (v.startsWith("3"))
				oldFileRefactor.requireUpdate();
		} catch (XMLException ex) {
		}
		
		turtleManager.loadXMLProperties(child);
		perspectiveManager.loadXMLProperties(child);
		geometryManager.loadXMLProperties(child);
		displayManager.loadXMLProperties(child);
		graphicSpace.loadXMLProperties(child);
		rendererManager.loadXMLProperties(child);
		processingContext.loadXMLProperties(child);
		if (child.hasChild(GTSandBox.XML_TAG))
			sandBox.loadXMLProperties(child);
		String layout = child.getAttribute("layout", "sandbox");
		LAYOUT_TYPE l;
		try {
			l = LAYOUT_TYPE.valueOf(layout.toUpperCase());
		} catch (IllegalArgumentException ex) {
			FWConsole.printWarning(this, "No such layout : "+layout);
			l = LAYOUT_TYPE.STANDARD;
		}
		updateLayout(l);
		
		return child;
	}
	
	
	/*
	 * FWS
	 */
	@Override
	public JPanel getSettingsPane(FWSettingsActionPuller actions) {
		FWButton restoreButton = new FWButton(RESTORE_DEFAULTS, new FWButtonListener() {
			public void actionPerformed(ActionEvent e, JButton source) {
				loadDefaultPreferences();
				((Window) source.getTopLevelAncestor()).dispose();
			}
		});

		FWButton storeButton = new FWButton(STORE_USER_PREFS, new FWButtonListener() {
			public void actionPerformed(ActionEvent e, JButton source) {
				storeUserPreferences();
			}
		});
		
		if (FWManager.isRestricted()) 
			storeButton.setEnabled(false);

		JPanel p = new JPanel();
		p.add(VerticalFlowLayout.createPanel(10, storeButton, restoreButton));
		return p; 
	}
	
	/*
	 * LAYOUTS
	 */
	public void updateLayout(LAYOUT_TYPE newLayout) {
		if (layout == LAYOUT_TYPE.PAINTER && newLayout != LAYOUT_TYPE.PAINTER )
			if (!actions.archivePix())
				return;
		
		if (newLayout == LAYOUT_TYPE.PAINTER)
			painter.setImage(graphicSpace.getImage());
		
		layout = newLayout;
		
		mainPane.setLayout(layout);
		actions.update(layout);		
	}
	
	public void setLayout(LAYOUT_TYPE newLayout) {
		if (newLayout == layout)
			return;
		updateLayout(newLayout);
	}
	
	public class GTModelWelcomeAssistant {
		private FWDirectoryEntry dir;
		
		public GTModelWelcomeAssistant() {
			this.dir = new FWDirectoryEntry(getFrame(), "catalog");
		}

		public void showOnlineCatalog() {
			onlineCatalog.show();
		}
		
		public void showLocalCatalog() {
			File f = dir.openFileChooser(getFrame());
			if (f!=null) 
				GTModelCatalog.show(GeoTortue.this, f);
		}
	}
	
	/*
	 * ACTIONS
	 */
	
	protected void registerActions() {
		super.registerActions();
		registerFilesActions();

		for (FWAction action : actions.getActions())
			actionManager.addAction(action);

		for (FWAction action : processingContext.getProcessorActions())
			actionManager.addAction(action);
	}


	/*
	 *	MODEL 
	 */
	private final FWFileAssistant modelFile = new FWFileAssistant(getFrame(), XRT_EXT);
	
	public void loadModel() {
		File file = modelFile.getFileForLoading();
		if (file!=null)
			loadModel(file);
	}
	
	public void loadModel(final File file) {
		new FWWorker(LOAD_MODEL, getFrame()) {
			@Override
			public void runInBackground() throws Exception {
				FWFileReader sReader = new FWFileReader(file);
				XMLReader reader = sReader.getXMLReader("model.xml");
				modelManager.loadXMLProperties(reader);
				sReader.close();
				updateLayout(LAYOUT_TYPE.MODEL);
			}

			@Override
			protected void done() {
				super.done();
				importSharedProcedures(file);
				turtleManager.addAll(modelManager.getSharedTurtles());
				try {
					geometryManager.loadXMLProperties(modelManager.getGeometry());
				} catch (XMLException e) {
					// should not occur
				}
				updatePreferences();
			}
		};
	}
	
	private void importSharedProcedures(File file) {
		Vector<ConflictingProcedure> conflicts = getConflicts();
		
		if (!conflicts.isEmpty()) {
			ANSWER answer = FWOptionPane.showConfirmDialog(getFrame(), LOAD_MODEL_IN_NEW);
			switch (answer) {
			case YES:
				new GeoTortue().loadModel(file);
				return;
			case NO:
				ANSWER confirmDeletion = FWOptionPane.showConfirmDialog(getFrame(), OVERWRITE_PROC, formatConflicts(conflicts));
				if (confirmDeletion == ANSWER.CANCEL)
					return;
				
				if (confirmDeletion == ANSWER.YES)
					for (ConflictingProcedure c : conflicts) 
						c.delete();
				break;
			default:
				return;
			}
		}
		
		SharedProcedures sharedProcedures = modelManager.getSharedProcedures();
		Library library = processingContext.getLibrary();
		for (Procedure proc : sharedProcedures.getLibraryProcedures()) 
			library.add(proc);
		
		ProcedureDocument procedureDoc = docFactory.getProcedureDocument();
		for (Procedure proc : sharedProcedures.getEditorProcedures()) 
				procedureDoc.append(proc);
		
		keywordManager.updateCompletionKeys();
	}
	
	private Vector<ConflictingProcedure>  getConflicts() {
		SharedProcedures sharedProcedures = modelManager.getSharedProcedures();
		Library library = processingContext.getLibrary();
		ProcedureManager procedureManager = processingContext.getProcedureManager();
		Vector<ConflictingProcedure> conflicts = new Vector<>();
		for (Procedure proc : sharedProcedures.getProcedures()) {
			String key = proc.getKey();
			Procedure old = library.getProcedure(key);
			if (old != null)
				conflicts.add(new ConflictingProcedure(old, Source.LIBRARY));
			old = procedureManager.getProcedure(key);
			if (old != null)
				conflicts.add(new ConflictingProcedure(old, Source.EDITOR));
		}
		return conflicts;
	}
	
	private static enum Source {LIBRARY, EDITOR};
	
	private class ConflictingProcedure {
		private final Procedure proc;
		private final Source source;

		private ConflictingProcedure(Procedure proc, Source src) {
			this.proc = proc;
			this.source = src;
		}
		
		private void delete() {
			Library library = processingContext.getLibrary();
			ProcedureDocument procedureDoc = docFactory.getProcedureDocument();
			if (source == Source.LIBRARY)
				library.remove(proc);
			else
				procedureDoc.remove(proc);
		}
	}
	private String formatConflicts(Vector<ConflictingProcedure> conflicts) {
		String s = "";
		for (ConflictingProcedure c : conflicts) {
			String htmlClass = (c.source == Source.LIBRARY) ? "library" : "procedure";
			s+= " &ndash; <span class=\""+htmlClass+"\">" + c.proc.getKey() + "</span><br/>";
		}
		return s;
	}
	
	public void editModels() {
		new GTModelEditor(turtleManager, perspectiveManager, geometryManager, displayManager, graphicSpace, rendererManager, processingContext);
	}
}