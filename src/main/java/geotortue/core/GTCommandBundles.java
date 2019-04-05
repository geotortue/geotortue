/**
 * 
 */
package geotortue.core;

import java.util.Stack;

/**
 * @author Salvatore Tummarello
 *
 */
public class GTCommandBundles {

	private final Stack<GTCommandBundle> originalStack = new Stack<>();
	private final Stack<GTCommandBundle> forUseStack = new Stack<>();

	public GTCommandBundle pop() {
		return forUseStack.pop();
	}

	public GTCommandBundle peek() {
		return forUseStack.peek();
	}

	public boolean isEmpty() {
		return forUseStack.isEmpty();
	}

	public void add(GTCommandBundle e) {
		originalStack.add(0, e);
		forUseStack.add(0, e);
	}

	public int size() {
		return forUseStack.size();
	}
	
	public GTCommandBundle firstElement() {
		return peek();
	}

	@Override
	public String toString() {
		String str = "";
		for (GTCommandBundle b : forUseStack) 
			str = b.getText()+" ; "+str;
		return str+isValueRequired;
	}

	/**
	 * 
	 */
	public void init() {
		forUseStack.clear();
		forUseStack.addAll(originalStack);
	}

	/**
	 * 
	 */
	public void clear() {
		forUseStack.clear();
		
	}

	private boolean isValueRequired = false;
	
	public void requireValue() {
		isValueRequired = true;
	}
	
	public boolean isValueRequired() {
		return isValueRequired;
	}

	/**
	 * @return
	 */
	public GTCommandBundles getCopy() { // TODO : redundant with init() call in GTProcessingContext.process()
		GTCommandBundles copy = new GTCommandBundles();
		copy.originalStack.addAll(originalStack);
		return copy;
	}
	
	

}
