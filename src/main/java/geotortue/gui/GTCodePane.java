package geotortue.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import fw.app.Translator.TKey;
import fw.gui.FWButton;
import fw.gui.FWButton.BKey;
import fw.gui.FWButton.FWButtonListener;
import fw.gui.FWDialog;
import fw.gui.layout.BasicLayoutAdapter;


public class GTCodePane extends JPanel {

	private static final BKey COPY = new BKey(GTCodePane.class, "copy");

	private static final long serialVersionUID = -6072999452541970536L;

	private int imW, imH;
	private int gap = 6;
	
	public GTCodePane(final String code) {
		setLayout(new Layout());
		setBackground(Color.WHITE);
		
		GTImagePane imgPane = new GTImagePane();
		add(imgPane);
		Dimension imgSize = imgPane.getPreferredSize();
		imW = imgSize.width;
		imH = imgSize.height;
		
		final JTextArea text = new JTextArea(code);
		text.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));		
		text.setEditable(false);
		
		
		JPanel textPane = new JPanel();
		textPane.setBackground(Color.WHITE);
		textPane.add(text);
		textPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		add(textPane);
		
		add(new FWButton(COPY, new FWButtonListener() {
			@Override
			public void actionPerformed(ActionEvent e, JButton source) {
				StringSelection contents = new StringSelection(code);
		        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		        clipboard.setContents(contents, null);
		        text.requestFocusInWindow();
		        text.selectAll();
			}
		}));
		
		Dimension d = text.getPreferredSize();
		d.width = Math.max(d.width+imW+3*gap+10, 300);
		d.height= Math.max(d.height+4*gap, 270);
		setPreferredSize(d);
	}
	
	public static void showDialog(Window owner, String code, TKey key) {
		new FWDialog(owner, key, new GTCodePane(code), true, false).setVisible(true);
	}

	private class Layout extends BasicLayoutAdapter {

		@Override
		public void layoutComponent(Component c, int idx) {
			if (idx==0) 
				c.setBounds(insets.left+gap, insets.top+gap, imW, imH);
			if (idx==2) 
				c.setBounds(insets.left+gap, insets.top+2*gap+imH, imW, c.getPreferredSize().height);
			if (idx==1) 
				c.setBounds(insets.left+imW+2*gap, insets.top+gap, 
						parentW-imW-3*gap, parentH-3*gap);
		}

	}
}
