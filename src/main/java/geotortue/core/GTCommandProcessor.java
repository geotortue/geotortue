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
	private final Object process_monitor = new Object();

	private ThreadQueue queue =  new ThreadQueue();
	
	private GTMidiI midi = null;
	
	private GTCommandBundle processedBundle;
	private final GTCommandFactory commandFactory;
	
	public GTCommandProcessor(GTProcessingContext context, GTMidiI m, GTCommandFactory cf) {
		this.context = context;
		action_interrupt.setEnabled(false);
		action_suspend.setEnabled(false);
		action_step.setEnabled(false);
		action_resume.setEnabled(false);
		this.midi = m;
		this.commandFactory = cf;
	}
	
	public FWAction[] getActions() {
		return new FWAction[]{action_interrupt, action_resume, action_step, action_suspend};
	}
	
	private final FWAction action_interrupt = new FWAction(INTERRUPT, 0, KeyEvent.VK_ESCAPE, "media-playback-stop.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			interrupt();
		}
	});
	
	
	private FWAction action_suspend = new FWAction(SUSPEND, 0, KeyEvent.VK_F9, "media-playback-pause.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) { 
			suspend();
		}
	});

	private FWAction action_resume = new FWAction(RESUME, 0, KeyEvent.VK_F11, "media-playback-start.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) { 
			resume();
		}
	});
	
	private FWAction action_step = new FWAction(STEP, 0, KeyEvent.VK_F9, "media-playback-step.png", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			step();
		}
	});
	
	public void launchExecution(final GTCommandBundles bundles, final Runnable finalJob) {
		if (bundles.isEmpty()) 
			return;
			
		queue.add(new Runnable() {
			public void run() {
				try {
					execute(bundles, context);
				} catch  (GTInterruptionException ex) {
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
	
	private void execute(GTCommandBundles bundles, GTProcessingContext context) throws GTInterruptionException {
		interrupted = false;
		action_interrupt.setEnabled(true);
		action_suspend.setEnabled(true);
		ownerThread = Thread.currentThread();
		
		stack.clear();
		stack.push(bundles);
		
		processLoop();
	}
	
	public void suspend() {
		suspended = true;
		midi.pause();
		
		action_suspend.setEnabled(false);
		action_step.setEnabled(!midi.isOpen());
		action_resume.setEnabled(true);
	}
	
	private void resume() {
		step = false;
		synchronized(process_monitor) {
			suspended = false;
            process_monitor.notify();
        }
		midi.resume();
		
		action_suspend.setEnabled(true);
		action_step.setEnabled(false);
		action_resume.setEnabled(false);
	}
	
	private void step() {
		step = true;
		synchronized(process_monitor) {
			suspended = false;
            process_monitor.notify();
        }
	}

	void interrupt() {
		synchronized(process_monitor) {
			suspended = false;
			pause = -1;
            process_monitor.notify();
        }
		midi.interrupt();
		
		interrupted = true;
		step = false;
		action_interrupt.setEnabled(false);
		action_suspend.setEnabled(false);
		action_step.setEnabled(false);
		action_resume.setEnabled(false);
	}
	
	private synchronized void monitor(GTCommandBundle bundle) // used by main thread
				throws GTException, GTInterruptionException {
		if (ownerThread != Thread.currentThread()) // to avoid deadlock
			return;
		
		if (suspended) 
			bundle.highlight(false);
			
		synchronized (process_monitor) {
			while (suspended) {
				try {
					process_monitor.wait();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		if (pause>0) 
			synchronized (process_monitor) {
				try {
					process_monitor.wait(pause);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				} finally {
					pause = -1;
				}
			}
		
		if (interrupted)
			throw new GTInterruptionException();
		
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
			if (!context.displayHelp(processedBundle))
				ex.displayDialog();
		}
	}

	public JObjectI<?> process(GTCommandBundles bundles) throws GTException, GTInterruptionException {
		stack.push(bundles);
		return processBundles(bundles);
	}
	
	
	private JObjectI<?> processBundles(GTCommandBundles bundles) throws GTException, GTInterruptionException {
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
	
	private JObjectI<?> process(GTCommandBundle bundle, boolean isValueRequired) throws GTException, GTInterruptionException {
		monitor(bundle);
		try {
			return process_unsync(bundle, isValueRequired);
		} catch (GTProcessException e) {
			if (isValueRequired) {
				// value required : parse whole bundle
				JObjectI<?> o = context.getJObject(bundle);
				if (o != null) { 
					JEP2Type t = o.getType();
					if (t != JEP2Type.ASSIGNMENT && t!=JEP2Type.NULL) 
						return o;
				}
			} else {
				// try to parse whole bundle : display result if any 
				try {
					JObjectI<?> o = context.getJObject(bundle);
					if (o != null) {
						context.addJObjectToBoard(bundle, o);
					}
				} catch (GTException ex) {
					ex.keep();
					throw new GTException(GTTrouble.GTJEP_NO_SUCH_COMMAND, bundle, bundle.getRawText());
				}
			}
			return null;
		}
	}
	
	private JObjectI<?> process_unsync(GTCommandBundle bundle, boolean isValueRequired) 
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
	
	public void sleep(int timeout) {
		pause = Math.min(timeout, 2000);
	}
	
	public void addThreadQueueListener(ThreadQueueListener l) {
		queue.addListener(l);
	}
	
	public static class GTInterruptionException extends Exception {
		private static final long serialVersionUID = -8773545091684735600L;
	}
	
	
	private static class GTProcessException extends Exception {
		private static final long serialVersionUID = 8251074708217585812L;
	}
}