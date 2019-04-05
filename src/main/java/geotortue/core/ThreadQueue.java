package geotortue.core;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.LinkedList;

public class ThreadQueue {
	
	private LinkedList<Thread> queue;
	private boolean isRunning = false;
	private Thread currentThread;
	private ArrayList<ThreadQueueListener> listeners = new ArrayList<>();
	
	public ThreadQueue(){
		this.queue =  new LinkedList<Thread>();
	}
	
	public void addListener(ThreadQueueListener l) {
		listeners.add(l);
	}
	
	public void add(final Runnable r){
		queue.offer(new Thread() {
			public synchronized void run() {
				r.run();
				startNextThread();
			}
		});
	}

	public void start(){
		if (!isRunning)
			startNextThread();
	}
	
	private void startNextThread(){
		if (!isRunning)
			for (ThreadQueueListener listener : listeners) 
				listener.started();
				
		isRunning=true;
		currentThread = queue.poll();
		if (currentThread!=null)
			currentThread.start();
		else {
			isRunning=false;
			for (ThreadQueueListener listener : listeners)
				listener.stopped();
		}
	}
	
	public interface ThreadQueueListener extends EventListener {
		public void started();
		public void stopped();
	}
}