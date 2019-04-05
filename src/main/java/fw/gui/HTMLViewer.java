package fw.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.FWLauncher;
import fw.app.FWToolKit;
import fw.app.Translator.TKey;



/**
 * 
 * 
 */
@Deprecated
public class HTMLViewer extends JFrame  {
	/**
	 * 
	 */
	private static final TKey ERROR_404 = new TKey(HTMLViewer.class, "404");
	/**
	 * 
	 */
	private static final TKey COPY = new TKey(HTMLViewer.class, "copy");
	private static final ActionKey GO_PREVIOUS = new ActionKey(HTMLViewer.class, "go-previous");
	private static final ActionKey GO_NEXT = new ActionKey(HTMLViewer.class, "go-next");
	private static final ActionKey GO_HOME = new ActionKey(HTMLViewer.class, "go-home");


	private static final long serialVersionUID = 3967530318852156201L;

	
	protected Stack<URL> previousURL = new Stack<URL>();
	protected Stack<URL> nextURL = new Stack<URL>();

	
	public final URL homeURL;
	
	private JEditorPane htmlPane = new JEditorPane() {
		private static final long serialVersionUID = 1917772754755953070L;

		public JPopupMenu getComponentPopupMenu(){
			JPopupMenu popup = new JPopupMenu();
			
			JMenuItem copy=new JMenuItem(new DefaultEditorKit.CopyAction());
			copy.setIcon(FWToolKit.getIcon("copy.png"));
			copy.setText(COPY.translate());
			
			JMenuItem goHome=new JMenuItem(goHomeAction);
			JMenuItem goPrevious=new JMenuItem(goPreviousAction);
			JMenuItem goNext=new JMenuItem(goNextAction);
			
			
			if (htmlPane.getSelectedText()==null){
				copy.setEnabled(false);
			}
			
			popup.add(copy);
			popup.add(new JSeparator());
			popup.add(goPrevious);
			popup.add(goNext);
			popup.add(new JSeparator());
			popup.add(goHome);
			return popup;
		}
	};

	
	private HTMLViewer(URL url, TKey key, JFrame owner) {
		super(key.translate());
		this.homeURL = url;
		setPage(url);
		htmlPane.setEditable(false);
		
		// hyperlink
		htmlPane.addHyperlinkListener(new HyperlinkListener(){
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType()== HyperlinkEvent.EventType.ACTIVATED) {
					nextURL.clear();
					setPage(e.getURL());
				}
			}
		});
		
		
		// Toolbar
		JToolBar toolBar = new JToolBar();
		
		JButton goHomeButton = goHomeAction.getButton();
		JButton goPreviousButton = goPreviousAction.getButton();
		JButton goNextButton = goNextAction.getButton();
		
		goHomeButton.setText("");
		goPreviousButton.setText("");
		goNextButton.setText("");
		
		toolBar.add(goHomeButton);
		toolBar.add(goPreviousButton);
		toolBar.add(goNextButton);
		
		// KeyListener
		 KeyAdapter keyAdapter = new KeyAdapter(){
			public void keyTyped(KeyEvent e){
				if ((e.getKeyChar()==KeyEvent.VK_ESCAPE) 
						|| (e.getKeyChar() == 23))
					dispose();
			}
		};

		htmlPane.addKeyListener(keyAdapter);
		goHomeButton.addKeyListener(keyAdapter);
		goPreviousButton.addKeyListener(keyAdapter);
		goNextButton.addKeyListener(keyAdapter);
		
		// Layout
		
		Dimension d = new Dimension(800, 600);
		
		setPreferredSize(d);
		htmlPane.setPreferredSize(d);
		setSize(getPreferredSize());
		setLocationRelativeTo(owner);
		
		JScrollPane scrollPane=new JScrollPane(htmlPane);
		scrollPane.setPreferredSize(d);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(toolBar, BorderLayout.NORTH);
		validate();

		
		// icon
		setIconImage(FWLauncher.ICON);

		setVisible(true);
	}
	
	private synchronized void setPage(URL url) {
		if (htmlPane.getPage()==null || !htmlPane.getPage().sameFile(url))
			previousURL.add(url);
		try {
			htmlPane.setContentType("text/html; charset=UTF-8");
			htmlPane.setPage(url);
		} catch (IOException ex) {
			Document doc = htmlPane.getEditorKit().createDefaultDocument();
			try {
				doc.insertString(0, ERROR_404.translate()+"\t" + url, null);
				htmlPane.setDocument(doc);
				ex.printStackTrace();
			} catch (BadLocationException ex1) {
				ex1.printStackTrace();
			}
		} finally {
			goPreviousAction.setEnabled(previousURL.size()>1);
			goNextAction.setEnabled(!nextURL.isEmpty());
		}
	}
	
	//Actions
	private FWAction goPreviousAction = new FWAction(GO_PREVIOUS, "go-previous.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (previousURL.size()>1){
				nextURL.add(previousURL.pop());
				setPage(previousURL.pop());
			}
		}
	});

	private FWAction goNextAction = new FWAction(GO_NEXT, "go-next.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (!nextURL.isEmpty()){
				setPage(nextURL.pop());
			}
		}
	});
	
	private FWAction goHomeAction = new FWAction(GO_HOME, "go-home.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			nextURL.clear();
			setPage(homeURL);
		}
	});
}