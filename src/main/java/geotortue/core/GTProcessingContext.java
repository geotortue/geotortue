package geotortue.core;

import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.nfunk.jep.addon.JEPException;
import org.nfunk.jep.function.PostfixMathCommandI;

import files.GTUserFileManager;
import function.JFunction;
import fw.app.FWAction;
import fw.app.FWConsole;
import fw.app.Translator.TKey;
import fw.geometry.util.MathException;
import fw.gui.FWLabel;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWBoolean;
import fw.gui.params.FWInteger;
import fw.gui.params.FWParameterListener;
import fw.renderer.MouseManager;
import fw.text.TextStyle;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.core.GTCommandProcessor.GTInterruptionException;
import geotortue.core.GTMessageFactory.GTTrouble;
import geotortue.core.KeywordManager.Filter;
import geotortue.core.ThreadQueue.ThreadQueueListener;
import geotortue.core.Turtle.NoScoreException;
import geotortue.core.TurtleManager.NoSuchTurtleException;
import geotortue.gallery.PictureWriter;
import geotortue.geometry.GTGeometry;
import geotortue.geometry.GTGeometryManager;
import geotortue.geometry.proj.GTPerspective;
import geotortue.gui.GTBoard;
import geotortue.gui.GTDialog;
import geotortue.gui.GTDialog.StringHandler;
import geotortue.gui.GTHelp;
import geotortue.gui.GTPanelAssistant;
import geotortue.renderer.GTGraphicSpace;
import geotortue.renderer.GTRendererI;
import jep2.JEP2;
import jep2.JEP2Exception;
import sound.GTMidi;
import sound.GTMidi.MidiChannelException;
import sound.GTMidiI;
import sound.GTMidiNotAvailable;
import sound.MusicEvent;
import type.JList;
import type.JObjectI;
import type.JObjectI.JEP2Type;
import type.JObjectsVector;
import type.JString;

public class GTProcessingContext implements XMLCapabilities, FWSettings {

	private static final TKey NAME = new TKey(GTProcessingContext.class, "settings");

	@Override
	public TKey getTitle() {
		return NAME;
	}


	private static final TKey PROMPT_MSG = new TKey(GTProcessingContext.class, "prompt.msg");
	private static final TKey PROMPT = new TKey(GTProcessingContext.class, "prompt");
	private static final TKey DELAY = new TKey(GTProcessingContext.class, "delay");
	private static final TKey REAL_TIME_PAINTING = new TKey(GTProcessingContext.class, "realTimePainting");
	
	private final ProcedureManager procManager;
	private final TurtleManager turtleManager;
	private final KeywordManager keywordManager;
	private final GTGraphicSpace graphicSpace;
	private final GTGeometryManager geometryManager;
	private final GTDisplayManager displayManager;
	private final GTEnhancedJEP numberParser;
	private final Library library;
	private final FWInteger waitingTime = new FWInteger("waitingTime", 100, 0, 2000);
	private final FWBoolean realTimePainting = new FWBoolean("realTimePainting", true);
	private final PictureWriter pictureWriter;
	private final GTBoard board;
	private GTMidiI midi = null;
	private final GTUserFileManager userFileManager;
	private final GTCommandProcessor processor;
	private final GTCommandFactory commandFactory;
	private final GTOptionalCommands optionalCommands;
	
	private GTPanelAssistant assistant;
	
	private GTProcessingContext(Window owner, TurtleManager tm, KeywordManager km, GTGraphicSpace gs,
			GTGeometryManager gm, GTDisplayManager dm, Library l, PictureWriter pw) {
		this.procManager = new ProcedureManager();
		this.turtleManager = tm;
		this.keywordManager = km;
		this.graphicSpace = gs;
		this.geometryManager = gm;
		this.displayManager = dm;

		this.commandFactory = new GTCommandFactory(keywordManager);
		
		this.userFileManager = new GTUserFileManager(owner);
		
		this.numberParser = new GTEnhancedJEP(getGeometry(), turtleManager, keywordManager,
				commandFactory, userFileManager);
		
		turtleManager.setListener(numberParser);
		
		this.library = l;
		this.pictureWriter = pw;
		this.board = new GTBoard(keywordManager);
		
		boolean midiAvailable = true;
		try {
			this.midi = new GTMidi(owner);
		} catch (InvalidMidiDataException | MidiUnavailableException ex) {
			FWConsole.printWarning(this, "MIDI not available");
			this.midi = new GTMidiNotAvailable();
			midiAvailable = false;
		}
		
		this.processor = new GTCommandProcessor(this, midi, commandFactory);
		
		this.optionalCommands = new GTOptionalCommands(midiAvailable) {
			public void setCircleEnabled(boolean value) {
				commandFactory.setCircleEnabled(value);
			}
			
			public void setMusicEnabled(boolean b) {
				numberParser.enableMusicItems(b);
				commandFactory.setMusicEnabled(b);
			}
		};
	}
	
	public GTProcessingContext(Window owner, TurtleManager tm, KeywordManager km, GTGraphicSpace gs, GTGeometryManager gm, GTDisplayManager dm) {
		this(owner, tm, km, gs, gm, dm, new Library(km), new PictureWriter(owner));
	}
	
	public static GTProcessingContext getModelInstance(Window owner, TurtleManager tm, KeywordManager km, GTGraphicSpace gs, GTGeometryManager gm, GTDisplayManager dm,  Library l) {
		return new GTProcessingContext(owner, tm, km, gs, gm, dm, l, null);
	}
	
	public FWAction[] getProcessorActions() {
		return processor.getActions();
	}
	
	public void interrupt() {
		processor.interrupt();
	}
	
	
	public JObjectI<?> process(GTCommandBundles bundles) throws GTException, GTInterruptionException {
		bundles.init();
		return processor.process(bundles);
	}
	
	public void addThreadQueueListener(ThreadQueueListener l) {
		processor.addThreadQueueListener(l);
	}

	public void sleep() {
		if (waitingTime.getValue()>0 && realTimePainting.getValue())
				sleep(waitingTime.getValue());
	}

	public void sleep(int x) {
		repaint();
		processor.sleep(x);
	}


	/**
	 * 
	 */
	public void suspend() {
		processor.suspend();
		
	}
	
	public ProcedureManager getProcedureManager(){
			return procManager;
	}
	
	public GTGeometry getGeometry() {
		return geometryManager.getGeometry();
	}

	public List<Turtle> getFocusedTurtles() {
		return turtleManager.getFocusedTurtles();
	}

	public double convertToRadians(double x) {
		return numberParser.convertToRadians(x);
	}

	public void addUserFunction(SourceLocalization keyLoc, Vector<SourceLocalization> argsLoc,  String rhs) throws GTException {
		String key = keywordManager.testValidity(keyLoc, Filter.OMIT_FUNCTION);
		
		int len = argsLoc.size();
		String[] args = new String[len];
		for (int idx = 0; idx < len; idx++) 
			args[idx] = keywordManager.testValidity(argsLoc.elementAt(idx));
		
		numberParser.addUserFunction(this, key, rhs, args);
	}

	public void vg() {
		turtleManager.resetTurtles();
		repaintIfNeeded();
	}
	
	public void resetGeometry() {
		graphicSpace.resetGeometry();
	}

	public void repaint() {
		graphicSpace.repaint();
	}
	
	public void repaintIfNeeded() {
		if (realTimePainting.getValue())
			repaint();
	}

	public GTRendererI getRenderer() {
		return graphicSpace.getRenderer();
	}
	

	public GTPerspective getPerspective() {
		return graphicSpace.getPerspective();
	}

	public MouseManager getMouseManager() {
		return geometryManager.getMouseManager();
	}

	public Turtle getTurtleAt(GTCommandBundle bundle, int pos) throws GTException, GTInterruptionException {
		SourceLocalization loc = bundle.getLocalizationAt(pos);

		JObjectI<?> o = getJObject(loc);
		
		int idx;
		try {
			idx = JEP2.getInteger(o);
		} catch (JEP2Exception ex) {
			throw new GTException(ex, loc);
		} catch (JEPException ex) {
			throw new GTException(ex, loc);
		} catch (MathException e) {
			throw new GTException(GTTrouble.GTJEP_TURTLE_INDEX, loc, o+"");
		}
		
		try {
			return turtleManager.getTurtle(idx);
		} catch (NoSuchTurtleException e) {
			throw new GTException(GTTrouble.GTJEP_TURTLE_INDEX, loc, idx+"");
		}
	}
	
	public Turtle[] getTurtles(SourceLocalization bundle, JList list) throws GTException, GTInterruptionException {
		int len = list.len();
		Turtle[] turtles = new Turtle[len];
		JObjectsVector vector = list.getItems();
		int[] indices = new int[len];
		int idx = 0;
		while (idx < len)
			try {
				indices[idx] = JEP2.getInteger(vector.elementAt(idx));
				idx++;
			} catch (JEP2Exception ex) {
				throw new GTException(ex, bundle);
			} catch (JEPException ex) {
				throw new GTException(ex, bundle);
			} catch (MathException e) {
				throw new GTException(GTTrouble.GTJEP_TURTLE_INDEX, bundle, indices[idx]+"");
			} 
		
		int index = -1;
		try {
			for (idx = 0; idx < len; idx++) { 
				index = indices[idx];
				turtles[idx] = turtleManager.getTurtle(index);
			}
		} catch (NoSuchTurtleException e) {
			throw new GTException(GTTrouble.GTJEP_TURTLE_INDEX, bundle, index+"");
		}
		
		return turtles;
	}

	public void setFocusOnAllTurtles() {
		turtleManager.setFocusOnAllTurtles();
	}

	public void setFocusedTurtle(Turtle[] turtles) {
		turtleManager.setFocusedTurtle(turtles);
	}

	public Library getLibrary() {
		return library;
	}

	public void launchExecution(GTCommandBundles bundles, Runnable finalJob) {
		processor.launchExecution(bundles, finalJob);
	}

	public void init() {
		GTGeometry g = getGeometry();
		numberParser.init(g, turtleManager);
		int d = g.getDimensionCount();
		commandFactory.setDimensionCommands(d);
		optionalCommands.update();
	}
	
	public void removeItem(SourceLocalization bundle) throws GTException, GTInterruptionException {
		numberParser.removeItem(bundle);
	}
	
	private void assignVariable(String key, SourceLocalization bundle) throws GTException, GTInterruptionException, JEPException {
		numberParser.assignVariable(this, key, bundle);
	}

	private class AskMechanism implements StringHandler {
		private final Object processMonitor = new Object();
		private final Thread thread;
		private String value = null;
		private boolean shouldWait = true;
		private boolean validationPerformed = false; 
		
		public AskMechanism(final GTCommandBundle bundle, final String key) {
			this.thread = new Thread() {
				@Override
				public void run() {
					synchronized (processMonitor) {
						while (shouldWait) {
							try {
								processMonitor.wait();
							} catch (InterruptedException ex) {
								ex.printStackTrace();
							}
						}
					}
					
					
					if (value!=null)
						if (value.length()==0)
							value = null;
						else 
							try {
								assignVariable(key, SourceLocalization.create(value, bundle.getTopLevelAncestor()));
							} catch (JEPException e) {
								e.printStackTrace();
							} catch (GTException ex) {
								value = null;
								ex.displayDialog();
							} catch (GTInterruptionException e) {
							}
				}
			};
			thread.start();
		}
		
		public void handle(String val) {
			validationPerformed = true;
			shouldWait = false;
			value = val;
			synchronized (processMonitor) {
				processMonitor.notify();
			}
		}

		public void giveUp() {
			shouldWait = false;
			synchronized (processMonitor) {
				processMonitor.notify();
			}
			if (!validationPerformed)
				interrupt();
		}
		
		public void join() {
			try {
				thread.join();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		};
	}
	
	public void askFor(final GTCommandBundle bundle, SourceLocalization keyLoc, String msg_) throws GTException {
		keywordManager.testValidity(keyLoc);
		
		String key = keyLoc.getText();
		final AskMechanism mechanism = new AskMechanism(bundle, key);
		
		final String msg = (msg_.length()>0) ? msg_ : PROMPT_MSG.translate(key); 
		final Window owner = bundle.getTopLevelAncestor();
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				GTDialog.show(owner, PROMPT, msg, mechanism);
			}
		});
		
		mechanism.join();
		
		if (mechanism.value == null) {
			if (mechanism.validationPerformed) 
				askFor(bundle, keyLoc, msg_);
			else
				interrupt();
		}
				
	}
	
	/*
	 * XML
	 */
	
	public String getXMLTag() {
		return "CommandManager"; // Backward Compatibility
	}

	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		waitingTime.storeValue(e);
		realTimePainting.storeValue(e);
		e.put(numberParser); 
		e.put(optionalCommands);
		return e;
	}

	public XMLReader loadXMLProperties(XMLReader e) {
		interrupt();
		XMLReader child = e.popChild(this);
		waitingTime.fetchValue(child, 100);
		realTimePainting.fetchValue(child, true);
		numberParser.loadXMLProperties(child);
		optionalCommands.loadXMLProperties(child);
		return child;
	}
	
	/*
	 * FWS 
	 */

	@Override
	public JPanel getSettingsPane(FWSettingsActionPuller actions) {
		JCheckBox realTimeCB =  realTimePainting.getComponent(new FWParameterListener<Boolean>() {
			@Override
			public void settingsChanged(Boolean value) {
				waitingTime.setEnabled(value);
			}
		});
		waitingTime.setEnabled(realTimePainting.getValue());
		
		return VerticalPairingLayout.createPanel(10, 10, 
				new FWLabel(REAL_TIME_PAINTING, SwingConstants.RIGHT), realTimeCB,
				new FWLabel(DELAY, SwingConstants.RIGHT), waitingTime.getComponent());
	}

	public FWSettings getJep() {
		return numberParser;
	}
	
	public BufferedImage getImage() {
		return graphicSpace.getImage();
	}


	private Stack<UndoableAction> undoableActions = new Stack<UndoableAction>();
	
	public void addUndoableAction(UndoableAction action) {
		undoableActions.push(action);
	}
	
	public void undo() {
		if (!undoableActions.isEmpty())
			undoableActions.pop().undo();
	}
	
	public void writePicture(BufferedImage image, String name) {
		if (pictureWriter != null) // no photo in GTModelManager
			pictureWriter.writePicture(image, name);
	}

	/**
	 * @return
	 */
	public FWSettings getPictureWriter() {
		return pictureWriter;
	}

	public JComponent getBoardPane() {
		return board.getPane();
	}
	
	public JComponent getMonitorPane() {
		return numberParser.getMonitorPane();
	}

	public void addToBoard(GTCommandBundle bundle) throws GTException, GTInterruptionException {
		int count = bundle.getArgumentsCount();
		if (count == 0) {
			board.clear();
			return;
		}

		if (count == 1) {
			SourceLocalization loc = bundle.getLocalizationAt(1);
//			if (loc.getText().equals("moniteur"))
//				if (assistant != null)
//					assistant.showMonitor();				
			JObjectI<?> o = getJObject(loc);
			addComputationToBoard(loc, o);
		} else {
			try { // try to display whole bundle
				SourceLocalization loc = bundle.getLocalizationSince(1);
				JObjectI<?> o = getJObject(loc);
				addComputationToBoard(loc, o);
				return;
			} catch (GTException ex) {
			}

			for (int idx = 1; idx <= count; idx++) { // display each piece
				SourceLocalization loc = bundle.getLocalizationAt(idx);
				JObjectI<?> o = getJObject(loc);
				addComputationToBoard(loc, o);
			}
		}
	}
	
	private void addComputationToBoard(SourceLocalization loc, JObjectI<?> o) {
		JEP2Type type = o.getType();
		if (type == JEP2Type.NULL || type == JEP2Type.ASSIGNMENT)
			return;
		
		String v = numberParser.format(o);
		String text = loc.getText() + " = " + v;
		board.add(text, numberParser.getProcedureArguments(), numberParser.getLoopVarNames());
		if (assistant != null)
			assistant.showBoard();			
	}
	
	public String format(JObjectI<?> o) {
		return numberParser.format(o);
	}
	
	public void addJObjectToBoard(SourceLocalization loc, JObjectI<?> o) {
		if (o.getType() == JEP2Type.STRING) {
			JString js = (JString) o;
			board.add(js.getValue(), null, null); // verbatim
		} else 
			addComputationToBoard(loc, o);
	}
	
	public void setBoardAssistant(GTPanelAssistant pane) {
		this.assistant = pane;
	}

	
	public TextStyle getGTStringStyle() {
		return displayManager.getStyle();
	}

	/**
	 * @param localVars
	 * @param bundle
	 * @throws GTInterruptionException 
	 * @throws GTException 
	 * @throws JEPException 
	 */
	void openLocalParser(String[] varNames, JObjectI<?>[] values) throws GTException, GTInterruptionException, JEPException {
		numberParser.openLocalParser(varNames, values);
	}

	/**
	 * 
	 */
	void closeLocalParser() {
		numberParser.closeLocalParser();
	}
	
	
	public void addLoopVariable(String name, Object o) {
		numberParser.addTempVariable(name, o);
	}
	

	public void removeLoopVariable(String varName) {
		numberParser.removeLoopVariable(varName);		
	}
	
	
	public JObjectI<?> getJObjectAt(GTCommandBundle bundle, int pos) throws GTException, GTInterruptionException{
		SourceLocalization loc = bundle.getLocalizationAt(pos);
		return numberParser.getJObject(this, loc);
	}
	
	/**
	 * @param bundle
	 * @return
	 * @throws GTInterruptionException 
	 * @throws JEPException 
	 */
	public JObjectI<?> getJObject(SourceLocalization bundle) throws GTException, GTInterruptionException {
		return numberParser.getJObject(this, bundle);
	}

	/**
	 * @param loc
	 * @throws GTException 
	 */
	public void declareGlobal(SourceLocalization loc) throws GTException {
		String name = keywordManager.testValidity(loc);
		numberParser.declareGlobal(name);
	}

	/**
	 * @param c
	 * @return
	 */
	public boolean displayHelp(GTCommandBundle bundle) {
		String text = bundle.getText();
		int offset = text.indexOf('?');
		if (offset<0)
			return false;
		
		Window owner = bundle.getTopLevelAncestor();
		
		// General help
		if (text.equals("?")) {
			GTHelp.displayHelp(owner);
			return true;
		}
		
		String key = bundle.getKey();
		
		// Command
		GTPrimitiveCommand command = commandFactory.getCommand(key);
		if (command != null) {
			GTHelp.displayHelp(owner, command);
			return true;
		}
		
		// Function
		PostfixMathCommandI fun =  numberParser.getFunctionTable().get(key);
		if (fun == null)
			return false;

		if (fun instanceof JFunction) {	
			GTHelp.displayHelp(owner, (JFunction) fun);
			return true;
		}
		
		return false;

	}

	/**
	 * @param d
	 */
	public void setWaitingTime(int d) {
		if (d<0)
			realTimePainting.getComponent().setSelected(false);
		else {
			if (!realTimePainting.getValue())
				realTimePainting.getComponent().setSelected(true);
			waitingTime.getComponent().setValue(d);
		}
	}

	/**
	 * @throws NoMidiException 
	 * 
	 */
	public synchronized void play(SourceLocalization loc) throws NoMidiException {
		if (midi == null) 
			throw new NoMidiException();
		
		loc.highlight(false);
		midi.play();
	}
	
	/**
	 * @param vec
	 * @param channel
	 * @throws NoMidiException 
	 */
	public void setMidiScore(List<MusicEvent> notes) throws NoMidiException {
		if (midi == null)
			throw new NoMidiException();
		
		midi.init();
		try {
			midi.putIn(notes, 0);
		} catch (MidiChannelException ex) {
			ex.printStackTrace(); // cannot occur
		}	
	}
	
	public static class NoMidiException extends Exception {
		private static final long serialVersionUID = -4245795938672276368L;
		
	}
	
	/**
	 * @param focusedTurtles
	 * @throws NoMidiException 
	 * @throws NoScoreException 
	 * @throws MidiChannelException 
	 */
	public void setMidiScores(List<Turtle> focusedTurtles) throws NoMidiException, NoScoreException, MidiChannelException {
		if (midi == null)
			throw new NoMidiException();
		
		midi.init();
		int idx = 0;
		for (Turtle turtle : focusedTurtles) {
			midi.putIn(turtle.getScore(), idx);
			idx++;
		}
	}

	/**
	 * @param fileName
	 */
	public void writeMidi(String fileName) {
		midi.write(fileName);
		
	}

	/**
	 * @return
	 */
	public FWSettings getMidi() {
		return midi;
	}
	
	public FWSettings getUserFileManager() {
		return userFileManager;
	}

	public GTOptionalCommands getOptionalCommands() {
		return optionalCommands;
	}
}
