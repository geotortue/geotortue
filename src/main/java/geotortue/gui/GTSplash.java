package geotortue.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import fw.app.FWLauncher;
import fw.app.FWManager;
import fw.app.Translator.TKey;
import fw.gui.FWImagePane;
import fw.gui.FWServices;
import fw.gui.layout.VerticalFlowLayout;
import geotortue.GTLauncher;



public class GTSplash extends JFrame {
	
	private static final long serialVersionUID = 404342214472288813L;
	
	private static BufferedImage SPLASH_IMG;
	
	protected JProgressBar progressBar;
	protected JPanel contentPane;

	private GTSplash(final Window owner, final String title, final boolean withProgressBar) {
		super(title);
		this.contentPane = new JPanel(new BorderLayout());
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				if (withProgressBar) {
					progressBar = new JProgressBar();
					progressBar.setIndeterminate(true);
					progressBar.setStringPainted(true);
					progressBar.setString("Initalisation...");
				}

				//setAlwaysOnTop(true);
				if (title == null)
					setUndecorated(true);
				setIconImage(FWLauncher.ICON);

				SPLASH_IMG = GTLauncher.IS_BETA ? FWManager.getImage("/cfg/splash-v4-beta.png")
							: FWManager.getImage("/cfg/splash-v4.png");

				FWImagePane imgPane = new FWImagePane(SPLASH_IMG);
				contentPane.add(imgPane, BorderLayout.CENTER);

				imgPane.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						dispose();
					}
				});

				if (withProgressBar)
					contentPane.add(progressBar, BorderLayout.SOUTH);

				contentPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
				setContentPane(contentPane);

				pack();

				setSize(getPreferredSize());
				setLocationRelativeTo(owner);
				setVisible(true);
			}
		});
	}
	
	public GTSplash(){
		this(null, null, true);
	}
	
	public GTSplash(final Window owner, ActionKey key, String licence, String url) {
		this(owner, FWManager.getApplicationTitle(), false);
		
		final FWAction visitAction = new FWAction(key, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FWServices.openBrowser(owner, "http://geotortue.free.fr");
			}
		});
		
		final AbstractAction quitAction = new AbstractAction() {
			private static final long serialVersionUID = 8422363352348479488L;

			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};
		
		JLabel appRef = new JLabel(FWManager.getApplicationTitle()+" v"+FWManager.getApplicationVersion(), SwingConstants.CENTER);
		
		JButton visitButton = new JButton(visitAction);
		visitButton.getActionMap().put("visit", visitAction);
		visitButton.getActionMap().put("quit", quitAction);

		visitButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "visit");

		visitButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "quit");
		
		JLabel mailRef = new JLabel("geotortue@free.fr", SwingConstants.CENTER);
		
		JPanel info = VerticalFlowLayout.createPanel(visitButton, appRef, mailRef,  new JLabel(licence));
		
		contentPane.add(info, BorderLayout.SOUTH);
		pack();
		doLayout();
	}
	
	public void setMessage(final TKey key){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (progressBar!=null)
					progressBar.setString(key.translate());
			}
		});
		
	}
	
	public void setValue(final int value){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (progressBar.isIndeterminate())
					progressBar.setIndeterminate(false);
				progressBar.setValue(value);
			}
		});
	
//		System.out.println(value);
//		
//		times.add(System.currentTimeMillis());
//		if (value==100) {
//			long ti = times.firstElement();
//			long tf = times.lastElement();
//			for (Long t : times) 
//				System.out.println((t-ti)/(0.+tf-ti)*100+"%");
//		}
	}
	
//	private Vector<Long> times = new Vector<Long>();
}