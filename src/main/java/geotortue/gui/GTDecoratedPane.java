package geotortue.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class GTDecoratedPane extends JPanel {

	private static final long serialVersionUID = -2200665558617780989L;

	private GTDecoratedPane() {
		super(new BorderLayout());
		add(new GTImagePane(), BorderLayout.WEST);
	}
	
	private GTDecoratedPane(String msg, int w) {
		this();
		HTMLTextPane textPane = new HTMLTextPane(msg, w);
		add(textPane, BorderLayout.CENTER);
		textPane.getMargin().right = 16;
		doLayout();
	}
	
	public GTDecoratedPane(String msg) {
		this(msg, 600);
	}

	public GTDecoratedPane(JPanel textPane){
		this();
		textPane.setBorder(BorderFactory.createLineBorder(Color.WHITE, 12));
		textPane.setBackground(Color.WHITE);
		Dimension d = textPane.getPreferredSize();
		d.width = Math.max(d.width, 300);
		d.height= Math.max(d.height, 200);
		textPane.setPreferredSize(d);
		add(textPane, BorderLayout.CENTER);
	}
}