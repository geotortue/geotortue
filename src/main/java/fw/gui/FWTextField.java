package fw.gui;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fw.text.TextStyle;


public class FWTextField extends JTextField implements DocumentListener, FWAccessible {
	private static final long serialVersionUID = -6289162380956497938L;
	private final FWTextFieldListener listener;
	
	public FWTextField(String text, int cols, FWTextFieldListener l) {
		super(text, cols);
		this.listener = l;
		this.getDocument().addDocumentListener(this);
		FWAccessibilityManager.register(this);
	}
	
	public FWTextField(String text, int cols) {
		this(text, cols, new FWTextFieldListener() {
			@Override
			public void textChanged(String text, DocumentEvent e) {
			}
		});
	}
	
	public void removeUpdate(DocumentEvent e) {
		listener.textChanged(getText(), e);
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		listener.textChanged(getText(), e);
	}
	
	@Override
	public void changedUpdate(DocumentEvent e) {
	}
	
	public interface FWTextFieldListener {
		public void textChanged(String text, DocumentEvent e);
	}
	
	@Override
	public void setFont(TextStyle s) {
		setFont(s.getFont());
	}
}