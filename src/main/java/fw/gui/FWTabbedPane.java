/**
 * 
 */
package fw.gui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import fw.text.TextStyle;

/**
 * @author Salvatore Tummarello
 *
 */
public class FWTabbedPane extends JTabbedPane implements FWAccessible {
	private static final long serialVersionUID = 5027682180307283417L;


	public FWTabbedPane() {
		FWAccessibilityManager.register(this);
	}
	
	 public void add(Component component, String constraints) {
		 addTab(constraints, component);
		 setTabComponentAt(getTabCount()-1, new JLabel(constraints));
	 }

	@Override
	public void setFont(TextStyle s) {
		Font f = s.deriveFont(TextStyle.BOLD, 0);
		for (int idx = 0; idx < getTabCount(); idx++)  
				getTabComponentAt(idx).setFont(f);
	}

}
