package geotortue.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.Translator.TKey;
import fw.gui.FWDialog;
import fw.gui.FWTitledPane;
import fw.gui.layout.VerticalFlowLayout;
import geotortue.core.GTDocumentFactory.ProcedureDocument;
import geotortue.core.GTMessageFactory.GTTrouble;
import geotortue.gui.GTImagePane;


public class LibraryManager {

	private static final TKey PROCS = new TKey(LibraryManager.class, "procedures");
	private static final TKey LIBRARY = new TKey(LibraryManager.class, "library");
	private static final ActionKey ADD_TO_LIBRARY = new ActionKey(LibraryManager.class, "addToLibrary");
	private static final ActionKey ADD_TO_PROCS = new ActionKey(LibraryManager.class, "addToProcedures");
	private final JList<String> libraryList, procsList;
    private final FWAction action_addToLibrary, action_addToProcedures;
    private FWDialog dial ;
	/*
	 * 
	 */
	public LibraryManager(GTProcessingContext context, final GTDocumentFactory d){
		final ProcedureManager procedureManager = context.getProcedureManager();
		final Library library = context.getLibrary();
		final ProcedureDocument procedureDoc = d.getProcedureDocument();
		
		final DefaultListModel<String> libraryModel = new DefaultListModel<String>() {
			private static final long serialVersionUID = -6029699751467922703L;

			public int getSize() {
				return library.getSortedKeys().size();
			}

	        public String getElementAt(int i) {
	        	return library.getSortedKeys().elementAt(i);
	        }
	    };
	    
	    final DefaultListModel<String> proceduresModel = new DefaultListModel<String>() {
			private static final long serialVersionUID = 6519529996575644796L;

			public int getSize() {
				return procedureManager.getSize();
			}

			public String getElementAt(int i) {
	        	return procedureManager.getSortedKeys().elementAt(i);
	        }
	    };
	    
		this.procsList = new JList<String>(proceduresModel);
		this.libraryList = new JList<String>(libraryModel);
		
		this.action_addToLibrary = new FWAction(ADD_TO_LIBRARY, "go-previous.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (d.isLocked()) {
					new GTException(GTTrouble.GTJEP_IS_RUNNING, dial).displayDialog();
					return;
				}
				List<String> values = procsList.getSelectedValuesList();
				for (String value : values) {
					Procedure p = procedureManager.getProcedure(value);
					library.add(p);
					String key = p.getKey();
					libraryModel.addElement(key);
					procedureDoc.remove(p);
				}
				
				procsList.setSize(procsList.getPreferredSize());
				libraryList.setSize(libraryList.getPreferredSize());
				
				procsList.setSelectedIndices(new int[]{});
				
				action_addToLibrary.setEnabled(procedureManager.getSize()!=0);
				action_addToProcedures.setEnabled(true);
			}
		});
		
		this.action_addToProcedures = new FWAction(ADD_TO_PROCS, "go-next.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (d.isLocked()) {
					new GTException(GTTrouble.GTJEP_IS_RUNNING, dial).displayDialog();
					return;
				}
				List<String> values = libraryList.getSelectedValuesList();
				for (String value : values) {
					Procedure p = library.getProcedure(value);
					library.remove(p);
					procedureDoc.append(p);
					proceduresModel.addElement(p.getKey());
				}
				
				procsList.setSize(procsList.getPreferredSize());
				libraryList.setSize(libraryList.getPreferredSize());
				
				libraryList.setSelectedIndices(new int[]{});
				action_addToLibrary.setEnabled(true);
				action_addToProcedures.setEnabled(!library.isEmpty());
			}
		});
		
		libraryList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2)
					action_addToProcedures.actionPerformed(null);
			}
		});

		procsList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2)
					action_addToLibrary.actionPerformed(null);
			}
		});
		
		action_addToLibrary.setEnabled(procedureManager.getSize()!=0);
		action_addToProcedures.setEnabled(!library.isEmpty());
	}
	
	
	
	public void showDialog(Window owner){
		JPanel contentPane = new JPanel(new GridLayout());
		
		JButton addToLibraryButton = new JButton(action_addToLibrary);
		JButton addToProceduresButton = new JButton(action_addToProcedures);
		addToProceduresButton.setHorizontalTextPosition(SwingConstants.LEFT);
		JPanel buttonsPane = VerticalFlowLayout.createPanel(12, 12, addToLibraryButton, new GTImagePane(), addToProceduresButton);
		buttonsPane.setBackground(Color.WHITE);
		contentPane.add(new FWTitledPane(LIBRARY, new JScrollPane(libraryList)));
		contentPane.add(buttonsPane);
		contentPane.add(new FWTitledPane(PROCS, new JScrollPane(procsList)));
		
		dial = new FWDialog(owner, LIBRARY, contentPane, false, true);
		dial.setModal(true);
		dial.setMinimumSize(new Dimension(500, 430));
		dial.setSize(700, 430);
		dial.setLocationRelativeTo(owner);
		dial.setVisible(true);
	}
	
}