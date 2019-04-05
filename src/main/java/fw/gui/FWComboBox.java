package fw.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import fw.text.TextStyle;


public class FWComboBox extends JComboBox<Object> implements ActionListener, FWAccessible {
	private static final long serialVersionUID = -63831124363366882L;

	private final FWComboBoxListener listener;
	
	public FWComboBox(Object[] objs, Object selectedObj, FWComboBoxListener l){
		super(objs);
		this.listener = l;
		addActionListener(this);
		setSelectedItem(selectedObj);
		FWAccessibilityManager.register(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		listener.itemSelected(getSelectedItem());
	}
	
	public interface FWComboBoxListener  {
		public void itemSelected(Object o);
	}

	@Override
	public void setFont(TextStyle s) {
		setFont(s.getFont());
	}
}
