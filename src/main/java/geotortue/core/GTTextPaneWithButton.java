package geotortue.core;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fw.gui.FWButton;

public class GTTextPaneWithButton extends JLayeredPane {

	private static final long serialVersionUID = -3234940893490291494L;
	
	private final JScrollPane scrollPane;
	private final JButton[] buttons;
	private int size = 28;
	private boolean isVerticalScrollBarVisible = false;
	private boolean isHorizontalScrollBarVisible = false;
	
	public GTTextPaneWithButton(JTextPane textPane, JButton... bs) {
		this.scrollPane = new JScrollPane(textPane);
		this.buttons = bs;
		scrollPane.getViewport().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
            	boolean b = scrollPane.getVerticalScrollBar().isVisible();
            	if (isVerticalScrollBarVisible != b) {
            		isVerticalScrollBarVisible = b;
            		doLayout();
            	}
            	b = scrollPane.getHorizontalScrollBar().isVisible();
            	if (isHorizontalScrollBarVisible != b) {
            		isHorizontalScrollBarVisible = b;
            		doLayout();
            	}
            }
        });
	    add(scrollPane, new Integer(0));
	    for (JButton button : buttons) {
			button.setText("");
			button.setFocusable(false);
			FWButton.removeBackground(button);
			button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
			add(button, new Integer(1));
			moveToFront(button);
		}		
	    
	    validate();
	}
	
	 public void doLayout() {
		 int w = getWidth();
		 int h = getHeight();
		 scrollPane.setBounds(0, 0, w, h);
		 
		 if (isVerticalScrollBarVisible)
			 w -= scrollPane.getVerticalScrollBar().getWidth();
		 if (isHorizontalScrollBarVisible)
			 h -= scrollPane.getHorizontalScrollBar().getHeight();
		 for (int idx = 0; idx < buttons.length; idx++) {
			 buttons[idx].setBounds(w-size*(idx+1)-3, h-size-3, size, size);
		 }
     }
}