/**
 * 
 */
package fw.text;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 *	Non significant UndoableEdit 
 *
 */
public class FWUndoableEdit implements UndoableEdit {
	private final UndoableEdit edit;

	public FWUndoableEdit(UndoableEdit edit) {
		this.edit = edit;
	}

	public void undo() throws CannotUndoException {
		edit.undo();
	}

	public boolean canUndo() {
		return edit.canUndo();
	}

	public void redo() throws CannotRedoException {
		edit.redo();
	}

	public boolean canRedo() {
		return edit.canRedo();
	}

	public void die() {
		edit.die();
	}

	public boolean addEdit(UndoableEdit anEdit) {
		return edit.addEdit(anEdit);
	}

	public boolean replaceEdit(UndoableEdit anEdit) {
		return edit.replaceEdit(anEdit);
	}

	public boolean isSignificant() {
		return false;
	}

	public String getPresentationName() {
		return edit.getPresentationName();
	}

	public String getUndoPresentationName() {
		return edit.getUndoPresentationName();
	}

	public String getRedoPresentationName() {
		return edit.getRedoPresentationName();
	}
}