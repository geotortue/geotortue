package fw.geometry.proj;


public class PerspectiveManager {

	private final PerspectiveI[] availablePerspectives;
	private PerspectiveI selectedPerspective;
	
	private int index = 0;
	
	public PerspectiveManager(PerspectiveI... ps) {
		availablePerspectives = ps;
		selectedPerspective = availablePerspectives[0];
	}
	
	public PerspectiveI getPerspective() {
		return selectedPerspective;
	}

	public PerspectiveI[] getAvailablePerspectives() {
		return availablePerspectives;
	}
	
	protected final void setPerspective(int idx) {
		if (idx == index)
			return;
		this.index = idx;
		selectedPerspective = availablePerspectives[idx];
	}
}
