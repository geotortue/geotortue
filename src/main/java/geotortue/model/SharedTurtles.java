/**
 * 
 */
package geotortue.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fw.app.Translator.TKey;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalPairingLayout;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.core.Turtle;
import geotortue.core.TurtleManager;

public class SharedTurtles implements XMLCapabilities, FWSettings {
	
	private static final TKey SHARING = new TKey(SharedTurtles.class, "sharing");
	private static final TKey SHARE = new TKey(SharedTurtles.class, "share.tooltip");
	
	private final TurtleManager turtleManager;

	private final ArrayList<Turtle> table = new ArrayList<>();

	public SharedTurtles(TurtleManager tm) {
		this.turtleManager = tm;
	}

	/**
	 * 
	 */
	public List<Turtle> getTurtles() {
		return table;
	}

	@Override
	public String getXMLTag() {
		return "SharedTurtles";
	}

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		for (Turtle t : turtleManager.getTurtles())
			if (table.contains(t))
				e.put(t);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		while (child.hasChild(Turtle.XML_TAG)) 
			table.add(new Turtle(child));
		return child;
	}

	@Override
	public TKey getTitle() {
		return SHARING;
	}

	@Override
	public JPanel getSettingsPane(FWSettingsActionPuller actions) {
		List<Turtle> turtles = turtleManager.getTurtles();
		int len = turtles.size();
		JComponent[] comps = new JComponent[2 * len];
		for (int idx = 0; idx < len; idx++) {
			Turtle t = turtles.get(idx);
			comps[2 * idx] = new JLabel("<html><span class=\"turtle\">" + t.getName() + "</span></html>");
			comps[2 * idx + 1] = getCheckBox(t);
		}
		return VerticalPairingLayout.createPanel(comps);
	}

	private JCheckBox getCheckBox(final Turtle t) {
		boolean isShared = table.contains(t);

		final JCheckBox checkBox = new JCheckBox("", isShared);
		checkBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				boolean isSelected = checkBox.isSelected();
				if (isSelected)
					table.add(t);
				else
					table.remove(t);
			}
		});
		
		checkBox.setSelected(isShared);
		checkBox.setToolTipText(SHARE.translate(t.getName()));
		
		return checkBox;
	}
}