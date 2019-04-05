package geotortue.core;

import java.awt.Window;

public interface SourceProvider {
	
	public Window getTopLevelAncestor();

	public String getText(int offset, int length);

	public boolean highlight(int offset, int len, boolean error);
	
}
