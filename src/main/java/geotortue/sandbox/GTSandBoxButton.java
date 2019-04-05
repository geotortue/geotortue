/**
 * 
 */
package geotortue.sandbox;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;

import fw.app.Translator.TKey;
import fw.gui.FWDialog;
import fw.gui.FWLabel;
import fw.gui.FWTextField;
import fw.gui.FWTextField.FWTextFieldListener;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWBoolean;
import fw.gui.params.FWFileAssistant;
import fw.gui.params.FWFileAssistant.FKey;
import fw.gui.params.FWParameterListener;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;
import geotortue.core.GTDocumentFactory;
import geotortue.gui.GTFileLoaderWidget;

class GTSandBoxButton extends JButton implements XMLCapabilities {
	
	private static final long serialVersionUID = 2028172439946290792L;

	private static final TKey TOOLTIP = new TKey(GTSandBoxButton.class, "tooltip");
	private static final TKey COMMAND = new TKey(GTSandBoxButton.class, "command");
	private static final TKey ICON = new TKey(GTSandBoxButton.class, "icon");
	private static final TKey SHOW_COMMAND = new TKey(GTSandBoxButton.class, "showCommand");
	private static final FKey ICON_FILE = new FKey(GTSandBox.class, "png", "jpg", "gif");
	
	static final XMLTagged XML_TAG = XMLTagged.Factory.create("GTSandBoxButton");
	private final static TKey OPEN_ICON = new TKey(GTSandBoxButton.class, "openIcon.tooltip");
	private final static TKey DELETE_ICON = new TKey(GTSandBoxButton.class, "deleteIcon.tooltip");
	private final static TKey NONE = new TKey(GTSandBoxButton.class, "none");
	private static final TKey EDITOR = new TKey(GTSandBoxButton.class, "editor");
	
	private final GTDocumentFactory docFactory;
	
	private String command = "";
	private URL iconURL = null;
	String jarPath = null;
	private FWBoolean showCommand = new FWBoolean("show", true);
	private ImageIcon icon;

	GTSandBoxButton(GTDocumentFactory df) {
		this.docFactory = df;
		setCommand("");
		setText("?");
		setToolTipText("");
		setVerticalTextPosition(SwingConstants.BOTTOM);
	    setHorizontalTextPosition(SwingConstants.CENTER);
	}
	
	void updateStyle() {
		setFont(GTSandBox.buttonStyle.getFont()); 
	}
	
	GTSandBoxButton(GTDocumentFactory df, XMLReader e) {
		this.docFactory = df;
		setVerticalTextPosition(SwingConstants.BOTTOM);
	    setHorizontalTextPosition(SwingConstants.CENTER);
		loadXMLProperties(e);
	}
	
	private void setCommand(final String command){
		this.command = command;
		setAction(new AbstractAction() {
			private static final long serialVersionUID = 7388296516532971169L;

			@Override
			public void actionPerformed(ActionEvent e) {
				docFactory.appendAndProcessCommand(command);
			}
		});
		updateText();
	}
	
	public void updateText() {
		if (showCommand.getValue())
			setText("<html><p class=\"button\">"+docFactory.getHtmlText(command)+"</p></html>");
		else
			setText("");
		repaint();				
	}
	
	
	private void setIcon(URL url) {
		setIcon(url, null);
	}
	
	private void setIcon(URL url, String jarPath) {
		this.jarPath = jarPath;
		if (url == null) {
			this.icon = null;
		} else
			try {
				File file = new File(url.toURI());
				if (file != null && file.exists())
					this.icon = new ImageIcon(file.getAbsolutePath());
				else
					this.icon = null;
			} catch (URISyntaxException ex) {
				ex.printStackTrace();
			}
		
		super.setIcon(icon);
		repaint();
		
		this.iconURL = (icon!=null)? url : null;
	}
	
	@Override
	public String getXMLTag() {
		return XML_TAG.getXMLTag();
	}

	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.setAttribute("command", command);
		String tooltip = getToolTipText();
		tooltip = (tooltip == null) ? "" : tooltip;
		e.setAttribute("tooltip", tooltip);
		
		if (jarPath != null)
			e.setAttribute("jaricon", jarPath);
		else if (iconURL != null)
			e.setAttribute("icon", iconURL.toString());
		showCommand.storeValue(e);;
		return e;
	}

	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		showCommand.fetchValue(child, true);
		setCommand(child.getAttribute("command", ""));
		setToolTipText(child.getAttribute("tooltip", ""));
		
		try {
			setIcon(new URL(child.getAttribute("icon")));
		} catch (XMLException ex) { // no icon attribute : may be an icon located in the jar
			try {
				jarPath = child.getAttribute("jaricon");
				URL url = getClass().getResource(jarPath);
				setIcon(url, jarPath);
			} catch (XMLException ex1) {}
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		}
		return child;
	}

	public JPanel getCell() {
		JPanel pane = new JPanel();
		if (icon != null)
			pane.add(new JLabel(icon));
		pane.add(new JLabel(getText()));
		return pane;
	}

	public JPanel getEditor() {
		final FWTextField commandF = new FWTextField(command, 32, new FWTextFieldListener() {
			@Override
			public void textChanged(String text, DocumentEvent e) {
				setCommand(text);
			}
		});
		
		JCheckBox showCommandCB = showCommand.getComponent(new FWParameterListener<Boolean>() {
			@Override
			public void settingsChanged(Boolean value) {
				updateText();
			}
		});

		final FWTextField tooltipF = new FWTextField(getToolTipText(), 32, new FWTextFieldListener() {
			@Override
			public void textChanged(String text, DocumentEvent e) {
				setToolTipText(text);
			}
		});
		
		final JLabel imgLabel= new FWLabel(NONE);
		if (iconURL!=null)
			imgLabel.setText(new File(iconURL.getFile()).getName());
		
		JPanel iconButtonsPane = new GTFileLoaderWidget(imgLabel,OPEN_ICON, "image-open.png", DELETE_ICON, "edit-clear.png") {
			private static final long serialVersionUID = -5084131822481579045L;

			@Override
			protected void open() {
				FWFileAssistant a = new FWFileAssistant((Window) imgLabel.getTopLevelAncestor(), ICON_FILE);
				File file = a.getFileForLoading();
				if (file!=null) {
					try {
						setIcon(file.toURI().toURL());
					} catch (MalformedURLException ex) {
						ex.printStackTrace();
					}
					if (icon != null)
						imgLabel.setText(file.getName());
					else
						imgLabel.setText(NONE.translate());
				}
			}
			
			@Override
			protected void delete() {
				setIcon(null, null);
				imgLabel.setText(NONE.translate());
			}
		};
		
		return 
				VerticalPairingLayout.createPanel(10, 10, 
				new FWLabel(COMMAND, SwingConstants.RIGHT), commandF,
				new FWLabel(SHOW_COMMAND, SwingConstants.RIGHT), showCommandCB,
				new FWLabel(TOOLTIP, SwingConstants.RIGHT), tooltipF,
				new FWLabel(ICON, SwingConstants.RIGHT), iconButtonsPane); 
	}
	
	void showButtonEditor(Window owner) {
		JPanel buttonEditor = getEditor();
		buttonEditor.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		FWDialog commentsDial = new FWDialog(owner, EDITOR, buttonEditor, true, false) ;
		commentsDial.setMinimumSize(new Dimension(600, 150));
		commentsDial.setModal(true);
		buttonEditor.getComponent(1).requestFocusInWindow();
		commentsDial.setVisible(true);
	}

	@Override
	public void setToolTipText(String text) {
		if (text == null || text.equals(""))
			super.setToolTipText(null);
		else
			super.setToolTipText(text);
	}
	
	
}