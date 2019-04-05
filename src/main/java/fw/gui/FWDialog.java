package fw.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.FWLauncher;
import fw.app.Translator.TKey;


public class FWDialog extends JDialog {
	private static final long serialVersionUID = -4236633243895033463L;
	private static final ActionKey CLOSE = new ActionKey(FWDialog.class, "close");
	private static final ActionKey CANCEL = new ActionKey(FWDialog.class, "cancel");
	
	public FWDialog(Window owner, TKey title, JComponent mainPane, boolean scroll, boolean addCancelButton, JButton... extraButtons){
		super(owner);

		setIconImage(FWLauncher.ICON);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		FWAction defaultAction = new FWAction(CLOSE, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				validationPerformed();
			}
		});
		
		FWAction quitAction = new FWAction(CANCEL, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		
		FWButton okButton = defaultAction.getButton();
		
		okButton.getActionMap().put("default", okButton.getAction());
		okButton.getActionMap().put("quit", quitAction);

		okButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "default");

		okButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "quit");
		
		JPanel contentPane = new JPanel(new BorderLayout());
		
		JPanel southPanel = new JPanel(new SouthLayout());
		southPanel.add(okButton);
		if (addCancelButton) 
			southPanel.add(quitAction.getButton());
		
		if (extraButtons != null)
			for (int idx = 0; idx < extraButtons.length; idx++) 
				southPanel.add(extraButtons[idx]);
			
		contentPane.add(southPanel, BorderLayout.SOUTH);
		
		if (scroll) { 
			JScrollPane scrollPane = new JScrollPane(mainPane);
			scrollPane.getVerticalScrollBar().setUnitIncrement(16);
			contentPane.add(scrollPane, BorderLayout.CENTER);
		} else {
			contentPane.add(mainPane, BorderLayout.CENTER);
		}
		
		setContentPane(contentPane);
		
		pack();
		
		Dimension d = getPreferredSize();
		d.width += 50;
		d.height = Math.min(d.height, 720);
		setPreferredSize(d);

		pack();
		setTitle(title.translate());

		setModal(true);
		setLocationRelativeTo(owner);
		okButton.requestFocusInWindow();
	}
	
	protected void validationPerformed() {
		close();
	}
	
	protected void close() {
		dispose();
	}
	
	private class SouthLayout extends FlowLayout {

		private static final long serialVersionUID = -7560417964155048970L;

		@Override
		public void layoutContainer(Container parent) {
			synchronized (parent.getTreeLock()) {
				Component[] components = parent.getComponents();
				if (components.length !=2) {
					super.layoutContainer(parent);
					return;
				}
				
				Component lc = components[0];
				Component rc = components[1];
				
				Dimension ld = lc.getPreferredSize();
				Dimension rd = rc.getPreferredSize();
				int w = Math.max(ld.width, rd.width);
				int h = Math.max(ld.height, rd.height);
				
				int gap = 20;
				int x = (parent.getWidth() - 2*w - gap)/2;
				int y = (parent.getHeight() - h)/2;
				
				lc.setBounds(x, y, w, h);
				rc.setBounds(x+w+gap, y, w, h);
			}
		}
	}
}