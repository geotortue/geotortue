package geotortue.geometry.proj;

import fw.geometry.proj.LinearPerspective;
import fw.geometry.proj.PerspectiveI;
import fw.geometry.proj.PerspectiveMatrix;
import fw.gui.FWSettingsActionPuller;
import fw.gui.params.FWParameterListener;


public abstract class GTLinearPerspective extends GTPerspective {
	private DelegateProjection delegate = new DelegateProjection();
	
	
	private class DelegateProjection extends LinearPerspective {
		@Override
		protected void setMatrix(PerspectiveMatrix m) {
			super.setMatrix(m);
		}
	}
	
	public GTLinearPerspective() {
	}
	
	protected abstract PerspectiveMatrix getMatrix();
	
	protected final void updateMatrix() {
		delegate.setMatrix(getMatrix());
	}
	
	@Override
	protected PerspectiveI getDelegateProjection() {
		return delegate;
	}
	
	protected FWParameterListener<Double> getupdateAction(final FWSettingsActionPuller actions) {
		return 	new FWParameterListener<Double>() {
			@Override
			public void settingsChanged(Double value) {
				updateMatrix();
				actions.fire(GTPerspectiveManager.UPDATE_PERSPECTIVE);
			}
		}; 
	}
};