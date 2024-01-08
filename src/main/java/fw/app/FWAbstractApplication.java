package fw.app;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import fw.app.FWAction.ActionKey;
import fw.app.FWWorker.WKey;
import fw.app.header.FWMenuHeader;
import fw.app.prefs.FWFileCycle;
import fw.app.prefs.FWLocalPreferences;
import fw.files.FileUtilities.HTTPException;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.ANSWER;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.params.FWFileAssistant;
import fw.gui.params.FWFileAssistant.FKey;
import fw.gui.params.FWParameterListener;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLFile;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;

/**
 * 
 */
public abstract class FWAbstractApplication implements XMLCapabilities {

	private static final ActionKey LOAD_ACTION = new ActionKey(FWAbstractApplication.class, "load");
	private static final ActionKey SAVE_ACTION = new ActionKey(FWAbstractApplication.class, "save");
	private static final ActionKey SAVE_AS = new ActionKey(FWAbstractApplication.class, "saveAs");
	private static final ActionKey SHOW_CONSOLE = new ActionKey(FWAbstractApplication.class, "showConsole");
	private static final WKey STORE_USER_PREFS = new WKey(FWAbstractApplication.class, "storeUserPreferences", true);
	private static final WKey SAVE = new WKey(FWAbstractApplication.class, "save", true);
	private static final WKey LOAD = new WKey(FWAbstractApplication.class, "load", false);
	private static final OPTKey REQUEST_SAVING = new OPTKey(FWAbstractApplication.class, "requestSaving");
	
	protected final FWActionManager actionManager = new FWActionManager();
	private final JFrame frame;
	private FWMenuHeader menuHeader;
	private final FWFileAssistant workFile;
	private final FWFileCycle lastFiles = new FWFileCycle(this);
	

	private boolean requireSaving = false;
	private static int instanceCount = 0;

	protected FWAbstractApplication(final FKey key) {
		instanceCount++;
		frame = new JFrame(getTitle());
		workFile = new FWFileAssistant(getFrame(), key);
		workFile.addParamaterListener(new FWParameterListener<File>() {
			@Override
			public void settingsChanged(File value) {
				updateFrameTitle();
				lastFiles.put(value);
				FWLocalPreferences.synchronize();
				registerLastFilesActions();
			}
		});
	}
	
	/*
	 * ACTIONS
	 */

	protected enum BASICS {FILE, QUIT, CONSOLE}
	
	protected void registerActions() {
		actionManager.addAction(actionShowConsole);
		actionManager.addAction(actionQuit);
	};
	
	protected void registerFilesActions() {
		actionManager.addAction(actionLoad);
		actionManager.addAction(actionSave);
		actionManager.addAction(actionSaveAs);
		registerLastFilesActions();
	}
	
	private void registerLastFilesActions() {
		Vector<File> files = lastFiles.getFiles();
		for (int idx = 0; idx < files.size(); idx++) 
			actionManager.addAction(getLoadOldFileAction(idx + 1, files.elementAt(idx)));
		renewHeader();
	}
	
	private final FWAction actionLoad = new FWAction(LOAD_ACTION,
			InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_O, "document-open.png",
			a -> {
				FWAbstractApplication that = this;
				doIfAuthorized(() -> loadInBackground(that.workFile.getFileForLoading()));
			});

	private final FWAction actionSave = new FWAction(SAVE_ACTION,
			InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_S, "document-save.png",
			a -> {
				FWAbstractApplication that = this;
				saveInBackground(that.workFile.getFile());
			});

	private final FWAction actionSaveAs = new FWAction(SAVE_AS,
			InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_S, "document-save-as.png",
			a -> {
				FWAbstractApplication that = this;
				saveInBackground(that.workFile.getFileForSaving());
			});

	private final FWAction actionQuit = new FWAction(new ActionKey(FWAbstractApplication.class, "quit"),
			InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_Q, "system-log-out.png",
			a -> quit());

	private final FWAction actionShowConsole = new FWAction(SHOW_CONSOLE, "log_debug.png",
			a -> FWConsole.showSharedInstance());
	
	private FWAction getLoadOldFileAction(final int idx, final File f) {
		AbstractAction a = new AbstractAction(idx + " : " + f.getName()) {
			private static final long serialVersionUID = 1684840190060099720L;

			public void actionPerformed(ActionEvent a) {
				doIfAuthorized(() -> loadInBackground(f));
			}
		};

		ImageIcon icon = FWToolKit.getIcon("icon.png");
		if (icon != null) {
			a.putValue(Action.SMALL_ICON, icon);
		}
		a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_0 + idx);
		return new FWAction("*load." + idx, a);
	}

	/*
	 * I/O
	 */
	
	private void doIfAuthorized(final Runnable runnable) {
		if (requireSaving) {
			ANSWER answer = FWOptionPane.showConfirmDialog(frame, REQUEST_SAVING);
			switch (answer) {
			case YES:
				saveInBackground(workFile.getFile(), runnable);
				break;
			case NO:
				runnable.run();
				break;
			default:
			    // do nothing
			}
		} else {
			runnable.run();
		}
	}
	
	public void loadInBackground(final File file) {
		if (file == null) {
			return;
		}

		new FWWorker(LOAD, frame) {
			public void runInBackground() throws Exception {
				load(file);
				workFile.setValue(file);
			}
			
			@Override
			public void handleOutcome(Exception ex) {
				if (ex == null) {
					setRequireSaving(false);
					return;
				}

				try {
					inspectErrorOnLoading(ex);
				} catch (Exception ex2) {
					super.handleOutcome(ex2);
				}
			}
		};
	}
	
	protected void inspectErrorOnLoading(Exception ex) throws Exception {
		throw ex;
	}
	
	private void saveInBackground(final File file, final Runnable runnable) {
		if (file == null) {
			return;
		}
		new FWWorker(SAVE, frame) {
			public void runInBackground() throws Exception {
				save(file);
				workFile.setValue(file);
				setRequireSaving(false);
			}

			@Override
			protected void done() {
				super.done();
				if (!requireSaving)
					runnable.run();
			}
		};
	}
	
	private void saveInBackground(File file) {
		saveInBackground(file, () -> {});
	}
	
	protected abstract void load(File file) throws Exception;

	protected abstract void save(File file) throws Exception;

	// FRAME

	protected final void initFrame(JComponent mainPane) {
		SwingUtilities.updateComponentTreeUI(frame);

		updateFrameTitle();
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});

		// Layout
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(mainPane, BorderLayout.CENTER);
		frame.setIconImage(FWLauncher.ICON);

		renewHeader();

		frame.pack();
		frame.setBounds(2, 2, 1024, 730);
		//frame.setBounds(2, 2, 1020, 580); 
	}

	
	private void renewHeader() {
		@SuppressWarnings("java:S1604")
		Runnable doRun = new Runnable() {
			public void run() {
				try {
					XMLReader e = new XMLFile(FWManager.getHeaderURL()).parse();
					if (menuHeader != null) {
						frame.getContentPane().remove(menuHeader);
					}
					menuHeader = getMenuHeader(e, actionManager);
					frame.getContentPane().add(menuHeader, BorderLayout.NORTH);
					frame.validate();
				} catch (XMLException | IOException | HTTPException ex) {
					ex.printStackTrace();
				}
			}
		};
		SwingUtilities.invokeLater(doRun);
	}

	protected abstract FWMenuHeader getMenuHeader(XMLReader e, FWActionManager m) throws XMLException ;

	private void updateFrameTitle() {
		SwingUtilities.invokeLater(() -> {
				String suffix = requireSaving ? " *" : "";
				frame.setTitle(FWManager.getApplicationTitle() + " - " + workFile.getName() + suffix);
			}
		);
	}
	
	public final JFrame getFrame() {
		return frame;
	}

	private final String getTitle() {
		return FWManager.getApplicationTitle();
	}

	private void setRequireSaving(boolean b) {
		if (b != requireSaving) {
			requireSaving = b;
			updateFrameTitle();
		}
	}

	protected void displayFrame() {
		setRequireSaving(false);
		@SuppressWarnings("java:S1604")
		Runnable doRun = new Runnable() {
			@Override
			public void run() {
				frame.setVisible(true);
				if (!FWConsole.isDebugModeEnabled())
					frame.setExtendedState(Frame.MAXIMIZED_BOTH);
				frame.validate();
			}
		};
		SwingUtilities.invokeLater(doRun);
	}

	private void quit() {
		@SuppressWarnings({"java:S1604", "java:S2696"})
		Runnable doRun = new Runnable() {
			public void run() {
				FWLocalPreferences.synchronize();
				frame.dispose();
				instanceCount--;
				if (instanceCount <= 0) {
					System.exit(0);
				} else {
					System.gc();
				}
			}
		};
		doIfAuthorized(doRun);
	}


	/*
	 * XML
	 */

	protected final void loadXMLProperties() {
		try {
			loadXMLProperties(new XMLFile(new File(FWManager.getConfigDirectory(), "properties.xml")).parse());
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			loadDefaultXMLProperties();
		} catch (FileNotFoundException ex) {
			FWConsole.printWarning(this, "No user prefs : loading defaults");
			loadDefaultXMLProperties();
		} catch (XMLException ex) {
			FWConsole.printWarning(this, "Obsolete user prefs : loading defaults instead");
			loadDefaultXMLProperties();
		} catch (FWRestrictedAccessException ex) {
			loadDefaultXMLProperties();
		} catch (IOException ex) {
			ex.printStackTrace();
			loadDefaultXMLProperties();
		}
	}

	private final void loadDefaultXMLProperties() {
		try {
			loadXMLProperties(new XMLFile(getClass().getResource("/cfg/properties.xml")).parse());
		} catch (IOException | HTTPException | XMLException ex) {
			ex.printStackTrace();
		}
	}

	private void storeXMLProperties() throws IOException, XMLException {
		try {
			final File file = new File(FWManager.getConfigDirectory(), "properties.xml");
			new XMLFile(getXMLProperties()).write(file);
		} catch (FWRestrictedAccessException ex) {
			// do nothing (?) before resuming
		}
	}

	public XMLWriter getXMLProperties() {
		final XMLWriter e = new XMLWriter(this);
		e.setAttribute("version", FWManager.getApplicationVersion());
		return e;
	}

	protected final void storeUserPreferences() {
		new FWWorker(STORE_USER_PREFS, frame) {
			public void runInBackground() throws Exception {
				storeXMLProperties();
				FWLocalPreferences.synchronize();
			}
		};
	}

	protected final void loadDefaultPreferences() {
		FWLocalPreferences.loadDefaults();
		loadDefaultXMLProperties();
		try {
			FWLocalPreferences.synchronize();
			storeXMLProperties();
		} catch (IOException | XMLException ex) {
			ex.printStackTrace();
		}
		registerLastFilesActions();
	}
	
	private DocumentListener docListener = new DocumentListener() {
		public void changedUpdate(DocumentEvent e) {
			// keep it
		}

		public void insertUpdate(DocumentEvent e) {
			setRequireSaving(true);
		}

		public void removeUpdate(DocumentEvent e) {
			setRequireSaving(true);
		}
	};
	
	
	protected void listenTo(Document doc) {
		doc.addDocumentListener(docListener);
		setRequireSaving(false);
	}
}