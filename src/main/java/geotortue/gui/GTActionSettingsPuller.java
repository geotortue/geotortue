/**
 * 
 */
package geotortue.gui;

import fw.gui.FWSettingsAction;
import fw.gui.FWSettingsActionPuller;
import geotortue.core.GTProcessingContext;
import geotortue.core.TurtleManager;
import geotortue.renderer.GTGraphicSpace;

public class GTActionSettingsPuller extends FWSettingsActionPuller {
	
	public final static FWSettingsActionKey 
		UPDATE_GEOMETRY = new FWSettingsActionKey(), 
		UPDATE_RENDERER = new FWSettingsActionKey(), 
		UPDATE_PERSPECTIVE = new FWSettingsActionKey();
	
	
	public GTActionSettingsPuller(final GTGraphicSpace graphicSpace, final TurtleManager turtleManager, 
			final GTProcessingContext processingContext) {
		
		register(FWSettingsActionPuller.REPAINT, new FWSettingsAction() {
			@Override
			public void fire() {
				graphicSpace.repaint();
			}
		});

		register(UPDATE_GEOMETRY, new FWSettingsAction() {
			@Override
			public void fire() {
				turtleManager.resetTurtles();
				graphicSpace.update();
				graphicSpace.resetGeometry();
				processingContext.init();
			}
		});
		
		register(UPDATE_RENDERER, new FWSettingsAction() {
			@Override
			public void fire() {
				graphicSpace.updateRenderer();
			}
		});

		register(UPDATE_PERSPECTIVE, new FWSettingsAction() { 
			@Override
			public void fire() {
				graphicSpace.update();
			}
		});
		
	}


}
