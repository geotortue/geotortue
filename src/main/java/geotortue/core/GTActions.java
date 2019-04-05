/**
 * 
 */
package geotortue.core;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.FWWorker;
import fw.app.FWWorker.WKey;
import fw.app.Language;
import fw.app.LanguageNotSupportedException;
import fw.app.Translator;
import fw.app.Translator.TKey;
import fw.app.prefs.FWLocalPreferences;
import fw.app.prefs.FWWritableDirectoryEntry.UninitializedDirectoryException;
import fw.gui.FWLabel;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.ANSWER;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.FWServices;
import fw.gui.FWTextField;
import fw.gui.FWTextForm;
import fw.gui.layout.VerticalFlowLayout;
import fw.gui.params.FWBoolean;
import fw.gui.params.FWFileAssistant;
import fw.gui.params.FWFileAssistant.FKey;
import geotortue.GTLauncher;
import geotortue.GTUpdateChecker;
import geotortue.core.GTMessageFactory.GTTrouble;
import geotortue.core.Procedure.ProcedureParsingException;
import geotortue.gallery.Drawing.DrawingIOException;
import geotortue.gallery.GTPrinter;
import geotortue.gallery.Gallery;
import geotortue.gallery.GalleryOrganizer;
import geotortue.geometry.FrameSupport;
import geotortue.gui.GTCodePane;
import geotortue.gui.GTDialog;
import geotortue.gui.GTPanel.LAYOUT_TYPE;
import geotortue.gui.GTSplash;
import geotortue.painter.GTPainter;
import geotortue.renderer.GTGraphicSpace;
import geotortue.renderer.GTGraphicSpace.HDExportTool;
import geotortue.renderer.GTPSTRenderer;
import geotortue.renderer.GTRendererManager;
import geotortue.renderer.GTSVGRenderer;
import geotortue.sandbox.GTSandBox;

public class GTActions {
	
	private static final OPTKey NO_MAIL_AVAILABLE = new OPTKey(GTActions.class, "noMailAvailable");
	private static final OPTKey EMPTY_GALLERY = new OPTKey(GTActions.class, "emptyGallery");
	private static final TKey REPLACE_GET_REPLACEMENT = new TKey(GTActions.class, "replace.getReplacement");
	private static final TKey REPLACE_GET_TARGET = new TKey(GTActions.class, "replace.getTarget");
	private static final TKey REPLACE_IN_LIBRARY = new TKey(GTActions.class, "replaceInLibrary");
	private static final TKey SMART_REPLACE = new TKey(GTActions.class, "smartReplace");
	private static final TKey FIND_DESC = new TKey(GTActions.class, "find");
	private static final TKey LIBRARY_GET_PASSWORD = new TKey(GTActions.class, "library.getPassword");
	private static final TKey LIBRARY_CONFIRM_PASSWORD = new TKey(GTActions.class, "library.confirmPassword");
	private static final TKey LIBRARY_SET_PASSWORD = new TKey(GTActions.class, "library.setPassword");
	private static final ActionKey LOCALE = new ActionKey(GTActions.class, "locale");
	private static final ActionKey PALETTE = new ActionKey(GTActions.class, "palette");
	private static final ActionKey ABOUT = new ActionKey(GTActions.class, "about");
	private static final ActionKey REPORT_BUG = new ActionKey(GTActions.class, "reportBug");
	private static final ActionKey BROWSE_IREM = new ActionKey(GTActions.class, "browseIrem");
	private static final ActionKey CHECK_UPDATE = new ActionKey(GTActions.class, "checkUpdate");
	private static final ActionKey HELP = new ActionKey(GTActions.class, "help");
	private static final ActionKey EDIT_SANDBOX = new ActionKey(GTActions.class, "editSandBox");
	private static final ActionKey PREFERENCES = new ActionKey(GTActions.class, "preferences");
	private static final ActionKey SETTINGS = new ActionKey(GTActions.class, "settings");
	private static final ActionKey EXPORT_HD_ACTION = new ActionKey(GTActions.class, "exportHD");
	private static final ActionKey SHOW_PST = new ActionKey(GTActions.class, "showPst");
	private static final ActionKey EXPORT_SVG_ACTION = new ActionKey(GTActions.class, "exportSvg");
	private static final ActionKey SHOW_HMTL = new ActionKey(GTActions.class, "showHtml");
	private static final ActionKey DECREASE_FONTSIZE = new ActionKey(GTActions.class, "decreaseFontSize");
	private static final ActionKey INCREASE_FONTSIZE = new ActionKey(GTActions.class, "increaseFontSize");
	private static final ActionKey FIND = new ActionKey(GTActions.class, "find");
	private static final ActionKey REPLACE = new ActionKey(GTActions.class, "replace");
	private static final ActionKey REFRESH = new ActionKey(GTActions.class, "refresh");
	private static final ActionKey UNLOCK_LIBRARY = new ActionKey(GTActions.class, "unlockLibrary");
	private static final ActionKey LOCK_LIBRARY = new ActionKey(GTActions.class, "lockLibrary");
	private static final ActionKey MANAGE_LIBRARY = new ActionKey(GTActions.class, "manageLibrary");
	private static final ActionKey IMPORT_GALLERY_ACTION = new ActionKey(GTActions.class, "importGallery");
	private static final ActionKey ARCHIVE_GALLERY_ACTION = new ActionKey(GTActions.class, "archiveGallery");
	private static final ActionKey MANAGE_GALLERY_ACTION = new ActionKey(GTActions.class, "manageGallery");
	private static final ActionKey BROWSE_GALLERY_ACTION = new ActionKey(GTActions.class, "browseGallery");
	private static final ActionKey ADD_TO_GALLERY = new ActionKey(GTActions.class, "addToGallery");
	private static final ActionKey PRINT_PIX = new ActionKey(GTActions.class, "printPix");
	private static final ActionKey EXPORT_PIX = new ActionKey(GTActions.class, "exportPix");
	private static final ActionKey IMPORT_PIX = new ActionKey(GTActions.class, "importPix");
	private static final ActionKey CROP = new ActionKey(GTActions.class, "crop");
	private static final ActionKey UNDO_FLOOD = new ActionKey(GTActions.class, "undoFlood");
	private static final ActionKey MAGIC_WAND = new ActionKey(GTActions.class, "magicWand");
	private static final ActionKey PICK = new ActionKey(GTActions.class, "pick");
	private static final ActionKey FLOOD = new ActionKey(GTActions.class, "flood");
	private static final ActionKey TOGGLE_TURTLE_AXIS = new ActionKey(GTActions.class, "toggleTurtleAxis");
	private static final ActionKey TOGGLE_ORIGIN = new ActionKey(GTActions.class, "toggleOriginVisibility");
	private static final ActionKey CENTER_TURTLE = new ActionKey(GTActions.class, "centerTurtle");
	private static final ActionKey INIT_GRAPHICS = new ActionKey(GTActions.class, "initGraphics");
	private static final ActionKey PAINTER_LAYOUT = new ActionKey(GTActions.class, "painterLayout");
	private static final ActionKey EDITOR_LAYOUT = new ActionKey(GTActions.class, "editorLayout");
	private static final ActionKey MODEL_LAYOUT = new ActionKey(GTActions.class, "modelLayout");
	private static final ActionKey SANDBOX_LAYOUT = new ActionKey(GTActions.class, "sandboxLayout");
	private static final ActionKey EDIT_MODEL = new ActionKey(GTActions.class, "editModel");
	private static final ActionKey OPEN_MODEL = new ActionKey(GTActions.class, "openModel");
	private static final ActionKey CATALOG_ONLINE = new ActionKey(GTActions.class, "onlineCatalog");
	private static final ActionKey CATALOG_LOCAL = new ActionKey(GTActions.class, "localCatalog");
	private static final ActionKey MERGE = new ActionKey(GTActions.class, "merge");
	private static final ActionKey NEW = new ActionKey(GTActions.class, "new");
	private static final WKey EXPORT_HD = new WKey(GTActions.class, "exportHD", true);
	private static final WKey EXPORT_SVG = new WKey(GTActions.class, "exportSvg", true);
	private static final WKey IMPORT_GALLERY = new WKey(GTActions.class, "importGallery", true);
	private static final OPTKey ABORT_GALLERY_INIT = new OPTKey(GTActions.class, "abortGalleryInit");
	private static final WKey ARCHIVE_GALLERY = new WKey(GTActions.class, "archiveGallery", true);
	private static final WKey MANAGE_GALLERY = new WKey(GTActions.class, "manageGallery", false);
	private static final WKey BROWSE_GALLERY = new WKey(GTActions.class, "browseGallery", false);
	private static final OPTKey ARCHIVE_PIX = new OPTKey(GTActions.class, "archivePix");
	private static final OPTKey ADDED_TO_GALERY = new OPTKey(GTActions.class, "addedToGallery");
	private static final OPTKey REPLACE_ERROR = new OPTKey(GTActions.class, "replaceError");
	private static final FKey PNG_EXT = new FKey(GTActions.class, new String[]{"png", "jpg", "gif"});
	private static final FKey ZIP_EXT = new FKey(GTActions.class, new String[]{"zip"});
	private static final FKey SVG_EXT = new FKey(GTActions.class, new String[]{"svg"});
	private static final ActionKey TOGGLE_AXIS = new ActionKey(GTActions.class, "toggleAxis");
	private static final ActionKey TOGGLE_GRID = new ActionKey(GTActions.class, "toggleGrid");
	private static final ActionKey MAKE_PROC = new ActionKey(GTActions.class, "makeProcedure");
	private static final TKey MAKE_PROC_DESC = new TKey(GTActions.class, "makeProcedure.getTitle");

	private final JFrame owner;
	private final GeoTortue geotortue;
	private final KeywordManager keywordManager;
	private final TurtleManager turtleManager;
	
	private final GTDisplayManager displayManager;
	private final GTRendererManager rendererManager;
	private final GTGraphicSpace graphicSpace;

	private final GTPainter painter;
	private final Gallery gallery;
	private final GTProcessingContext processingContext;
	private final GTDocumentFactory docFactory;
	private final GTSandBox sandBox;
	
	private final Vector<FWAction> actions = new Vector<>();
	
	private final FWFileAssistant pixFile, zipFile, svgFile;
	
	
	public GTActions(GeoTortue geotortue, KeywordManager keywordManager, TurtleManager turtleManager, GTDisplayManager displayManager,
			GTRendererManager rendererManager, GTGraphicSpace graphicSpace, GTPainter painter, Gallery gallery,
			GTProcessingContext processingContext, GTDocumentFactory docFactory, GTSandBox sandBox) {
			super();
			this.geotortue = geotortue;
			this.owner = geotortue.getFrame();
			this.keywordManager = keywordManager;
			this.turtleManager = turtleManager;
			this.displayManager = displayManager;
			this.rendererManager = rendererManager;
			this.graphicSpace = graphicSpace;
			this.painter = painter;
			this.gallery = gallery;
			this.processingContext = processingContext;
			this.docFactory = docFactory;
			this.sandBox = sandBox;
			
			this.pixFile = new FWFileAssistant(owner, PNG_EXT);
			this.zipFile = new FWFileAssistant(owner, ZIP_EXT);
			this.svgFile = new FWFileAssistant(owner, SVG_EXT);
			
			painter.addListener(new GTPainter.Listener() {
				public void undoStatusUpdated(boolean b) {
					action_undoFlood.setEnabled(b);
				}
			});
			
			actions.add(action_new);
			actions.add(action_merge);
			
			actions.add(action_refresh);
			actions.add(action_find);
			actions.add(action_replace);
			actions.add(action_increaseFontSize);
			actions.add(action_decreaseFontSize);
			
			actions.add(action_openModel);
			actions.add(action_openOnlineCatalog);
			actions.add(action_openLocalCatalog);
			actions.add(action_editModel);
			
			actions.add(action_editorLayout);
			actions.add(action_painterLayout);
			actions.add(action_sandboxLayout);
			actions.add(action_modelLayout);
			
			actions.add(action_initGraphics);
			actions.add(action_centerTurtle);
			actions.add(action_toggleOriginVisibility);
			actions.add(action_toggleAxisVisibility);

			actions.add(action_crop);
			actions.add(action_flood);
			actions.add(action_pick);
			actions.add(action_magicWand);
			actions.add(action_undoFlood);
			actions.add(action_importPix);
			actions.add(action_exportPix);
			actions.add(action_printPix);

			actions.add(action_manageLibrary);
			actions.add(action_lockLibrary);
			actions.add(action_unlockLibrary);
			
			actions.add(action_addToGallery);
			actions.add(action_manageGallery);
			actions.add(action_browseGallery);
			actions.add(action_archiveGallery);
			actions.add(action_importGallery);
			
			
			actions.add(action_exportSvg);
			actions.add(action_showPst);
			actions.add(action_exportHD);
			actions.add(action_showHtml);
			actions.add(action_preferences);
			actions.add(action_settings);
			actions.add(action_editSandBox);
			
			actions.add(action_help);
			actions.add(action_browseIrem);
			actions.add(action_checkUpdate);
			actions.add(action_reportBug);
			actions.add(action_about);
			
			actions.add(action_palette);
			
			actions.add(action_locale);
			
			actions.add(action_toggleAxis);
			actions.add(action_toggleGrid);
			
			actions.add(action_makeProcedure);
	}

	public Collection<FWAction> getActions() {
		return actions;
	}
	
	public void update(final LAYOUT_TYPE layout) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				update_(layout);		
			}
		});
	}
	
	public void update_(LAYOUT_TYPE layout) {
		switch (layout) {
			case STANDARD:
				action_editorLayout.putValue(Action.SELECTED_KEY, true);
				break;
			case SANDBOX:
				action_sandboxLayout.putValue(Action.SELECTED_KEY, true);
				break;
			case PAINTER:
				action_painterLayout.putValue(Action.SELECTED_KEY, true);
				break;
			case MODEL:
				action_modelLayout.putValue(Action.SELECTED_KEY, true);
				break;
			default:
				break;
		}

		
		boolean isPainterLayout = (layout == LAYOUT_TYPE.PAINTER);
		action_crop.setEnabled(isPainterLayout);
		action_flood.setEnabled(isPainterLayout);
		action_pick.setEnabled(isPainterLayout);
		action_magicWand.setEnabled(isPainterLayout);
		action_undoFlood.setEnabled(isPainterLayout);
		action_importPix.setEnabled(isPainterLayout);
		action_exportPix.setEnabled(isPainterLayout);
		action_printPix.setEnabled(isPainterLayout);
		
		action_addToGallery.setEnabled(isPainterLayout);
		
		update();
	}
	
	private void update() {		
		boolean isLibraryLocked  = processingContext.getLibrary().isPasswordSet();
		action_manageLibrary.setEnabled(!isLibraryLocked );
		action_lockLibrary.setEnabled(!isLibraryLocked );
		action_unlockLibrary.setEnabled(isLibraryLocked );
		
		boolean hasFrameSupport = (processingContext.getGeometry().getFrameSupport() != null);
		action_toggleAxis.setEnabled(hasFrameSupport);
		action_toggleGrid.setEnabled(hasFrameSupport);
	}
	
	public boolean archivePix() {
		if (painter.hasNotBeenModified())
			return true;
		ANSWER answer = FWOptionPane.showConfirmDialog(owner, ARCHIVE_PIX);
		switch (answer) {
			case YES: 
				action_addToGallery.actionPerformed(null);
				return true;
			case NO :
				return true;
			default:
				return false;
		}
	}

	private FWAction action_new = new FWAction(NEW, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_N, 
			"document-new.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			FWLocalPreferences.synchronize();
			try {
				new GeoTortue();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	});

	private FWAction action_merge = new FWAction(MERGE, "format-indent-more.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			geotortue.merge();
		}
	});
	
	private FWAction action_openOnlineCatalog = new FWAction(CATALOG_ONLINE, "package_graphics_web.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			geotortue.new GTModelWelcomeAssistant().showOnlineCatalog();
		}
	});
	
	private FWAction action_openLocalCatalog = new FWAction(CATALOG_LOCAL, "package_graphics_local.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			geotortue.new GTModelWelcomeAssistant().showLocalCatalog();
		}
	});
	
	private FWAction action_openModel = new FWAction(OPEN_MODEL, "package_graphics.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			geotortue.loadModel();
		}
	});
	
	private FWAction action_editModel = new FWAction(EDIT_MODEL, "package_graphics_edit.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			geotortue.editModels();
		}
	});
	
	
	private FWAction action_sandboxLayout = new FWAction(SANDBOX_LAYOUT, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_F5, "sandbox.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			geotortue.setLayout(LAYOUT_TYPE.SANDBOX);
		}
	});
	
	private FWAction action_modelLayout = new FWAction(MODEL_LAYOUT,  KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_F8, "package_graphics.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			geotortue.setLayout(LAYOUT_TYPE.MODEL);
		}
	});
	
	private FWAction action_editorLayout = new FWAction(EDITOR_LAYOUT, KeyEvent.CTRL_DOWN_MASK,  KeyEvent.VK_F6, "settings.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			geotortue.setLayout(LAYOUT_TYPE.STANDARD);
		}
	});

	
	private FWAction action_painterLayout = new FWAction(PAINTER_LAYOUT, KeyEvent.CTRL_DOWN_MASK,  KeyEvent.VK_F7, "paint.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			geotortue.setLayout(LAYOUT_TYPE.PAINTER);
		}
	});

	
	private FWAction action_initGraphics = new FWAction(INIT_GRAPHICS, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_L, "view-init.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			graphicSpace.resetGeometry();
		}
	});
	
	
	private FWAction action_centerTurtle = new FWAction(CENTER_TURTLE, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_T, "turtle-find.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			graphicSpace.centerOn(processingContext.getFocusedTurtles().get(0));
		}
	});
	
	private FWAction action_toggleOriginVisibility = new FWAction(TOGGLE_ORIGIN, 0, KeyEvent.VK_F2, "origin-show.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			displayManager.toggleStartingPointVisibility();
			graphicSpace.repaint();
		}
	});
	
	private FWAction action_toggleAxisVisibility = new FWAction(TOGGLE_TURTLE_AXIS, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_F3, "axis-show.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			turtleManager.toggleAxisVisibility();
			graphicSpace.repaint();
		}
	});
	
//	private FWAction action_painterReset = new FWAction(new ActionKey(GTActions.class, "painterReset", KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_P, "applications-graphics.png", new ActionListener() {
//		public void actionPerformed(ActionEvent e) {
//			action_painterLayout.actionPerformed(e);
//			painter.setImage(graphicSpace.getImage());
//		}
//	});
	
	private FWAction action_flood = new FWAction(FLOOD, 0, KeyEvent.VK_F, "flood.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			painter.setMode(GTPainter.MOUSE_MODE.FLOOD);
		}
	});

	private FWAction action_pick = new FWAction(PICK, 0, KeyEvent.VK_G, "color-picker.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			painter.setMode(GTPainter.MOUSE_MODE.PICK);
		}
	});

	private FWAction action_magicWand = new FWAction(MAGIC_WAND, 0, KeyEvent.VK_H, "magic-wand.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			painter.setMode(GTPainter.MOUSE_MODE.MAGIC_WAND);
		}
	});

	private FWAction action_undoFlood = new FWAction(UNDO_FLOOD, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_Z, "edit-undo.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			painter.undo();
		}
	});
	
	private FWAction action_crop = new FWAction(CROP, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_C, "crop.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			painter.crop();
		}
	});

	private FWAction action_importPix = new FWAction(IMPORT_PIX, "image-open.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			File file = pixFile.getFileForLoading();
			if (file == null)
				return;
			try {
				painter.setImage(ImageIO.read(file));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	});
	
	private FWAction action_exportPix = new FWAction(EXPORT_PIX, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_E, "image-save.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			File file = pixFile.getFileForSaving();
			if (file!=null)
				try {
					ImageIO.write(painter.getImage(), "png", file);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
		}
	});
	
	
	private FWAction action_printPix = new FWAction(PRINT_PIX, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_P, "document-print.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			rendererManager.getClass();
			GTPrinter.print(painter.getImage());
		}
	});
	
	private FWAction action_addToGallery = new FWAction(ADD_TO_GALLERY, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_H, "camera.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				gallery.add(painter.getImage());
				FWOptionPane.showInformationMessage(owner, ADDED_TO_GALERY);
			} catch (DrawingIOException ex) {
				ex.printStackTrace();
			} catch (UninitializedDirectoryException ex) {
				FWOptionPane.showInformationMessage(owner, ABORT_GALLERY_INIT);
			}
		}
	});

	private FWAction action_browseGallery = new FWAction(BROWSE_GALLERY_ACTION, "archive.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (gallery.isEmpty()) {
				FWOptionPane.showErrorMessage(owner, EMPTY_GALLERY);
				return;
			}
			new FWWorker(BROWSE_GALLERY, owner) {
				public void runInBackground() throws Exception {
					gallery.createCells();
					try {
						URL url = gallery.compileHTMLFiles();
						Desktop.getDesktop().browse(url.toURI());
					} catch(UninitializedDirectoryException ex) {
						FWOptionPane.showInformationMessage(owner, ABORT_GALLERY_INIT); 
					} catch(IOException ex) {
						FWOptionPane.showErrorMessage(owner, EMPTY_GALLERY);
					}
				}
			};

		}
	});
	
	private FWAction action_manageGallery = new FWAction(MANAGE_GALLERY_ACTION, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_U, "protractor.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			new FWWorker(MANAGE_GALLERY, owner) {
				public void runInBackground() throws Exception {
					gallery.createCells();
				}
				
				public void handleOutcome(Exception ex) {
					if (ex!=null)
						super.handleOutcome(ex);
					else
						new GalleryOrganizer(geotortue, gallery, painter).showDialog(owner);
				}
			};
			
		}
	});
	
	private FWAction action_archiveGallery = new FWAction(ARCHIVE_GALLERY_ACTION, "archive-export.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (gallery.isEmpty()) {
				FWOptionPane.showErrorMessage(owner, EMPTY_GALLERY);
				return;
			}
			final File file = zipFile.getFileForSaving();
			if (file!=null)
				new FWWorker(ARCHIVE_GALLERY, owner) {
					@Override
					public void runInBackground() throws Exception {
						gallery.writeArchive(file);
					}
				};
		}
	});
	
	
	
	private FWAction action_importGallery = new FWAction(IMPORT_GALLERY_ACTION,	"archive-open.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			final File file = zipFile.getFileForLoading();
			if (file == null)
				return;
			
			new FWWorker(IMPORT_GALLERY, owner) {
				@Override
				public void runInBackground() throws Exception {
					gallery.openArchive(file);
				}
				
				public void handleOutcome(Exception ex) {
					if (ex instanceof UninitializedDirectoryException)
						FWOptionPane.showInformationMessage(owner, ABORT_GALLERY_INIT);
					else
						super.handleOutcome(ex);
				}
			};
		}
	});
	
	private FWAction action_manageLibrary = new FWAction(MANAGE_LIBRARY, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_B, "library.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			new LibraryManager(processingContext, docFactory).showDialog(owner);
		}
	});
	
	private FWAction action_lockLibrary = new FWAction(LOCK_LIBRARY, "library-lock.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			final Library library = processingContext.getLibrary();
			final JPasswordField passwordField_1 = new JPasswordField(30);
			final JPasswordField passwordField_2 = new JPasswordField(30);
			JPanel textPane = VerticalFlowLayout.createPanel(10, new FWLabel(LIBRARY_SET_PASSWORD), passwordField_1, 
					new FWLabel(LIBRARY_CONFIRM_PASSWORD), passwordField_2);
			
			GTDialog dial = new GTDialog(owner, LOCK_LIBRARY, textPane, true) {
				private static final long serialVersionUID = 7970244144143998467L;

				@Override
				protected void validationPerformed() {
					String p1 = new String(passwordField_1.getPassword());
					String p2 = new String(passwordField_2.getPassword());
					if (p1.length()==0 || p2.length()==0)
						return;
					if (p1.equals(p2)) {
						library.setPassword(p1);
						dispose();
						GTActions.this.update();
					} else {
						passwordField_1.setText("");
						passwordField_2.setText("");
						new GTException(GTTrouble.GTJEP_MISMATCHING_PASSWORD, this).displayTransientWindow();
					}
				}
			};
			dial.setVisible(true);
			passwordField_1.requestFocusInWindow();
		}
	});
	
	private FWAction action_unlockLibrary = new FWAction(UNLOCK_LIBRARY, "library-unlock.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			final Library library = processingContext.getLibrary();
			final JPasswordField passwordField = new JPasswordField(30);
			JPanel textPane = VerticalFlowLayout.createPanel(10, new FWLabel(LIBRARY_GET_PASSWORD), passwordField);

			GTDialog dial = new GTDialog(owner, UNLOCK_LIBRARY, textPane, true) {
				private static final long serialVersionUID = 7970244144143998467L;

				@Override	 
				protected void validationPerformed() {
					if (library.checkPassword(passwordField.getPassword())) {
						library.setPassword(null);
						dispose();
						GTActions.this.update();
					} else {
						passwordField.setText("");
						new GTException(GTTrouble.GTJEP_WRONG_PASSWORD, this).displayTransientWindow();
					}
				}
			};
			dial.setVisible(true);
			passwordField.requestFocusInWindow();
		}
	});
	
	private FWAction action_refresh = new FWAction(REFRESH, 0, KeyEvent.VK_F5, "view-refresh.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			docFactory.parseAndRefresh();
			graphicSpace.repaint();
			
			//Translator.dump();

			// DEBUGGING : refresh css 
//			URL url = getClass().getResource("/cfg/html/java_style.css"); 
//			StyleSheet styles;
//			try {
//				styles = new CSSFile(url).getStyleSheet();
//				new HTMLEditorKit().setStyleSheet(styles);
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
			
		}
	});
	
	
	
	private FWAction action_replace = new FWAction(REPLACE, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_R, "edit-find-replace.png", new ActionListener() {
		final FWTextForm jtfs = new FWTextForm(REPLACE_GET_TARGET, REPLACE_GET_REPLACEMENT);
		final FWBoolean replaceInLibrary = new FWBoolean("replaceInLibrary", true);
		final FWBoolean smartReplace = new FWBoolean("smartReplacement", true);
		{
			jtfs.add(new JLabel());
			
			JCheckBox libCB = replaceInLibrary.getComponent();
			libCB.setBackground(Color.WHITE);
			libCB.setText(REPLACE_IN_LIBRARY.translate());
			jtfs.add(libCB);
			
			jtfs.add(new JLabel());
			
			JCheckBox exactCB = smartReplace.getComponent();
			exactCB.setBackground(Color.WHITE);
			exactCB.setText(SMART_REPLACE.translate());
			jtfs.add(exactCB);
		}
		
		public void actionPerformed(ActionEvent e) {
			String text=docFactory.getSelectedText();
			if (text !=null)
				jtfs.setText(text, 0);
			final Library library = processingContext.getLibrary();
			replaceInLibrary.setEnabled(!library.isEmpty());
			
			GTDialog dial = new GTDialog(owner, REPLACE, jtfs, true) {
				private static final long serialVersionUID = 3269850159697041012L;
				
				public void validationPerformed() {
					String[] strs = jtfs.getStrings();
					String target = strs[0];
					String replacement = strs[1];
					if (target.length()<1 || replacement.length()<1)
						return;
					boolean smart = smartReplace.getValue(); 
					docFactory.replace(target, replacement, smart);
					if (!library.isEmpty() && replaceInLibrary.getValue()) 
						try {
							library.replace(target, replacement, smart);
						} catch (GTException | ProcedureParsingException ex){
							FWOptionPane.showErrorMessage(getOwner(), REPLACE_ERROR, target, replacement);
						}
					dispose();
				}
			};
			jtfs.requestFocusInWindow();
			dial.setVisible(true);
		}
	});
	
	private FWAction action_find= new FWAction(FIND, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_F, "edit-find.png", new ActionListener() {
		final FWTextField textField = new FWTextField("", 30);
		JPanel pane = VerticalFlowLayout.createPanel(new FWLabel(FIND_DESC, SwingConstants.LEFT), textField);
		public void actionPerformed(ActionEvent e) {
			String text = docFactory.getSelectedText();
			if (text !=null)
				textField.setText(text);
			GTDialog dial = new GTDialog(owner, FIND, pane, true) {
				private static final long serialVersionUID = -6953715325457261315L;

				public void validationPerformed() {
						String target = textField.getText();
						if (target.length()<1)
							return;
						docFactory.find(target);
						dispose();
				}
			};
			textField.setSelectionStart(0);
			textField.setSelectionStart(textField.getText().length());
			textField.requestFocusInWindow();
			dial.setVisible(true);
		}
	});
	
	private FWAction action_increaseFontSize = new FWAction(INCREASE_FONTSIZE, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_ADD, 
			"zoom-in.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			keywordManager.increaseFontSize();
		}
	});
	
	private FWAction action_decreaseFontSize = new FWAction(DECREASE_FONTSIZE, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_SUBTRACT, 
			"zoom-out.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			keywordManager.decreaseFontSize();
		}
	});
	
	private FWAction action_showHtml = new FWAction(SHOW_HMTL, "text-html.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String code = docFactory.getHtmlText();
			GTCodePane.showDialog(owner, code, SHOW_HMTL);
		}
	});
	
	private FWAction action_exportSvg = new FWAction(EXPORT_SVG_ACTION, "stock-tool-path.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			final File file = svgFile.getFileForSaving();
			if (file!=null)
				new FWWorker(EXPORT_SVG, owner) {
					@Override
					public void runInBackground() throws Exception {
						new GTSVGRenderer(graphicSpace.getRenderer(), displayManager).export(file);						
				}
			};
		}
	});
	
	private FWAction action_showPst = new FWAction(SHOW_PST, "pstricks.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String code = new GTPSTRenderer(graphicSpace.getRenderer(), displayManager).getCode();
			GTCodePane.showDialog(owner, code, SHOW_PST);
		}
	});
	
	private FWAction action_exportHD = new FWAction(EXPORT_HD_ACTION, "image-save-hd.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			final File file = pixFile.getFileForSaving();
			if (file==null)
				return;

			final HDExportTool exportTool = graphicSpace.getHdExportTool();

			GTDialog dial = new GTDialog(owner, EXPORT_HD_ACTION, exportTool.getSettingsPane(null), true) {
				private static final long serialVersionUID = 3269850159697041012L;

				public void validationPerformed() {
					new FWWorker(EXPORT_HD, owner) {
						@Override
						public void runInBackground() throws Exception {
							BufferedImage im = exportTool.getHDImage();
							ImageIO.write(im, "png", file);
						}
					};
					dispose();
				}
			};
			dial.setVisible(true);
		}
	});
	
	
	private FWAction action_preferences = new FWAction(PREFERENCES, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_F12, "preferences-desktop-font.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					geotortue.showPreferencesPane();
				}
			});
		}
	});

	private FWAction action_settings = new FWAction(SETTINGS, 0, KeyEvent.VK_F12, "preferences-system.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					geotortue.showSettingsPane();
				}
			});
		}
	});

	private FWAction action_editSandBox = new FWAction(EDIT_SANDBOX, "sandbox-editor.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					sandBox.showDialog(owner);
				}
			});
		}
	});
	
	private FWAction action_help = new FWAction(HELP, 0, KeyEvent.VK_F1, "help-browser.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			FWServices.openBrowser(owner, "http://geotortue.free.fr/index.php?page=aide");
		}
	});

	private FWAction action_browseIrem = new FWAction(BROWSE_IREM, "internet-web-browser.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			FWServices.openBrowser(owner, "http://www-irem.univ-paris13.fr/site_spip/spip.php?rubrique1");
		}
	});

	private FWAction action_checkUpdate = new FWAction(CHECK_UPDATE, "system-software-update.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			GTUpdateChecker.checkUpdateNow(owner);
		}
	});
	
	private FWAction action_reportBug = new FWAction(REPORT_BUG, "mail-message-new.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			new Thread() {
				public void run() {
					try {
						Desktop.getDesktop().mail(
								new URI("mailto", "geotortue@free.fr?SUBJECT= GéoTortue : report de bogue", null));
					} catch (IOException ex) {
						FWOptionPane.showErrorMessage(owner, NO_MAIL_AVAILABLE);
					} catch (URISyntaxException ex) {
						ex.printStackTrace();
					}
				}
			}.start();
		}
	});

	private FWAction action_about = new FWAction(ABOUT, "weather-clear.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String licence = "<html>Copyright (C) 2008-2017 S. Tummarello<br/>" +
					"This program is free software: you can redistribute it and/or modify it under the terms of the<br/>" +
					"GNU General Public License as published by the Free Software Foundation.</html>";
			new GTSplash(owner, ABOUT, licence, "http://geotortue.free.fr");
		}
	});
	
	private JDialog palette ;
	private FWAction action_palette= new FWAction(PALETTE, "nuancier.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (palette==null)
				palette = JColorChooser.createDialog(owner, PALETTE.translate(), false, 
						new JColorChooser(), null, null);
			palette.setLocationRelativeTo(owner);
			palette.setVisible(true);
		}
	});
	
	private FWAction action_locale = new FWAction(LOCALE, "preferences-desktop-locale.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Language[] langs_ = Language.getAvailableLanguages();
			Vector<Language> langs = new Vector<Language>();
			for (int idx = 0; idx < langs_.length; idx++)
				langs.add(langs_[idx]);
			Collections.sort(langs);
			
			
			final JList<Language> list = new JList<Language>(langs);

			for (Language l : langs) 
				if (l.getLocale().equals(Translator.getLocale()))
						list.setSelectedValue(l, true);			
			
			JPanel pane = VerticalFlowLayout.createPanel(list);
			
			GTDialog dial = new GTDialog(owner, LOCALE, pane, true) {
				private static final long serialVersionUID = 16435225772374968L;
	
				public void validationPerformed() {
					Language target = (Language) list.getSelectedValue();
					tryToLaunch(target);
					dispose();
				}
			};
			dial.setVisible(true);
		}
	});
	
	
	private void tryToLaunch(Language target) {
		final Locale lang = target.getLocale();
		if (lang.equals(Translator.getLocale()))
			return;
		new Thread(){
			@Override
			public void run() {
				try {
					GTLauncher.launch(lang);
				} catch (LanguageNotSupportedException ex) {
					ex.printStackTrace();
				}
			}
		}.start();
	}

	
	private FWAction action_toggleAxis = new FWAction(TOGGLE_AXIS, 0, KeyEvent.VK_F3, "toggle-axis.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			FrameSupport fs = processingContext.getGeometry().getFrameSupport();
			if (fs != null) {
				fs.toggleAxis();
				graphicSpace.repaint();
			}
		}
	});

	private FWAction action_toggleGrid = new FWAction(TOGGLE_GRID, 0, KeyEvent.VK_F4, "toggle-grid.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			FrameSupport fs = processingContext.getGeometry().getFrameSupport();
			if (fs != null) {
				fs.toggleGrid();
				graphicSpace.repaint();
			}
		}
	});
	
	private FWAction action_makeProcedure = new FWAction(MAKE_PROC, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_K, "wizard-hat.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			final JTextField textField = new JTextField(30);
			JPanel textPane = VerticalFlowLayout.createPanel(2, new FWLabel(MAKE_PROC_DESC), textField);
			//final Window owner = (Window) GTPanel.this.getTopLevelAncestor();
			
			GTDialog dial = new GTDialog(owner, MAKE_PROC, textPane, true) {
				private static final long serialVersionUID = 7970244144143998467L;
				
				@Override
				protected void validationPerformed() {
					String key = textField.getText();
					if (key.length() < 1)
						return;
					key = key.replace(' ', '_');
					try {
						docFactory.makeProcedure(owner, key);
						dispose();
					} catch (GTException ex) {
						ex.displayTransientWindow();
					}
					
				}
			};
			textField.requestFocusInWindow();
			dial.setVisible(true);
		}
	});


	public FWAction getMakeProcedureAction() {
		return action_makeProcedure;
	}
}
