package geotortue.core;

import java.awt.Window;

public class SourceLocalization {
	
	private final SourceProvider provider;
	private final int offset, len ;
	
	public SourceLocalization(SourceProvider owner, int offset, int len) {
		this.provider = owner;
		this.offset = offset;
		this.len = len;
		if (offset<0 || len <0)
			new Exception("bad offset = "+offset+"or bad len = "+len).printStackTrace();
	}

	public SourceProvider getProvider() {
		return provider;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return len;
	}
	
	public boolean highlight(boolean error) {
		return provider.highlight(offset, len, error);
	}
	
	public String getText() {
		return provider.getText(offset, len);
	}
	
	public String getRawText() { 
		return provider.getText(offset, len);
	}

	public String toString() {
		return provider+" "+getRawText();
	}

	public Window getTopLevelAncestor() {
		return provider.getTopLevelAncestor();
	}

	public static SourceLocalization create(final String str, final Window owner) {
		SourceProvider provider = new SourceProvider() {
			
			@Override
			public boolean highlight(int offset, int len, boolean error) {
				return false;
			}
			
			@Override
			public Window getTopLevelAncestor() {
				return owner;
			}
			
			@Override
			public String getText(int offset, int length) {
				return str.substring(offset, offset+length);
			}
		};
		return new SourceLocalization(provider, 0, str.length());
	}

	public SourceLocalization getSubLocalization(int idx, int len) {
		return new SourceLocalization(provider, offset+idx, len);
	}

	public SourceLocalization getSubLocalization(int idx) {
		return getSubLocalization(idx, len-idx);
	}
	
	public SourceLocalization trim() {
		int start = 0;
		int end = len-1;
		
		String content = getRawText();
		char c = content.charAt(start); 
		while (Character.isWhitespace(c) && start<end) { 
			start ++;
			c = content.charAt(start);
		}
		
		c = content.charAt(end);
		while (Character.isWhitespace(c) && start<end) { 
			end--;
			c = content.charAt(end);
		}
		
		return getSubLocalization(start, end-start+1);
	}
}