/**
 * 
 */
package geotortue.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFrame;

import fw.app.FWAbstractApplication;
import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.FWActionManager;
import fw.app.header.FWMenuBar.FWMenuTitles;
import fw.app.header.FWMenuBar.MKey;
import fw.app.header.FWMenuHeader;
import fw.files.FWFileReader;
import fw.files.FWFileWriter;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.params.FWFileAssistant.FKey;
import fw.gui.tabs.FWTabs;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import geotortue.core.GTDisplayManager;
import geotortue.core.GTProcessingContext;
import geotortue.core.TurtleManager;
import geotortue.geometry.GTGeometryManager;
import geotortue.geometry.proj.GTPerspectiveManager;
import geotortue.renderer.GTGraphicSpace;
import geotortue.renderer.GTRendererManager;

/**
 *
 */
public class GTModelEditor extends FWAbstractApplication {
	
	private static final MKey MENU_FILES = new MKey(GTModelEditor.class, "files");
	private static final MKey MENU_EDIT = new MKey(GTModelEditor.class, "edition");
	private static final ActionKey REFRESH_TAB = new ActionKey(GTModelEditor.class, "refreshTab");
	private static final ActionKey MODEL_PREFS = new ActionKey(GTModelManager.class,"modelPreferences");
	private static final OPTKey NOT_XRT = new OPTKey(GTModelEditor.class, "notAnXrtFile");
	private static final FKey XRT_EXT = new FKey(GTModelEditor.class, new String[]{"xrt"});
	private static final ActionKey SHARE_PROCS = new ActionKey(GTModelEditor.class, "shareProcs");
	private static final ActionKey EDIT_PROCS = new ActionKey(GTModelEditor.class, "editProcs");
	
	private final JFrame frame = new JFrame();
	private final GTModelManager modelManager;
	private final FWTabs<GTModel> tabs;
	private GTModelTabs modelTabs;
	
	public GTModelEditor(TurtleManager tm, GTPerspectiveManager pm, GTGeometryManager gm, GTDisplayManager dm, 
			GTGraphicSpace gs, GTRendererManager rm, GTProcessingContext pc) {
		super(XRT_EXT);
		this.modelManager = new GTModelManager(frame, tm, pm, gm, dm, gs, rm, pc);
		this.modelTabs = new GTModelTabs(modelManager);
		this.tabs = new FWTabs<GTModel>(modelTabs);
		registerActions();
		
	    initFrame(tabs);
		displayFrame();
		listenTo(modelTabs.getHtmlDoc());
	}

	@Override
	protected FWMenuHeader getMenuHeader(XMLReader e, FWActionManager m) throws XMLException {
		FWMenuTitles titles = new FWMenuTitles();
		titles.add(MENU_FILES);
		titles.add(MENU_EDIT);
		return new FWMenuHeader(e.popChild(this), m, titles);
	}
	
	@Override
	protected void registerActions() {
		super.registerActions();
		registerFilesActions();

		actionManager.addAction(action_refreshTab);
		actionManager.addAction(action_shareProcedures);
		actionManager.addAction(action_modelPreferences);
		actionManager.addAction(action_editProcedures);
	}
	
	private FWAction action_refreshTab = new FWAction(REFRESH_TAB, 0, KeyEvent.VK_F5, "view-refresh.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) { 
			modelTabs.refresh();
		}
	});
	
	private FWAction action_shareProcedures = new FWAction(SHARE_PROCS, 0,  KeyEvent.VK_F6, "link.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) { 
			modelManager.showSharingDialog();
		}
	});
	
	private FWAction action_modelPreferences = new FWAction(MODEL_PREFS, 0, KeyEvent.VK_F12, "preferences-system.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			modelManager.showPreferences();
		}
	});

	private FWAction action_editProcedures= new FWAction(EDIT_PROCS, 0, KeyEvent.VK_F7, "accessories-text-editor22.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			modelManager.editProcedures(getFrame());
		}
	});
	
	@Override
	protected void load(File file) throws Exception {
		if (!file.getName().endsWith(".xrt")) {
			FWOptionPane.showErrorMessage(frame, NOT_XRT);
			return;
		}
		final FWFileReader sReader = new FWFileReader(file);
		XMLReader reader = sReader.getXMLReader("model.xml");
		modelManager.loadXMLProperties(reader);
		sReader.close();
		this.modelTabs = new GTModelTabs(modelManager);
		tabs.setSupplier(modelTabs);
		listenTo(modelTabs.getHtmlDoc());
	}

	@Override
	protected void save(File file) throws Exception {
		FWFileWriter sWriter = new FWFileWriter(file);
		try {
			sWriter.writeXML(modelManager, "model.xml");
		} catch (XMLException ex) {
			ex.printStackTrace();
		}
		sWriter.close();
	}

	/*
	 * XML
	 */

	@Override
	public String getXMLTag() {
		return "GTModelEditor";
	} 
	
	public XMLReader loadXMLProperties(XMLReader e) {
		new Exception("This method should not be called").printStackTrace();
		return null;
	}	

}
