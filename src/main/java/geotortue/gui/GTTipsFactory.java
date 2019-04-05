package geotortue.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fw.app.FWToolKit;
import fw.app.Translator;
import fw.app.Translator.TKey;
import fw.files.FileUtilities.HTTPException;
import fw.gui.FWButton;
import fw.gui.FWButton.BKey;
import fw.gui.FWButton.FWButtonListener;
import fw.gui.FWImagePane;
import fw.gui.FWLabel;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLFile;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;


public class GTTipsFactory implements XMLCapabilities {

	/**
	 * 
	 */
	private static final BKey NEXT = new BKey(GTTipsFactory.class, "next");
	private static final BKey PREVIOUS = new BKey(GTTipsFactory.class, "prev");
	private static final TKey TIPS = new TKey(GTTipsFactory.class, "tips");
	
	private final Vector<GTTip> tips = new Vector<GTTip>();
	private int index = 0;

	final private JPanel pane = new JPanel(new BorderLayout());
	final private FWButton nextButton = new FWButton(NEXT, new FWButtonListener() {
		@Override
		public void actionPerformed(ActionEvent e, JButton source) {
			refreshPane(index+1);
		}
	});
	
	final private FWButton prevButton = new FWButton(PREVIOUS, new FWButtonListener() {
		@Override
		public void actionPerformed(ActionEvent e, JButton source) {
			refreshPane(index-1);
		}
	});
	
	public GTTipsFactory(URL url) {
		try {
			loadXMLProperties(new XMLFile(url).parse());
		} catch (XMLException | IOException | HTTPException ex) {
			ex.printStackTrace();
		}
		nextButton.setIcon(FWToolKit.getIcon("go-next.png"));
		prevButton.setIcon(FWToolKit.getIcon("go-previous.png"));
		pane.setBackground(Color.WHITE);
	}
	
	public JPanel getRandomTip() {
		refreshPane((int) (Math.random()*tips.size()));
		return pane;
	}
	
	private void refreshPane(int idx) {
		pane.removeAll();

		index = (idx)%(tips.size());
		
		if (index<0)
			index += tips.size();
		
		GTTip tip = tips.get(index);
		String tipText = "<html>"+tip.content+"<br></html>";
		JLabel tipTextLabel = new JLabel(tipText);
		tipTextLabel.setPreferredSize(new Dimension(520, 120));
		tipTextLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		JPanel tipPane =  new JPanel(new BorderLayout());
		tipPane.add(tipTextLabel);
		tipPane.setBackground(Color.WHITE);
		tipPane.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, Color.LIGHT_GRAY));
		
		JPanel titlePane  = new JPanel(new BorderLayout());
		titlePane.setBackground(Color.WHITE);
		titlePane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY));
		
		FWImagePane imPane = new FWImagePane(FWToolKit.getIcon("star.png").getImage());
		imPane.setBackground(Color.WHITE);
		imPane.setPreferredSize(new Dimension(32, 36));
		titlePane.add(imPane, BorderLayout.WEST);
		titlePane.add(new FWLabel(TIPS), BorderLayout.CENTER);
		
		JPanel buttonPane = new JPanel(new BorderLayout());
		buttonPane.add(nextButton, BorderLayout.EAST);
		buttonPane.add(prevButton, BorderLayout.WEST);
		buttonPane.setBackground(Color.WHITE);
		
		pane.add(titlePane, BorderLayout.NORTH);
		pane.add(tipPane, BorderLayout.CENTER);		
		pane.add(buttonPane, BorderLayout.SOUTH);
		pane.validate();
	}

	@Override
	public String getXMLTag() {
		return "GTTipsFactory";
	}

	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		for (GTTip tip : tips)
			e.put(tip);

		return e;
	}

	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		while (child!=null){ 
			try {
				String code = child.getAttribute("lang");
				if (Translator.getLanguage().equals(new Locale(code).getLanguage())) {
					while (child.hasChild(GTTip.XML_TAG))
						tips.add(new GTTip(child));
					return child;
				} else
					child = e.popChild(this);
			} catch (XMLException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	private static class GTTip implements XMLCapabilities {
		
		private static final XMLTagged XML_TAG = XMLTagged.Factory.create( "GTTip");
		private String content = "";

		private GTTip(XMLReader e) {
			loadXMLProperties(e);
		}
		
		public String getXMLTag() {
			return XML_TAG.getXMLTag();
		}

		public XMLWriter getXMLProperties() {
			XMLWriter e = new XMLWriter(this);
			e.setContent(content);
			return e;
		}

		public XMLReader loadXMLProperties(XMLReader e) {
			XMLReader child = e.popChild(this);
			try {
				content = child.getContent();
			} catch (XMLException ex) {
				ex.printStackTrace();
			}
			return child;
		}
	}
}