/**
 * 
 */
package geotortue.gui;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import fw.app.Translator.TKey;
import fw.files.ProxyConfigurator;
import fw.gui.FWAccessibilityManager;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.FWTabbedPane;
import fw.gui.layout.VerticalFlowLayout;
import geotortue.core.GTDisplayManager;
import geotortue.core.GTOptionalCommands;
import geotortue.core.GTProcessingContext;
import geotortue.core.GeoTortue;
import geotortue.core.KeywordManager;
import geotortue.core.TurtleManager;
import geotortue.gallery.Gallery;
import geotortue.geometry.GTGeometryManager;
import geotortue.geometry.proj.GTPerspectiveManager;
import geotortue.renderer.GTGraphicSpace;
import geotortue.renderer.GTRendererManager;

/**
 *
 */
public class GTPreferences {

	private static final TKey ADVANCED = new TKey(GTPreferences.class, "advanced");
	private static final TKey SETTINGS_MANAGEMENT = new TKey(GTPreferences.class, "settings");
	private static final TKey FONTS_COLORS = new TKey(GTPreferences.class, "fontsAndColors");
	private static final TKey RENDERER = new TKey(GTPreferences.class, "renderer");
	private static final TKey MATHS = new TKey(GTPreferences.class, "maths");
	private static final TKey GRAPHICS = new TKey(GTPreferences.class, "graphics");
	private static final TKey TURTLES = new TKey(GTPreferences.class, "turtles");

	private static final ProxyConfigurator proxyConfigurator = new ProxyConfigurator(); 
	
	private final FWSettingsActionPuller actions;
	
	public GTPreferences(GTGraphicSpace graphicSpace, TurtleManager turtleManager, GTProcessingContext processingContext) {
		this.actions = new GTActionSettingsPuller(graphicSpace, turtleManager, processingContext);
	}
	
	public FWTabbedPane createPane(TurtleManager turtleManager, GTPerspectiveManager perspectiveManager, 
			GTGeometryManager geometryManager, GTDisplayManager displayManager, GTGraphicSpace graphicSpace,
			GTRendererManager rendererManager, GTProcessingContext processingContext, GeoTortue geotortue,
			GTOptionalCommands optionalCommands) {
		
		JPanel turtles = VerticalFlowLayout.createPanel(
				getSettingsPane(processingContext),
				getSettingsPane(turtleManager));
		JPanel graphics = VerticalFlowLayout.createPanel( 
				getSettingsPane(graphicSpace),
				getSettingsPane(geometryManager.getMouseManager()),
				getSettingsPane(displayManager)); 
		JPanel maths = VerticalFlowLayout.createPanel( 
				getSettingsPane(processingContext.getJep()),
				getSettingsPane(geometryManager));
		JPanel renderer = VerticalFlowLayout.createPanel( 
				getSettingsPane(perspectiveManager),
				getSettingsPane(rendererManager),
				getSettingsPane(rendererManager.getLightingContext()),
				getSettingsPane(rendererManager.getRendererSettings()));
		
		JPanel settingsManagement = (geotortue == null)
				? VerticalFlowLayout.createPanel(getSettingsPane(optionalCommands))
				: VerticalFlowLayout.createPanel(
						getSettingsPane(optionalCommands), 
						getSettingsPane(geotortue));

		JPanel[] panes = new JPanel[]{turtles, graphics, maths, renderer, settingsManagement};
		TKey[] keys= new TKey[]{TURTLES, GRAPHICS, MATHS, RENDERER, SETTINGS_MANAGEMENT};
		
		return new GTPreferencesPane(panes, keys);
	}
	
	public FWTabbedPane createPane(KeywordManager keywordManager, GTDisplayManager displayManager, 
			GTProcessingContext processingContext, Gallery gallery) {

		JPanel fontsAndColors = VerticalFlowLayout.createPanel(
				getSettingsPane(FWAccessibilityManager.getCommonInstance()),
				getSettingsPane(keywordManager),
				getSettingsPane(displayManager.getStyleSettings()));
		
		JPanel advanced = VerticalFlowLayout.createPanel(
				getSettingsPane(proxyConfigurator),
				getSettingsPane(gallery),
				getSettingsPane(processingContext.getPictureWriter()),
				getSettingsPane(processingContext.getMidi()),
				getSettingsPane(processingContext.getUserFileManager()));
		
		
		JPanel[] panes = new JPanel[]{fontsAndColors, advanced};
		TKey[] keys= new TKey[]{FONTS_COLORS, ADVANCED};
		
		return new GTPreferencesPane(panes, keys);
		
	}
	
	private JPanel getSettingsPane(FWSettings s) {
		JPanel p = s.getSettingsPane(actions);
		p.setBorder(BorderFactory.createTitledBorder(s.getTitle().translate()));
		return p;
	}
	
	private class GTPreferencesPane extends FWTabbedPane {
		private static final long serialVersionUID = 1557558946089208718L;

		/**
		 * @param panes
		 * @param keys
		 */
		private GTPreferencesPane(JPanel[] panes, TKey[] keys) {
			for (int idx = 0; idx < keys.length; idx++) {
				JScrollPane scrollPane = new JScrollPane(panes[idx]);
				scrollPane.getVerticalScrollBar().setUnitIncrement(16);
				scrollPane.setBorder(BorderFactory.createEmptyBorder());
				add(scrollPane, keys[idx].translate());
			}		
			
			Dimension size = getPreferredSize();
			size.width = 460;
			setPreferredSize(size);
		}

	}
}
