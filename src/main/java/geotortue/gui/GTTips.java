package geotortue.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Window;
import java.net.URL;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import fw.app.FWManager;
import fw.app.Translator;
import fw.app.Translator.TKey;
import fw.app.prefs.FWBooleanEntry;
import fw.gui.FWDialog;
import fw.gui.FWLabel;
import fw.gui.params.FWBoolean;
import fw.xml.XMLTagged;


public class GTTips implements XMLTagged {
	
	/**
	 * 
	 */
	private static final TKey SHOW = new TKey(GTTips.class, "show");
	/**
	 * 
	 */
	private static final TKey HIDE = new TKey(GTTips.class, "hide");
	private final FWBooleanEntry HIDE_TIPS = new FWBooleanEntry(this, "hideTips."+FWManager.getApplicationVersion(), false);
	

	public void showTips(Window owner) {
		showTips(owner, false);
	}
	
	private void showTips(Window owner, boolean force) {
		if (HIDE_TIPS.getValue() && !force)
			return;
		
		URL tipsUrl = FWManager.getResource("/cfg/lang/"+Translator.getLanguage()+"/tips.xml");
		GTTipsFactory tipsFactory = new GTTipsFactory(tipsUrl);
		show(owner, HIDE_TIPS, tipsFactory.getRandomTip());
	}
	
	private void show(Window owner, FWBoolean bool, JComponent comp) {
		JPanel southPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JCheckBox cb = bool.getComponent();
		cb.setBackground(Color.WHITE);
		southPane.add(cb);
		southPane.add(new FWLabel(HIDE));
		southPane.setBackground(Color.WHITE);

		JPanel msgPane = new JPanel(new BorderLayout()); 
		msgPane.add(comp, BorderLayout.NORTH);
		msgPane.add(southPane, BorderLayout.SOUTH);
		
		FWDialog  dial = new FWDialog(owner, SHOW, msgPane, true, false);
		dial.setVisible(true);
	}
	
	@Override
	public String getXMLTag() {
		return "GTTips";
	}
}