package geotortue.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Stack;

import fw.app.FWAction;
import fw.app.FWAction.ActionKey;
import geotortue.core.GTMessageFactory.GTTrouble;
import geotortue.core.ThreadQueue.ThreadQueueListener;
import sound.GTMidiI;
import type.JObjectI;
import type.JObjectI.JEP2Type;

public class GTCommandProcessor {
	
	private static final ActionKey INTERRUPT = new ActionKey(GTCommandProcessor.class,  "interrupt");
	private static final ActionKey SUSPEND = new ActionKey(GTCommandProcessor.class,  "suspend");
	private static final ActionKey RESUME = new ActionKey(GTCommandProcessor.class,  "resume");
	private static final ActionKey STEP = new ActionKey(GTCommandProcessor.class,  "step");

	private final GTProcessingContext context;
	
	private boolean interrupted = false;
	private boolean suspended = false;
	private boolean step = false;
	private int pause = -1;
	
	private Thread ownerThread;
	private final Object processMonitor = new Object();

	private ThreadQueue queue =  new ThreadQueue();
	
	private GTMidiI midi = null;
	
	private GTCommandBundle processedBundle;
	private final GTCommandFactory commandFactory;
	
	public GTCommandProcessor(final GTProcessingContext context, final GTMidiI m, final GTCommandFactory cf) {
		this.context = context;
		actionInterrupt.setEnabled(false);
		actionSuspend.setEnabled(false);
		actionStep.setEnabled(false);
		actionResume.setEnabled(false);
		this.midi = m;
		this.commandFactory = cf;
	}
	
	public FWAction[] getActions() {
		return new FWAction[]{ actionInterrupt, actionResume, actionStep, actionSuspend };
	}
	
	private final FWAction actionInterrupt = new FWAction(INTERRUPT, 0, KeyEvent.VK_ESCAPE, "media-playback-stop.png",
		e -> interrupt());
	
	
	private FWAction actionSuspend = new FWAction(SUSPEND, 0, KeyEvent.VK_F9, "media-playback-pause.png",
		e -> suspend());

	private FWAction actionResume = new FWAction(RESUME, 0, KeyEvent.VK_F11, "media-playback-start.png",
		e -> resume());
	
	private FWAction actionStep = new FWAction(STEP, 0, KeyEvent.VK_F9, "media-playback-step.png",
		e -> step());
	
	@SuppressWarnings("java:S1604")
	public void launchExecution(final GTCommandBundles bundles, final Runnable finalJob) {
		if (bundles.isEmpty()) {
			return;
		}
			
		queue.add(new Runnable() {
			public void run() {
				try {
					execute(bundles, context);
				} catch  (GTInterruptionException ex) {
					// do nothing
				} catch (Exception ex) {
					ex.printStackTrace();
				} catch (StackOverflowError err){
					System.gc();
					new GTException(GTTrouble.GTJEP_STACK_OVERFLOW, bundles.firstElement()).displayDialog();
				} finally {
					context.repaint();
					interrupt();
					finalJob.run();
					GTException.forget();
				}
			}
		});
		queue.start();
	}
	
	private void execute(final GTCommandBundles bundles, @SuppressWarnings("unused") final GTProcessingContext context) throws GTInterruptionException {
		interrupted = false;
		actionInterrupt.setEnabled(true);
		actionSuspend.setEnabled(true);
		ownerThread = Thread.currentThread();
		
		stack.clear();
		stack.push(bundles);
		
		processLoop();
	}
	
	public void suspend() {
		suspended = true;
		midi.pause();
		
		actionSuspend.setEnabled(false);
		actionStep.setEnabled(!midi.isOpen());
		actionResume.setEnabled(true);
	}
	
	private void resume() {
		step = false;
		synchronized(processMonitor) {
			suspended = false;
            processMonitor.notifyAll();
        }
		midi.resume();
		
		actionSuspend.setEnabled(true);
		actionStep.setEnabled(false);
		actionResume.setEnabled(false);
	}
	
	private void step() {
		step = true;
		synchronized(processMonitor) {
			suspended = false;
            processMonitor.notifyAll();
        }
	}

	void interrupt() {
		synchronized(processMonitor) {
			suspended = false;
			pause = -1;
            processMonitor.notifyAll();
        }
		midi.interrupt();
		
		interrupted = true;
		step = false;
		actionInterrupt.setEnabled(false);
		actionSuspend.setEnabled(false);
		actionStep.setEnabled(false);
		actionResume.setEnabled(false);
	}
	
	private synchronized void monitor(final GTCommandBundle bundle) // used by main thread
				throws GTException, GTInterruptionException {
		if (ownerThread != Thread.currentThread()) { // to avoid deadlock
			return;
		}
		
		if (suspended) {
			bundle.highlight(false);
		}
			
		synchronized (processMonitor) {
			while (suspended) {
				try {
					processMonitor.wait();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		if (pause > 0) 
			synchronized (processMonitor) {
				try {
					processMonitor.wait(pause);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				} finally {
					pause = -1;
				}
			}
		
		if (interrupted) {
			throw new GTInterruptionException();
		}
		
		if (step) {
			context.repaint();
			bundle.highlight(false);
			suspend();
		}
	}
	
	private final Stack<GTCommandBundles> stack = new Stack<>();
	
	private void processLoop() throws GTInterruptionException {
		try {
			while (!stack.isEmpty()) 
				processBundles(stack.pop());
		} catch (GTException ex) {
			if (!context.displayHelp(processedBundle)) {
				ex.displayDialog();
			}
		}
	}

	public JObjectI<?> process(final GTCommandBundles bundles) throws GTException, GTInterruptionException {
		stack.push(bundles);
		return processBundles(bundles);
	}
	
	
	private JObjectI<?> processBundles(final GTCommandBundles bundles) throws GTException, GTInterruptionException {
		while (!bundles.isEmpty()) {
			GTCommandBundle b = bundles.pop();
			try {
				
				JObjectI<?> o = process(b, bundles.isValueRequired());
				if (o != null) {
					bundles.clear();
					return o;
				}
			} catch (GTException ex) {
				this.processedBundle = b;
				throw ex;
			}
			
		}
		return null;
	}
	
	private JObjectI<?> process(final GTCommandBundle bundle, final boolean isValueRequired) throws GTException, GTInterruptionException {
		monitor(bundle);
		try {
			return processUnsync(bundle, isValueRequired);
		} catch (GTProcessException e) {
			if (!isValueRequired) {
				// try to parse whole bundle : display result if any 
				try {
					JObjectI<?> o = context.getJObject(bundle);
					if (o != null) {
						context.addJObjectToBoard(bundle, o);
					}
    				return null;
				} catch (GTException ex) {
					ex.keep();
					throw new GTException(GTTrouble.GTJEP_NO_SUCH_COMMAND, bundle, bundle.getRawText());
				}
			}

			// value required : parse whole bundle
			JObjectI<?> o = context.getJObject(bundle);
			if (o == null) {
				return null;
			}

			JEP2Type t = o.getType();
			if (t == JEP2Type.ASSIGNMENT || t == JEP2Type.NULL) {
				return null;
			}
			
			return o;
		}
	}
	
	private JObjectI<?> processUnsync(final GTCommandBundle bundle, final boolean isValueRequired) 
			throws GTException, GTInterruptionException, GTProcessException {
		String key = bundle.getKey();

		// Command
		GTPrimitiveCommand command = commandFactory.getCommand(key);
		if (command != null) { 
			return command.execute(bundle, context);
		}
		
		// Procedure
		Procedure proc = context.getProcedureManager().getProcedure(key);
		if (proc != null) { 
			JObjectI<?> o = proc.execute(bundle, context);
			return isValueRequired ? o : null;
		}

		// Library
		proc = context.getLibrary().getProcedure(key);
		if (proc != null) { 
			JObjectI<?> o = proc.execute(bundle, context);
			return isValueRequired ? o : null;
		}

		throw new GTProcessException();
	}
	
	public void sleep(final int timeout) {
		pause = Math.min(timeout, 2000);
	}
	
	public void addThreadQueueListener(final ThreadQueueListener l) {
		queue.addListener(l);
	}
	
	public static class GTInterruptionException extends Exception {
		private static final long serialVersionUID = -8773545091684735600L;
	}
	
	
	private static class GTProcessException extends Exception {
		private static final long serialVersionUID = 8251074708217585812L;
	}
}