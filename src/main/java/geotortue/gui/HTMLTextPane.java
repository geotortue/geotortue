/**
 * 
 */
package geotortue.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Position;

import fw.app.FWToolKit;
import fw.app.Translator.TKey;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.FWServices;
import fw.xml.XMLCapabilities;
import fw.xml.XMLException;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;

public class HTMLTextPane extends JEditorPane implements XMLCapabilities {

	private static final TKey COPY = new TKey(HTMLTextPane.class, "copy");
	private final static OPTKey INVALID_LINK = new OPTKey(HTMLTextPane.class, "invalidLink");
	
	private static final long serialVersionUID = -2200665558617780989L;
	
	private final int width;

	public HTMLTextPane(String msg, int w) { 
		this.width = w;
		setEditable(false);
		setContentType("text/html");
		setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
		//setFocusable(false);
		addHyperlinkListener(new HyperlinkListener() {
		    public void hyperlinkUpdate(HyperlinkEvent e) {
		        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		        	URL url = e.getURL();
		        	if (url==null)
						try {
							url = new URL("http://geotortue.free.fr/"+e.getDescription());
						} catch (MalformedURLException e1) {
						}
		        	Container owner = getTopLevelAncestor();
		        	if (url==null)
		    			FWOptionPane.showErrorMessage(owner, INVALID_LINK);
		        	else
		        		FWServices.openBrowser(owner, url);
		        }
		    }
		});
		setText(msg);
		validate();
	}
	
	 

	
	public HTMLTextPane(final String msg) {
		this(msg, -1);
	}
	
	private Rectangle rect(Position p) throws BadLocationException {
		int off = p.getOffset();
		Rectangle r = modelToView(off > 0 ? off - 1 : off);
		return r;
	}

	public Dimension getPreferredSize() { // http://bugs.java.com/view_bug.do?bug_id=4765285
		if (width<0)
			return super.getPreferredSize();
		try {
			Rectangle start = rect(getDocument().getStartPosition());
			Rectangle end = rect(getDocument().getEndPosition());
			if (start == null || end == null) {
				return super.getPreferredSize();
			}
			int height = end.y + end.height - start.y + 4;
			int w = super.getPreferredSize().width;
			if (w>width)
				w = width;
			return new Dimension(w, height);
		} catch (BadLocationException e) {
			return super.getPreferredSize();
		}
	}
	
	@Override
	public void setText(String t) {
		super.setText(t);
		validate();
		setCaretPosition(0);
	}
	
	public JPopupMenu getComponentPopupMenu() {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem copy = new JMenuItem(new DefaultEditorKit.CopyAction());

		if (getSelectedText() == null) 
			copy.setEnabled(false);

		copy.setAccelerator(KeyStroke.getKeyStroke('C', KeyEvent.CTRL_MASK));
		copy.setIcon(FWToolKit.getIcon("copy.png"));
		copy.setText(COPY.translate());
		popup.add(copy);
		return popup;
	}

	@Override
	public String getXMLTag() {
		return "HTMLTextPane";
	}

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.setContent(getText());
		return e;
	}

	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		try {  
			setText(child.getContent());
		} catch (XMLException ex) {
			setText("");
			ex.printStackTrace();
		}
		return child;
	}
}