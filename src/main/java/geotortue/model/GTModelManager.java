/**
 * 
 */
package geotortue.model;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.FWLauncher;
import fw.app.Translator.TKey;
import fw.gui.FWButton;
import fw.gui.FWButton.BKey;
import fw.gui.FWButton.FWButtonListener;
import fw.gui.FWDialog;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.ANSWER;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.FWSettings;
import fw.gui.FWTabbedPane;
import fw.gui.FWTitledPane;
import fw.gui.layout.HorizontalCenteredFlowLayout;
import fw.gui.layout.VerticalFlowLayout;
import fw.text.FWEnhancedTextPane;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLFile;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.core.GTCommandBundle;
import geotortue.core.GTCommandBundles;
import geotortue.core.GTDisplayManager;
import geotortue.core.GTDocumentFactory;
import geotortue.core.GTDocumentFactory.ProcedureDocument;
import geotortue.core.GTException;
import geotortue.core.GTMessageFactory.GTTrouble;
import geotortue.core.GTProcessingContext;
import geotortue.core.KeywordManager;
import geotortue.core.Library;
import geotortue.core.Procedure;
import geotortue.core.SourceLocalization;
import geotortue.core.Turtle;
import geotortue.core.TurtleManager;
import geotortue.geometry.GTGeometryManager;
import geotortue.geometry.proj.GTPerspectiveManager;
import geotortue.gui.GTPreferences;
import geotortue.renderer.GTGraphicSpace;
import geotortue.renderer.GTRendererManager;

/**
 *
 */
public class GTModelManager implements XMLCapabilities { 

	private static final OPTKey CONFIRM_DELETION = new OPTKey(GTModelManager.class, "confirmDeletion");
	private static final TKey SHARING = new TKey(GTModelManager.class, "sharing");
	private static final TKey PROCEDURES = new TKey(GTModelManager.class, "procedures");
	private static final TKey MODEL_PREFS = new ActionKey(GTModelManager.class,"modelPreferences");
	private static final ActionKey CANCEL = new ActionKey(GTModelManager.class, "cancelProcEdition");
	private static final BKey UPDATE = new BKey(GTModelManager.class, "updateLibrary");
	
	private final Vector<GTModel> models = new Vector<>();

	private Window owner;
	private final KeywordManager keywordManager;
	private final TurtleManager turtleManager;
	private final GTGeometryManager geometryManager;
	private final GTDisplayManager displayManager;
	private final GTRendererManager rendererManager;
	private final GTPerspectiveManager perspectiveManager;	
	private final GTGraphicSpace graphicSpace;
	private final GTProcessingContext context;
	private final Library library;
	private final GTModelPane modelPane;
	
	private SharedProcedures sharedProcedures;
	private SharedTurtles sharedTurtles;
	
	public GTModelManager(Window owner) {
		this.owner = owner;
		this.keywordManager = new KeywordManager();
		this.turtleManager = new TurtleManager(keywordManager);
		this.geometryManager  = new GTGeometryManager();
		this.displayManager = new GTDisplayManager(geometryManager, turtleManager);
		this.rendererManager = new GTRendererManager(displayManager);
		this.perspectiveManager = new GTPerspectiveManager();	
		this.graphicSpace = new GTGraphicSpace(geometryManager, rendererManager, perspectiveManager);
		this.library = new Library(keywordManager);
		//this.sharedProcedures = new SharedProcedures(library);
		this.context = GTProcessingContext.getModelInstance(owner, 
				turtleManager, keywordManager, graphicSpace, geometryManager, displayManager, library);
		this.modelPane = new GTModelPane(this);
	}
	
	// used in GTModelEditor 
	GTModelManager(Window owner, TurtleManager tm, GTPerspectiveManager pm, GTGeometryManager gm, GTDisplayManager dm, 
			GTGraphicSpace gs,  GTRendererManager rm, GTProcessingContext pc) {
		this(owner);
		copyData(tm, turtleManager);
		copyData(pm, perspectiveManager);
		copyData(gm, geometryManager);
		copyData(dm, displayManager);
		copyData(gs, graphicSpace);
		copyData(pc, context);
		for (Procedure p : pc.getProcedureManager().getAllProcedures()) 
			library.add(p);

		for (Procedure p : pc.getLibrary().getAllProcedures()) 
			library.add(p);
		
		this.sharedProcedures = new SharedProcedures(library);
		this.sharedTurtles= new SharedTurtles(turtleManager);
		
		models.add(new GTModel(keywordManager));
	}
	
	
	private void copyData(XMLCapabilities src, XMLCapabilities dest) {
		dest.loadXMLProperties(src.getXMLProperties().toReader());
	}

	public GTGraphicSpace getGraphicSpace() {
		return graphicSpace;
	}

	public void refreshGraphics(GTModel model) {
		context.interrupt();
		context.resetGeometry();
		context.vg();
		String text = model.getCommand();
		
		new GTException(GTTrouble.GTJEP_MODEL_EXCEPTION, owner).keep();
		GTCommandBundles bundles;
		SourceLocalization loc =  SourceLocalization.create(text, owner);
		try {
			bundles = GTCommandBundle.parse(loc);
			context.launchExecution(bundles, new Runnable() {
				@Override
				public void run() {}
			});
		} catch (GTException ex) {
			ex.displayDialog();
		}
	}
	
	public GTModelPane getModelPane() {
		return modelPane;
	}

	
	public void showPreferences() {
		GTPreferences preferences = new GTPreferences(graphicSpace, turtleManager, context);
		FWTabbedPane preferencesPane = preferences.createPane(turtleManager, perspectiveManager, geometryManager, 
				displayManager, graphicSpace, rendererManager, context, null, context.getOptionalCommands());
		FWDialog dial = new FWDialog(owner, MODEL_PREFS, preferencesPane, false, false);
		dial.setModal(true);
		dial.setVisible(true);
	}
	
	
	public GTModel addNewModel() {
		GTModel m = new GTModel(keywordManager);
		models.add(m);
		return m;
	}
	
	public Vector<GTModel> getModels() {
		return models;
	}



	public boolean remove(GTModel model) {
		ANSWER answer = FWOptionPane.showConfirmDialog(owner, CONFIRM_DELETION);
		if (answer == ANSWER.YES)
			return models.remove(model);
		return false;
	}

	
	public boolean move(GTModel model, int idx) {
		boolean b = models.remove(model);
		models.add(idx, model);
		return b;
	}
	
	public GTModel copy(GTModel model) {
		GTModel copy =  new GTModel(model);
		models.add(copy);
		return copy;
	}

	private JFrame frame = new JFrame(PROCEDURES.translate());
	
	void editProcedures(Window owner) {
		if (frame.isVisible())
			frame.requestFocusInWindow();
		else
			frame = new JFrame(PROCEDURES.translate());
		
		
		GTDocumentFactory docFactory = new GTDocumentFactory(keywordManager, context);
		ProcedureDocument doc = docFactory.getProcedureDocument();
		FWEnhancedTextPane textPane = docFactory.getProcedurePane();
		 
		
		final Collection<Procedure> procs = library.getAllProcedures();
		
		JPanel pane = new FWTitledPane(PROCEDURES, new JScrollPane(textPane));
		
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.add(pane, BorderLayout.CENTER);		
		
		FWAction cancelAction = new FWAction(CANCEL, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				
				updateLibrary(procs);	
			}
		});
		FWButton cancelButton = cancelAction.getButton();
		
		FWButton updateButton = new FWButton(UPDATE, new FWButtonListener() {
			@Override
			public void actionPerformed(ActionEvent e, JButton source) {
				frame.dispose();
				Vector<Procedure> ps = new Vector<>();
				ps.addAll(context.getProcedureManager().getAllProcedures());

				for (Procedure p : ps)
					library.add(p);
				
				context.getProcedureManager().clear();
				//doc.removeAll();
				
				sharedProcedures = new SharedProcedures(library);
				initDialog();
			}
		});
		
		textPane.getActionMap().put("quit", cancelAction);
		textPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "quit");
		
		JPanel buttonsPane = HorizontalCenteredFlowLayout.createPanel(updateButton, cancelButton);
		buttonsPane.setBackground(Color.WHITE);
		mainPane.add(buttonsPane , BorderLayout.SOUTH);
		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				updateLibrary(procs);
			}
		});

		frame.setContentPane(mainPane);
		frame.setIconImage(FWLauncher.ICON);

		frame.pack();
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
		
		docFactory.getProcedureDocument().refresh();
		
		for (Procedure p : procs) {
			library.remove(p);
			doc.append(p);
		}
		
		textPane.setCaretPosition(0);
	}
	
	private void updateLibrary(Collection<Procedure> procs) {
		for (Procedure p : procs) 
			library.add(p);
	}
	
	@Override
	public String getXMLTag() {
		return "GTModelManager";
	}

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.put(context);
		e.put(graphicSpace);
		e.put(geometryManager);
		e.put(perspectiveManager);
		e.put(displayManager);
		e.put(rendererManager);
		e.put(turtleManager);
		e.put(library);
		e.put(sharedProcedures);
		e.put(sharedTurtles);
		for (GTModel model : models) 
			e.put(model);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		context.interrupt();
		XMLReader child = e.popChild(this);
		turtleManager.loadXMLProperties(child);
		perspectiveManager.loadXMLProperties(child);
		geometryManager.loadXMLProperties(child);
		displayManager.loadXMLProperties(child);
		rendererManager.loadXMLProperties(child);
		graphicSpace.loadXMLProperties(child);
		context.loadXMLProperties(child);
		library.loadXMLProperties(child);
		sharedProcedures = new SharedProcedures(library);
		sharedProcedures.loadXMLProperties(child);
		sharedTurtles = new SharedTurtles(turtleManager);
		sharedTurtles.loadXMLProperties(child);
		initDialog();
		models.clear();
		while (child.hasChild(GTModel.XML_TAG))
			models.add(new GTModel(keywordManager, child));
		modelPane.init();
		return child;
	}
	
	public List<Turtle> getSharedTurtles() {
		return sharedTurtles.getTurtles();
	}

	public SharedProcedures getSharedProcedures() {
		return sharedProcedures;
	}

	public XMLReader getGeometry() throws XMLException {
		return new XMLFile(geometryManager).parse();
	}

	
	private FWDialog dial = null;
	
	public void showSharingDialog() {
		if (dial == null) 
			initDialog();
		
		dial.setVisible(true);
	}
	
	private void initDialog() {
		JPanel contentPane = VerticalFlowLayout.createPanel(
				getSettingsPane(sharedProcedures), 
				getSettingsPane(sharedTurtles));

		dial = new FWDialog(owner, SHARING, contentPane, true, true);
		dial.setModal(true);
		dial.setMinimumSize(new Dimension(500, 430));
		dial.setSize(700, 430);
		dial.setLocationRelativeTo(owner);
	}
	
	private JPanel getSettingsPane(FWSettings s) {
		JPanel p = s.getSettingsPane(null);
		p.setBorder(BorderFactory.createTitledBorder(s.getTitle().translate()));
		return p;
	}
}