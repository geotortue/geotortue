package geotortue.gallery;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.Translator.TKey;
import fw.gui.FWDialog;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.ANSWER;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.FWTitledPane;
import fw.gui.layout.VerticalFlowLayout;
import geotortue.core.GeoTortue;
import geotortue.gui.GTImagePane;
import geotortue.gui.GTPanel.LAYOUT_TYPE;
import geotortue.painter.GTPainter;


public class GalleryOrganizer {
	
	private static final TKey DRAWINGS_LIST = new TKey(GalleryOrganizer.class, "drawingList");
	private static final TKey GALLERY = new TKey(GalleryOrganizer.class, "gallery");
	private static final TKey COMMENTS_EDITOR = new TKey(GalleryOrganizer.class, "commentsEditor");
	private static final ActionKey PRINT_PIX = new ActionKey(GalleryOrganizer.class, "printPix");
	private static final ActionKey DELETE_DRAWING = new ActionKey(GalleryOrganizer.class,  "deleteDrawing");
	private static final ActionKey IMPORT_DRAWING = new ActionKey(GalleryOrganizer.class,  "importDrawing");
	private static final ActionKey STEP_DRAWING_DOWN = new ActionKey(GalleryOrganizer.class,  "stepDrawingDown");
	private static final ActionKey STEP_DRAWING_UP = new ActionKey(GalleryOrganizer.class,  "stepDrawingUp");
	private static final OPTKey CONFIRM_DELETION = new OPTKey(GalleryOrganizer.class, "confirmDrawingDeletion");

	private final GeoTortue geotortue;
	private final JList<Drawing> galleryList;
	private final Gallery gallery;
	
	private final FWAction action_stepDrawingUp;
	private final FWAction action_stepDrawingDown; 
	private final FWAction action_importDrawing;
	private final FWAction action_deleteDrawing;
	private final FWAction action_commentDrawing;
	private final FWAction action_printDrawing;
	
	private FWDialog dial;

	public GalleryOrganizer(GeoTortue gt, Gallery g, final GTPainter painter) {
		this.geotortue = gt;
		this.gallery = g;
		
		final DefaultListModel<Drawing> galleryModel = new DefaultListModel<Drawing>() {
			private static final long serialVersionUID = -6029699751467922703L;

			public int getSize() {
				return gallery.getSize();
			}

	        public Drawing getElementAt(int i) {
	        	return gallery.getDrawingAt(i);
	        }
	    };
	    
	    this.galleryList = new JList<Drawing>(galleryModel);
	    galleryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    
	    galleryList.setCellRenderer(new ListCellRenderer<Drawing>() {
			
			@Override
			public Component getListCellRendererComponent(JList<? extends Drawing> list, Drawing d,
					int index, boolean isSelected, boolean cellHasFocus) {
				final JPanel c = d.getCell();
				if (isSelected) {
					c.setBorder(BorderFactory.createLoweredBevelBorder());
					c.setBackground(UIManager.getColor("List.selectionBackground"));
				} else {
					c.setBorder(UIManager.getBorder("List.border"));
					c.setBackground(UIManager.getColor("Panel.background"));
				}
				return c;
			}
		});
	    
	    galleryList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int idx = galleryList.getSelectedIndex();
					action_stepDrawingUp.setEnabled(idx>0);
					action_stepDrawingDown.setEnabled(idx < gallery.getSize()-1);
					action_importDrawing.setEnabled(idx!=-1);
					action_deleteDrawing.setEnabled(idx!=-1);
					action_printDrawing.setEnabled(idx!=-1);
					action_commentDrawing.setEnabled(galleryList.getSelectedIndices().length==1);
				}
			}
		});
	    
		this.action_stepDrawingUp = new FWAction(STEP_DRAWING_UP, "go-up.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Drawing d = (Drawing) galleryList.getSelectedValue();
				int newIndex = galleryList.getSelectedIndex() - 1;

				gallery.moveDrawingAt(d, newIndex);
				galleryList.setSelectedIndices(new int[]{newIndex});
				galleryList.scrollRectToVisible(galleryList.getCellBounds(newIndex, newIndex));
				
				galleryList.setSize(galleryList.getPreferredSize());
			}
		});
		
		this.action_stepDrawingDown = new FWAction(STEP_DRAWING_DOWN, "go-down.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Drawing d = (Drawing) galleryList.getSelectedValue();
				int newIndex = galleryList.getSelectedIndex() + 1;
				
				gallery.moveDrawingAt(d, newIndex);
				galleryList.setSelectedIndices(new int[]{newIndex});
				galleryList.scrollRectToVisible(galleryList.getCellBounds(newIndex, newIndex));
				
				galleryList.setSize(galleryList.getPreferredSize());
			}
		});
		
		this.action_importDrawing = new FWAction(IMPORT_DRAWING, "brush-icon.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Drawing d = (Drawing) galleryList.getSelectedValue();
				if (d == null)
					return;
				geotortue.setLayout(LAYOUT_TYPE.PAINTER);
				painter.setImage(d.getImage());
				dial.dispose();
			}
		});
		
		this.action_deleteDrawing = new FWAction(DELETE_DRAWING, "trash.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Drawing d = (Drawing) galleryList.getSelectedValue();
				if (d == null)
					return;
				ANSWER answer = FWOptionPane.showConfirmDialog(galleryList.getTopLevelAncestor(), CONFIRM_DELETION);
				if (answer == ANSWER.YES) {
					gallery.remove(d);
					galleryModel.removeElement(d);
					galleryList.setSelectedIndices(new int[] {});
					galleryList.setSize(galleryList.getPreferredSize());
				}
			}
		});
		
		
		this.action_commentDrawing = new FWAction(new ActionKey(GalleryOrganizer.class,  "commentDrawing"), "accessories-text-editor.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Drawing d = (Drawing) galleryList.getSelectedValue();
				if (d == null)
					return;
				showCommentsDialog(d);
			}
		});
		
		this.action_printDrawing = new FWAction(PRINT_PIX, "document-print-32x32.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Drawing d = (Drawing) galleryList.getSelectedValue();
				if (d == null)
					return;
				GTPrinter.print(d.getImage());
			}
		});

		action_stepDrawingUp.setEnabled(false);
		action_stepDrawingDown.setEnabled(false);
		action_importDrawing.setEnabled(false);
		action_deleteDrawing.setEnabled(false);
		action_commentDrawing.setEnabled(false);
		action_printDrawing.setEnabled(false);
		
		galleryList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
					int index = galleryList.locationToIndex(e.getPoint());
					Drawing d = gallery.getDrawingAt(index);
					showCommentsDialog(d);
				} 
			}
		});
	}
	
	public void showDialog(JFrame owner){
		JButton stepUpB = new JButton(action_stepDrawingUp);
		JButton stepDownB =new JButton(action_stepDrawingDown);
		JButton importB = new JButton(action_importDrawing);
		JButton deleteB = new JButton(action_deleteDrawing);
		JButton commentsB = new JButton(action_commentDrawing);
		JButton printB = new JButton(action_printDrawing);

		JLabel label = new JLabel();
		label.setPreferredSize(new Dimension(175, 10));
		
		JPanel settingsPane = VerticalFlowLayout.createPanel(-10, 12, label, stepUpB, stepDownB, new GTImagePane(),  commentsB, printB, importB, deleteB);
		settingsPane.setBackground(Color.WHITE);

		JPanel contentPane = new JPanel(new BorderLayout());		
		contentPane.add(settingsPane, BorderLayout.EAST);
		contentPane.add(new FWTitledPane(DRAWINGS_LIST, new JScrollPane(galleryList)), BorderLayout.CENTER);
		
		dial = new FWDialog(owner, GALLERY, contentPane, true, false);
		dial.setModal(true);
		dial.setSize(525, 700);
		dial.setLocationRelativeTo(owner);
		dial.setVisible(true);
	}

	private void showCommentsDialog(Drawing d) {
		JTextArea commentsEditor = d.getCommentsEditor();
		commentsEditor.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		FWDialog commentsDial = new FWDialog(dial, COMMENTS_EDITOR, commentsEditor, true, true) {
			private static final long serialVersionUID = 5742305892467262582L;
	
			@Override
			protected void close() {
				gallery.updateXMLIndex();
				dispose();
			}
		};
		commentsDial.setMinimumSize(new Dimension(400, 300));
		commentsDial.setModal(true);
		commentsDial.setVisible(true);
	}
}