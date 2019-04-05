/**
 * 
 */
package geotortue.model;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.FWConsole;
import fw.app.FWLauncher;
import fw.app.FWWorker;
import fw.app.FWWorker.WKey;
import fw.app.Translator.TKey;
import fw.files.FWFileReader;
import fw.files.FileUtilities;
import fw.files.FileUtilities.HTTPException;
import fw.files.NoMoreEntryAvailableException;
import fw.gui.FWButton;
import fw.gui.FWButton.BKey;
import fw.gui.FWButton.FWButtonListener;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.FWTitledPane;
import fw.gui.layout.BasicLayoutAdapter;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLFile;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;
import geotortue.core.GeoTortue;

public class GTModelCatalog implements XMLCapabilities {

	private final static WKey INIT = new WKey(GTModelCatalog.class, "init", false);
	private static final TKey TITLE = new TKey(GTModelCatalog.class, "title");
	private static final TKey THEME = new TKey(GTModelCatalog.class, "theme");
	private static final TKey SEQUENCE = new TKey(GTModelCatalog.class, "sequence");
	private static final ActionKey CLOSE = new ActionKey(GTModelCatalog.class, "close");
	private static final BKey LOAD = new BKey(GTModelCatalog.class, "load");
	private static final OPTKey ERROR = new OPTKey(GTModelCatalog.class, "error");
	private static final OPTKey CATALOG_NOT_FOUND= new OPTKey(GTModelCatalog.class, "catalogNotFound");
	
//	private final String ROOT = UIManager.getString("models.catalog.dir");
	private final String root;

	private final GeoTortue geotortue;
	private boolean initialized = false;
	private final Vector<Sequence> sequences = new Vector<>();
	private final JList<Sequence> sequencesList;
	private GTModelManager manager;
	
	private Model currentModel;
	
	private JFrame frame;
	private JPanel buttonsPane;
	private JButton quitButton;
	
	public GTModelCatalog(GeoTortue geotortue, String root)  {
		this.geotortue = geotortue;
		this.root = root;
		final DefaultListModel<Sequence> sequenceModel = new DefaultListModel<Sequence>() {
			private static final long serialVersionUID = -6755487368320890758L;

			public int getSize() {
				return sequences.size();
			}

	        public Sequence getElementAt(int i) {
	        	return sequences.elementAt(i);
	        }
	    };
	    this.sequencesList = new JList<>(sequenceModel);
	    sequencesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    sequencesList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting())
					updateBrowser();
			}
		});
	}
	
	public GTModelCatalog(GeoTortue geotortue) {
		this(geotortue, "http://geotortue.free.fr/catalog4/");	// TODO : (done) catalog
	}

	private void init() {
		final JFrame owner = geotortue.getFrame();
		new FWWorker(INIT, owner) {
			@Override
			public void runInBackground() throws Exception {
				GTModelCatalog.this.manager = new GTModelManager(owner);
				URL url = new URL(root+"index.xml");
				XMLFile f = new XMLFile(url);
				loadXMLProperties(f.parse());
			}
	
			@Override
			public void handleOutcome(Exception ex) {
				if (ex == null) {
					initialized =true;
					new Thread(new Runnable() {
						public void run() {
							showBrowser();		
						}
					}).start(); 
					 
				} else if (ex instanceof HTTPException) {
					FWOptionPane.showErrorMessage(owner, ERROR, ex.getMessage());
				} else
					super.handleOutcome(ex);
			}
		};
	}

	public void show() {
		if (!initialized)
			init();
		else
			showBrowser();
	}
	
	private static HashMap<File, GTModelCatalog> localCatalogs = new HashMap<>();
	
	public static void show(GeoTortue geotortue, File path) {
		GTModelCatalog catalog = localCatalogs.get(path);
		if (catalog!=null) {
			catalog.show();
			return;
		}
		Window owner = geotortue.getFrame(); 
		try {
			String root = "file://"+path.getCanonicalPath()+"/";
			URL url = new URL(root+"index.xml"); 
			new XMLFile(url); // try to read index.xml
			catalog = new GTModelCatalog(geotortue, root);
			localCatalogs.put(path, catalog);
			catalog.show();
		} catch (HTTPException ex) {
			FWOptionPane.showErrorMessage(owner, ERROR, ex.getMessage());
		} catch (IOException ex) {
			FWOptionPane.showErrorMessage(owner, CATALOG_NOT_FOUND);
		} 
	}
	
	private void showBrowser(){
		if (frame!=null) {
			frame.setVisible(true);
			return;
		}
		frame = new JFrame(TITLE.translate());
		frame.setIconImage(FWLauncher.ICON);
		frame.setMinimumSize(new Dimension(500, 430));
		frame.setBounds(2, 2, 1024, 730);
		if (!FWConsole.isDebugModeEnabled())
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		FWAction quitAction = new FWAction(CLOSE, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
			}
		});
		quitButton = new JButton(quitAction);
		quitButton.getActionMap().put("quit", quitAction);
		quitButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "quit");
		
		FWButton loadButton = new FWButton(LOAD, new FWButtonListener() {
			@Override
			public void actionPerformed(ActionEvent e, JButton source) {
				load();
			}
		});
		
		buttonsPane = new JPanel();
		buttonsPane.add(loadButton);
		buttonsPane.add(quitButton);
		buttonsPane.setBackground(Color.WHITE);
		updateBrowser();
	}
	
	private void updateBrowser() {
		Sequence s = sequencesList.getSelectedValue();
		if (s==null) {
			sequencesList.setSelectedIndex(0);
			s = sequencesList.getSelectedValue();
		}
		Model model = s.modelsList.getSelectedValue();
		if (model == null) {
			s.modelsList.setSelectedIndex(0);
			model = s.modelsList.getSelectedValue();
		}
		if (model == currentModel)
			return ;
		
		currentModel = model;
			
		frame.getContentPane().removeAll();
		JPanel contentPane = new JPanel(new BorderLayout());
		
		JPanel mainPane = new JPanel(new ModelBrowserLayout());
		mainPane.add(new FWTitledPane(THEME, new JScrollPane(sequencesList)));
		mainPane.add(new FWTitledPane(SEQUENCE, new JScrollPane(s.modelsList)));
		mainPane.add(manager.getModelPane());
	
		contentPane.add(mainPane, BorderLayout.CENTER);
		contentPane.add(buttonsPane, BorderLayout.SOUTH);
		frame.setContentPane(contentPane);
		frame.validate();
		frame.setVisible(true);
		quitButton.requestFocusInWindow();

		try {
			FWFileReader sReader = new FWFileReader(model.getFile());
			XMLReader reader = sReader.getXMLReader("model.xml");
			manager.loadXMLProperties(reader);
			sReader.close();
		} catch(HTTPException ex) {
			FWOptionPane.showErrorMessage(getOwner(), ERROR, ex.getMessage());
		} catch (XMLException | IOException | NoMoreEntryAvailableException ex) {
			showError(ex);
		}
	}

	private void load() {
		Sequence sequence = sequencesList.getSelectedValue();
		if (sequence == null)
			return;
		Model model = sequence.modelsList.getSelectedValue();
		if (model == null)
			return;
		frame.setVisible(false);		
		try {
			geotortue.loadModel(model.getFile());
		} catch(HTTPException ex) {
			FWOptionPane.showErrorMessage(getOwner(), ERROR, ex.getMessage());
		} catch (IOException ex) {
			showError(ex);
		}
	}
	
	private void showError(Exception ex) {
		FWOptionPane.showErrorMessage(geotortue.getFrame(), ERROR, ex.getMessage());
		//ex.printStackTrace();
		if (localCatalogs.containsValue(this))
			localCatalogs.values().remove(this);
	}
	
	private Container getOwner() {
		if (frame != null && frame.isVisible())
			return frame;
		return geotortue.getFrame();
	}

	@Override
	public String getXMLTag() {
		return "GTModelBrowser";
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		while (child.hasChild(SEQUENCE_XML_TAG)) {
			sequences.add(new Sequence(child));
		}
		return child;
	}	
	
	@Override
	public XMLWriter getXMLProperties() {
		return null;
	}
	
	

	/*
	 * Sequence
	 */
	private static final XMLTagged SEQUENCE_XML_TAG = XMLTagged.Factory.create("Sequence");
	
	private class Sequence implements XMLCapabilities {
		private final Vector<Model> models = new Vector<>();
		private final JList<Model> modelsList;
		private String title;
		
		private Sequence(XMLReader e) {
			loadXMLProperties(e);
			
			final DefaultListModel<Model> modelsModel = new DefaultListModel<Model>() {
				private static final long serialVersionUID = -2774650357222054770L;

				public int getSize() {
					return models.size();
				}

		        public Model getElementAt(int i) {
		        	return models.elementAt(i);
		        }
		    };
		    
		    this.modelsList = new JList<>(modelsModel);
		    modelsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		    modelsList.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount()==2)
						load();
				}
			});
		    modelsList.setSelectedIndex(0);
		    modelsList.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!modelsList.getValueIsAdjusting())
						updateBrowser();
				}
			});
		}
		
		@Override
		public String getXMLTag() {
			return SEQUENCE_XML_TAG.getXMLTag();
		}

		@Override
		public XMLReader loadXMLProperties(XMLReader e) {
			XMLReader child = e.popChild(this);
			title = child.getAttribute("title", "?");
			while (child.hasChild(MODEL_XML_TAG)) {
				models.add(new Model(child));
			}
			return child;
		}	
		
		@Override
		public XMLWriter getXMLProperties() {
			return null;
		}
		
		@Override
		public String toString() {
			return " "+title+" ";
		}
	}

	private static final XMLTagged MODEL_XML_TAG = XMLTagged.Factory.create("Model");
	private class Model implements XMLCapabilities {
		private String path, title;

		private Model(XMLReader e) {
			loadXMLProperties(e);
		}
		
		private File getFile() throws IOException, HTTPException {
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			URL url = new URL(root+path);
			File f = FileUtilities.copy(url); 
			f.deleteOnExit();
			frame.setCursor(Cursor.getDefaultCursor());
			return f;
		}
		
		@Override
		public String getXMLTag() {
			return MODEL_XML_TAG.getXMLTag();
		}

		@Override
		public XMLReader loadXMLProperties(XMLReader e) {
			XMLReader child = e.popChild(this);
			path = child.getAttribute("path", "");
			title = child.getAttribute("title", "?");
			return child;
		}
		
		@Override
		public XMLWriter getXMLProperties() {
			return null;
		}

		@Override
		public String toString() {
			return " "+title+" ";
		}
	}
	
	private class ModelBrowserLayout extends BasicLayoutAdapter {

		
		public void layoutContainer(Container parent) {
			init(parent);
			layoutModel(parent.getComponents());
		}
		
		int gap = 2;
		
		
		private void layoutModel(Component[] components) {
			int listW = 150;
			int graphicsW = Math.min(parentW-listW, 800);
			listW = Math.min(parentW-graphicsW, 300);
			
			Component c = components[0]; // list
			c.setBounds(currX, currY, listW, parentH/2-gap);
			currY += parentH/2+gap; 
			
			c = components[1]; // list
			c.setBounds(currX, currY, listW, parentH/2-gap);
			currX += listW;
			currY = insets.top;
			
			c = components[2]; // modelPane
			c.setBounds(currX, currY, graphicsW, parentH);
			}
	
		@Override
		public void layoutComponent(Component c, int idx) {}
	}
}