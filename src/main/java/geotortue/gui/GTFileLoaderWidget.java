/**
 * 
 */
package geotortue.gui;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fw.app.Translator.TKey;
import fw.gui.FWButton;
import fw.gui.FWButton.FWButtonListener;
import fw.gui.layout.FixGridLayout;
import fw.gui.layout.HorizontalFlowLayout;

/**
 * @author Salvatore Tummarello
 *
 */
public abstract class GTFileLoaderWidget extends JPanel {
	private static final long serialVersionUID = 5756830928899325545L;

	private final JButton button1, button2;
	
	public GTFileLoaderWidget(JLabel label, TKey OPEN, String openIcon, TKey DELETE, String deleteIcon) {
		super(new HorizontalFlowLayout(10));
		add(label);

		JPanel buttonsPane = new JPanel(new FixGridLayout(2, 1, 0, 0, 30, 30));
		this.button1 = FWButton.createIconButton(OPEN, openIcon, new FWButtonListener() {
			@Override
			public void actionPerformed(ActionEvent e, JButton source) {
				open();
			}
		});
		this.button2 = FWButton.createIconButton(DELETE, deleteIcon, new FWButtonListener() {
			
			@Override
			public void actionPerformed(ActionEvent e, JButton source) {
				delete();
			}
		});
		
		FWButton.removeBackground(button1);
		FWButton.removeBackground(button2);
		
		buttonsPane.add(button1);
		buttonsPane.add(button2);
		
		add(buttonsPane);	
	}

	protected abstract void open();
	
	protected abstract void delete();

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		getComponent(0).setEnabled(enabled);
		button1.setEnabled(enabled);
		button2.setEnabled(enabled);
	}
	
	
}
