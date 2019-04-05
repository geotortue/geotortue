/**
 * 
 */
package fw.text;

/**
 *
 */
public class FWScope {
	private final int start, end;

	public FWScope(int scopeStart, int scopeEnd) {
		this.start = scopeStart;
		this.end = scopeEnd;
	}
	
	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getLength() {
		return end-start;
	}

	public boolean contains(int offset) {
		return (start<=offset && offset<end);
	}
	
	public boolean overlaps(int s, int e) {
		return (e>start) && (s<end);
	}

	@Override
	public String toString() {
		return "("+start+", "+end+")";
	}

	public boolean equals(FWScope o) {
		if (o == null)
			return false;
		return (start == o.start) && (end == o.end);
	}
	
	
	
}
