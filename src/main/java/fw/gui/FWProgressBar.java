package fw.gui;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JWindow;

import fw.app.FWLauncher;


public class FWProgressBar extends JWindow {
	
	private static final long serialVersionUID = -2656855692667362725L;

	private JProgressBar progressBar = new JProgressBar();
	
	public FWProgressBar(Component owner, boolean indeterminate){
		progressBar.setIndeterminate(indeterminate);
		progressBar.setStringPainted(true);
		setContentPane(progressBar);
		setSize(progressBar.getPreferredSize());
		setAlwaysOnTop(true);
		setIconImage(FWLauncher.ICON);
		//setUndecorated(true);
		//setTitle("SProgressBar.message");
		pack();
		setLocationRelativeTo(owner);
		new Thread(){
			@Override
			public void run() {
				setVisible(true);
			}
		
		}.start();

	}

	/**
	 * @return the progressBar
	 */
	public JProgressBar getProgressBar() {
		return progressBar;
	}

	/**
	 * @param progressBar the progressBar to set
	 */
	public void setProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	
	public void setValue(double x){
		progressBar.setValue((int) (x*100));
		repaint();
		if (x>=1)
			dispose();
	}
}