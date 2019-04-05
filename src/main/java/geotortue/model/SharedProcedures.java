/**
 * 
 */
package geotortue.model;

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fw.app.Translator.TKey;
import fw.gui.FWComboBox;
import fw.gui.FWComboBox.FWComboBoxListener;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.HorizontalFlowLayout;
import fw.gui.layout.VerticalPairingLayout;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;
import geotortue.core.Library;
import geotortue.core.Procedure;

public class SharedProcedures implements XMLCapabilities, FWSettings {

	private static final TKey SHARE = new TKey(SharedProcedures.class, "share.tooltip");
	private static final TKey PROCS_MANAGER = new TKey(SharedProcedures.class, "procsManager");
	private static final XMLTagged SPROC_XML_TAG = XMLTagged.Factory.create("SProcedure");
	
	
	private static class Destination {
		private final String code;
		private final TKey key;
		private Destination(String code) {
			this.code = code;
			this.key = new TKey(SharedProcedures.class, code);
		}

		@Override
		public String toString() {
			return key.translate();
		}
	};
	
	private static final Destination LIBRARY = new Destination("library");
	private static final Destination EDITOR = new Destination("editor");
	private static final Destination HIDDEN = new Destination("hidden");
	
	@Override
	public String toString() {
		return super.toString();
	}

	private  final Map<String, SProcedure> table = new TreeMap<String, SProcedure>();
	private final Library library; 
	
	public SharedProcedures(Library l) {
		this.library = l;
	}

	public Vector<Procedure> getLibraryProcedures() {
		Vector<Procedure> procs = getProcedures(LIBRARY);
		Vector<Procedure> hProcs = getProcedures(HIDDEN);
		for (Procedure p  : hProcs)
			p.hide();
		procs.addAll(hProcs);
		return procs;
	}

	public Vector<Procedure> getEditorProcedures() {
		return getProcedures(EDITOR);
	}
	

	public Vector<Procedure> getProcedures() {
		Vector<Procedure> procedures = new Vector<>();
		for (SProcedure p : table.values())
			procedures.addElement(library.getProcedure(p.key));
		return procedures;
	}
	
	private Vector<Procedure> getProcedures(Destination dest) {
		Vector<Procedure> procedures = new Vector<>();
		for (SProcedure p : table.values())
			if (p.destination == dest)
				procedures.addElement(library.getProcedure(p.key));
		return procedures;
	}

	@Override
	public String getXMLTag() {
		return "SharedProcedures";
	}

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		for (SProcedure p : table.values()) 
			e.put(p);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		table.clear();
		while (child.hasChild(SPROC_XML_TAG)) {
			SProcedure p = new SProcedure(child);
			table.put(p.key, p);
		}
		return child;
	}
	
	private class SProcedure implements XMLCapabilities {
		
		private String key;
		private Destination destination;

		public SProcedure(Procedure p) {
			this.key = p.getKey();
			this.destination = LIBRARY;
		}
		
		public SProcedure(XMLReader e) {
			loadXMLProperties(e);
		}

		@Override
		public String getXMLTag() {
			return SPROC_XML_TAG.getXMLTag();
		}

		@Override
		public XMLWriter getXMLProperties() {
			XMLWriter e = new XMLWriter(this);
			e.setAttribute("key", key);
			e.setAttribute("destination", destination.code);
			return e;
		}

		@Override
		public XMLReader loadXMLProperties(XMLReader e) {
			XMLReader child = e.popChild(this);
			key = child.getAttribute("key", "");
			String dest = child.getAttribute("destination", "");
			if (dest.equals(EDITOR.code))
				destination = EDITOR;
			else if (dest.equals(LIBRARY.code))
				destination = LIBRARY;
			else
				destination = HIDDEN;
			return child;
		}
	}
	
	@Override
	public TKey getTitle() {
		return PROCS_MANAGER;
	}
	
	@Override
	public JPanel getSettingsPane(FWSettingsActionPuller actions) {
		Vector<String> keys = library.getSortedKeys();
		int len = keys.size();
		JComponent[] comps = new JComponent[2 * len];
		for (int idx = 0; idx < len; idx++) {
			String key = keys.elementAt(idx);
			comps[2 * idx] = new JLabel("<html><span class=\"library\">" + key + "</span></html>");
			SProcedure proc = table.get(key);
			if (proc == null)
				proc = new SProcedure(library.getProcedure(key));
			comps[2 * idx + 1] = getPane(proc);
		}
		return VerticalPairingLayout.createPanel(comps);
	}

	private JPanel getPane(final SProcedure proc) {
		final FWComboBox box = new FWComboBox(new Destination[]{LIBRARY, EDITOR, HIDDEN}, proc.destination, new FWComboBoxListener() {
			
			@Override
			public void itemSelected(Object o) {
				proc.destination = (Destination) o;
			}
		});
		boolean isShared = table.keySet().contains(proc.key);
		box.setEnabled(isShared);
		
		final JCheckBox checkBox = new JCheckBox("", isShared);
		checkBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				boolean isSelected = checkBox.isSelected();
				if (isSelected) 
					table.put(proc.key, proc);
				else
					table.remove(proc.key);
				box.setEnabled(isSelected);
			}
		});
		checkBox.setToolTipText(SHARE.translate(proc.key));
		
		return HorizontalFlowLayout.createPanel(checkBox, box);
	}
}