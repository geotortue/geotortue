/**
 * 
 */
package fw.gui.params;

import java.util.ArrayList;

import javax.swing.JComponent;

public abstract class FWParameter<T, C extends JComponent> implements FWParameterI<T, C> {
	
	private T value;
	private final String xmlTag;
	private final ArrayList<FWParameterListener<T>> listeners = new ArrayList<>();
	

	public FWParameter(String tag, T v) {
		this.value = v;
		this.xmlTag = tag;
	}

	@Override
	public String getXMLTag() {
		return xmlTag;
	}

	@Override
	public T getValue() {
		return value;
	}
	
	protected boolean setValue(T v) {
		if (this.value == v || v == null)
			return false;
		this.value = v;
		for (FWParameterListener<T> l  : listeners) 
			l.settingsChanged(v);
		return true;
	}
	
	public void addParamaterListener(FWParameterListener<T> l) {
		if (l==null)
			new Exception().printStackTrace(); 
		listeners.add(l);
	}
	
	public C getComponent(FWParameterListener<T> l) {
		addParamaterListener(l);
		return getComponent();
	}
	

}
