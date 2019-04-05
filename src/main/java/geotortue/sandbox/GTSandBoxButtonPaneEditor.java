/**
 * 
 */
package geotortue.sandbox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.Translator.TKey;
import fw.gui.FWTitledPane;
import fw.gui.layout.VerticalFlowLayout;
import geotortue.gui.GTImagePane;

class GTSandBoxButtonPaneEditor extends JPanel  {

	private static final long serialVersionUID = -7794398260416321471L;

	private final GTSandBoxButtonPane buttonPane;
	private final JList<GTSandBoxButton> buttonList;
	
	private static final ActionKey STEP_COMMAND_UP = new ActionKey(GTSandBoxButtonPaneEditor.class, "stepCommandUp");
	private static final ActionKey STEP_COMMAND_DOWN = new ActionKey(GTSandBoxButtonPaneEditor.class, "stepCommandDown");
	private static final ActionKey EDIT_COMMAND = new ActionKey(GTSandBoxButtonPaneEditor.class, "editCommand");
	private static final ActionKey CREATE_COMMAND = new ActionKey(GTSandBoxButtonPaneEditor.class, "createCommand");
	private static final ActionKey DELETE_COMMAND = new ActionKey(GTSandBoxButtonPaneEditor.class, "deleteCommand");
	private static final TKey BUTTON_LIST = new TKey(GTSandBoxButtonPaneEditor.class, "buttonList");

	
	GTSandBoxButtonPaneEditor(final Window owner, GTSandBoxButtonPane bp) {
		super(new BorderLayout());
		this.buttonPane = bp;
		
		final DefaultListModel<GTSandBoxButton> buttonListModel = new DefaultListModel<GTSandBoxButton>() {
			private static final long serialVersionUID = -3989563429462174957L;

			public int getSize() {
				return buttonPane.getComponentCount();
			}

	        public GTSandBoxButton getElementAt(int i) {
	        	return (GTSandBoxButton) buttonPane.getComponent(i);
	        }
	    };
	    
	    this.buttonList = new JList<GTSandBoxButton>(buttonListModel);
	    buttonList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    
	    buttonList.setCellRenderer(new ListCellRenderer<GTSandBoxButton>() {
			
			@Override
			public Component getListCellRendererComponent(JList<? extends GTSandBoxButton> list, GTSandBoxButton value,
					int index, boolean isSelected, boolean cellHasFocus) {

				final JPanel c = value.getCell();
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
	    
	    buttonList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
					int idx = buttonList.locationToIndex(e.getPoint());
					GTSandBoxButton b = (GTSandBoxButton) buttonPane.getComponent(idx);
					b.showButtonEditor(owner);
				} 
			}
		});
	    
	    add(new FWTitledPane(BUTTON_LIST, new JScrollPane(buttonList)), BorderLayout.CENTER);
	    
		/*
	     * Actions
	     */
	    final FWAction action_stepCommandUp = new FWAction(STEP_COMMAND_UP, "go-up.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GTSandBoxButton b = (GTSandBoxButton) buttonList.getSelectedValue();
				int newIndex = buttonList.getSelectedIndex() - 1;
				buttonPane.add(b, newIndex);
				buttonList.setSelectedIndices(new int[]{newIndex});
				buttonList.scrollRectToVisible(buttonList.getCellBounds(newIndex, newIndex));
				buttonPane.doLayout();
			}
		});
		

		final FWAction action_stepCommandDown = new FWAction(STEP_COMMAND_DOWN, "go-down.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GTSandBoxButton b = (GTSandBoxButton) buttonList.getSelectedValue();
				int newIndex = buttonList.getSelectedIndex() + 1;
				buttonPane.add(b, newIndex);
				buttonList.setSelectedIndices(new int[]{newIndex});
				buttonList.scrollRectToVisible(buttonList.getCellBounds(newIndex, newIndex));
				buttonPane.doLayout();
			}
		});
	    

		final FWAction action_editCommand= new FWAction(EDIT_COMMAND, "accessories-text-editor.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GTSandBoxButton b = (GTSandBoxButton) buttonList.getSelectedValue();
				if (b == null)
					return;
				b.showButtonEditor(owner);
			}
		});
	    
		
		final FWAction action_createCommand = new FWAction(CREATE_COMMAND, "list-add-32.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GTSandBoxButton b = buttonPane.newButton();
				buttonListModel.addElement(b);
				int newIndex = buttonPane.getComponentCount()-1;
				buttonList.setSelectedIndex(newIndex);
				buttonList.scrollRectToVisible(buttonList.getCellBounds(newIndex, newIndex));
				action_editCommand.actionPerformed(e);
				buttonList.repaint();
			}
		});
		
	    
		final FWAction action_deleteCommand = new FWAction(DELETE_COMMAND, "trash.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GTSandBoxButton b = (GTSandBoxButton) buttonList.getSelectedValue();
				if (b == null)
					return;

				int idx = buttonList.getSelectedIndex();
				buttonListModel.removeElement(b);
				buttonList.repaint();
				buttonList.setSelectedIndices(new int[]{});

				buttonPane.remove(b);
				buttonPane.validate();
				buttonPane.repaint();
				
				if (idx>=0 && idx < buttonPane.getComponentCount())
					buttonList.setSelectedIndex(idx);
			}
		});
	    
	    buttonList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int idx = buttonList.getSelectedIndex();
					action_stepCommandUp.setEnabled(idx>0);
					action_stepCommandDown.setEnabled(idx>=0 && idx < buttonPane.getComponentCount()-1);
					action_deleteCommand.setEnabled(idx!=-1);
					action_editCommand.setEnabled(buttonList.getSelectedIndices().length==1);
				}
			}
		});

	    action_stepCommandUp.setEnabled(false);
		action_stepCommandDown.setEnabled(false);
		action_createCommand.setEnabled(true);
		action_deleteCommand.setEnabled(false);
		action_editCommand.setEnabled(false);
	    		    
		JLabel label = new JLabel();
		label.setPreferredSize(new Dimension(175, 5));
		
		JButton stepUpB = new JButton(action_stepCommandUp);
		JButton stepDownB = new JButton(action_stepCommandDown);
		JButton editB = new JButton(action_editCommand);
		JButton createB = new JButton(action_createCommand);
		JButton deleteB = new JButton(action_deleteCommand);
		
		JPanel settingsPane = VerticalFlowLayout.createPanel(20, label, stepUpB,  stepDownB, new GTImagePane(),
				editB, createB, deleteB);
		settingsPane.setBackground(Color.WHITE);
		add(settingsPane, BorderLayout.EAST);
		
	}
}