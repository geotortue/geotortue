package fw.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultEditorKit;

import fw.app.FWAction.ActionKey;
import fw.app.Translator.TKey;
import fw.app.header.FWMenuBar;
import fw.gui.FWLabel;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.ANSWER;
import fw.gui.FWOptionPane.OPTKey;


public class FWConsole extends JFrame {

	private static final long serialVersionUID = 760996916048611499L;
	
	private static final TKey TITLE = new TKey(FWConsole.class, "title");
	private static final TKey MENU = new TKey(FWConsole.class, "menu");
	private static final TKey PREAMBULE = new TKey(FWConsole.class, "preambule");
	private static final ActionKey QUIT = new ActionKey(FWConsole.class, "quit");
	private static final ActionKey EXIT = new ActionKey(FWConsole.class, "exit");
	private static final OPTKey SHOW_CONSOLE = new OPTKey(FWConsole.class, "showConsole");
	private static final OPTKey CONFIRM_EXIT = new OPTKey(FWConsole.class, "confirmExit");
	private static final ActionKey COPY = new ActionKey(FWConsole.class, "copy");

	private static final FWConsole SHARED_CONSOLE = new FWConsole();
	
	private transient File logFile;
	private final String header;
	private final JTextArea textArea;
	private transient FileOutputStream fileStream;

	private final transient WindowListener exitJVMListener = new WindowAdapter(){
		@Override
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	};
		
	private FWConsole() {
		super("System error stream");
		try {
			this.logFile = new File(FWManager.getConfigDirectory(), "error.log");
		} catch (FWRestrictedAccessException e) {
			this.logFile = null;
		}
		final String path = (logFile != null) ? logFile.getAbsolutePath() : "none";
		this.header = "  " + FWManager.getApplicationTitle()+" v"+FWManager.getApplicationVersion()+"\n" +
				"  Log file : " + path + "\n" +
				"  System : " + System.getProperty("os.name") + " ; " 
				+ System.getProperty("os.arch") + " ; " + System.getProperty("os.version") + "\n" +
				"  Java : v" + System.getProperty("java.version") + " ; " + System.getProperty("java.vendor") +"\n\n";
		
		this.textArea = new JTextArea(header) {
			private static final long serialVersionUID = 7267119790720613667L;

			@Override
			public JPopupMenu getComponentPopupMenu() {
				JPopupMenu popup = new JPopupMenu();
				JMenuItem copy = new JMenuItem(new DefaultEditorKit.CopyAction());
	
				if (getSelectedText() == null) 
					copy.setEnabled(false);
	
				copy.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_MASK));
				copy.setIcon(FWToolKit.getIcon("copy.png"));
				copy.setText(COPY.translate());
				popup.add(copy);
				return popup;
			}};
		textArea.setEditable(false);

		getContentPane().add(new JScrollPane(textArea));

		setSize(new Dimension(800, 600));
		setLocationRelativeTo(null);
		
		addWindowListener(exitJVMListener);
	}
	
	private static synchronized void log(String msg) {
		SHARED_CONSOLE.doLog(msg);
	}
	
	private synchronized void doLog(String msg) {
		if (fileStream == null) {
			System.err.println(msg);
			return;
		}

		try {
			final OutputStreamWriter textOut = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8.newEncoder());
			textOut.write(msg + "\n");
			textOut.close();
			fileStream = new FileOutputStream(logFile, true);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private static void redirectErrorStream() {
		SHARED_CONSOLE.doRedirectErrorStream();
	}
	
	private void doRedirectErrorStream() {
		if (logFile == null) {
			return;
		}

		try {
			fileStream = new FileOutputStream(logFile, true);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}

		log("\n[Version]" + FWManager.getApplicationTitle()+" v"+FWManager.getApplicationVersion());
		log("[System] " + System.getProperty("os.name") + " / " + System.getProperty("os.arch") + " / " + 
						System.getProperty("os.version"));
		log("[Java] v" + System.getProperty("java.version") + " " + System.getProperty("java.vendor"));
		
		System.setErr(new PrintStream(new ConsoleStream()));
	}
	
	public static void beautifySharedInstance() {
		SHARED_CONSOLE.beautify();
	}	

	private void beautify() {
		removeWindowListener(SHARED_CONSOLE.exitJVMListener);
		setTitle(FWManager.getApplicationTitle() +" - "+TITLE.translate());
		setIconImage(FWManager.getImage("/cfg/icon/log_debug.png"));
		
		setJMenuBar(new FWMenuBar(MENU,  
				new FWAction(QUIT, 0, KeyEvent.VK_ESCAPE, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						FWConsole.this.setVisible(false);
					}
				}),
				new FWAction(COPY, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						textArea.selectAll();
						textArea.copy();
						textArea.requestFocusInWindow();
					}
				}),
				new FWAction(EXIT, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ANSWER answer = FWOptionPane.showErrorDialog(SHARED_CONSOLE, CONFIRM_EXIT);
						if (answer == ANSWER.YES)
							System.exit(0);
					}
				})
		));
		
		JPanel headPane = new JPanel(new BorderLayout());
		headPane.add(new FWLabel(PREAMBULE), BorderLayout.CENTER);
		headPane.setBorder(BorderFactory.createEtchedBorder());
		
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.add(new JScrollPane(textArea), BorderLayout.CENTER);
		mainPane.add(headPane, BorderLayout.NORTH);
		
		getContentPane().removeAll();
		getContentPane().add(mainPane);
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		getContentPane().add(mainPane);
		ignore = false;
	}
	
	private static boolean ignore = true;

	public static void showSharedInstance() {
		SHARED_CONSOLE.setVisible(true);
		SHARED_CONSOLE.requestFocus();
	}
	
	private static void askToShowSharedInstance() {
		if (ignore)
			return;
		if (!SHARED_CONSOLE.isVisible()) {
			ANSWER answer = FWOptionPane.showErrorDialog(null, SHOW_CONSOLE);
			if (answer == ANSWER.YES)
				showSharedInstance();
			else {
				ignore = true;
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						ignore = false;
					}
				}, 1000);
			}
		}
	}
	
	private class ConsoleStream extends OutputStream {
		
		public void write(int b) throws IOException {
			fileStream.write(b);
			textArea.append(new String(new byte[]{(byte) b}));
			askToShowSharedInstance();
		}

		@Override
		public final void write(byte[] bytes) throws IOException {
			fileStream.write(bytes);
			textArea.append(new String(bytes));
			askToShowSharedInstance();
		}
	}
	
	/*
	 * DEBUG
	 */
	
	private static boolean debugModeEnabled = false;

	public static boolean isDebugModeEnabled() {
		return debugModeEnabled;
	}

	public static void setDebugModeEnabled(final boolean debug) {
		debugModeEnabled = debug;
		if (debug) 
			printInfo(SHARED_CONSOLE, "DEBUG MODE ENABLED");
		else 
			redirectErrorStream();
	}

	public static void printInfo(final Object o, final String msg) {
		if (debugModeEnabled)
			System.out.println("[Info @ " + o.getClass().getSimpleName()+"] > " + msg);
	}

	public static void printWarning(Object o, String msg) {
		if (debugModeEnabled)
			System.out.println("[Warning @ " + o.getClass().getSimpleName() + "] > " + msg);
	}
}