package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.rendering.UltraHudRenderer;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
@Config(name = Ultracraft.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/stone_bricks.png")
public class Ultraconfig implements ConfigData
{
	@ConfigEntry.Gui.Tooltip
	public boolean serverJoinInfo = true;
	@ConfigEntry.Gui.Tooltip(count = 2)
	public boolean freezeVFX = true;
	@ConfigEntry.Category("ultra-hud")
	@ConfigEntry.Gui.Tooltip
	public boolean fishingJoke = true;
	@ConfigEntry.Category("ultra-hud")
	@ConfigEntry.Gui.Tooltip(count = 2)
	public UltraHudRenderer.UltraHudVisibility ultraHudVisibility = UltraHudRenderer.UltraHudVisibility.ALWAYS;
	@ConfigEntry.Category("ultra-hud")
	@ConfigEntry.Gui.Tooltip
	public boolean ultraHudCrosshair = true;
	@ConfigEntry.Category("ultra-hud")
	@ConfigEntry.Gui.Tooltip
	public boolean moveUltrahud = true;
	@ConfigEntry.Category("ultra-hud")
	@ConfigEntry.Gui.Tooltip
	public boolean switchSides = false;
	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.BoundedDiscrete(min = 16, max = 128)
	public int maxTrails = 64;
	@ConfigEntry.Gui.Tooltip(count = 2)
	public boolean trailParticles = true;
	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.BoundedDiscrete(min = 0, max = 100)
	public int slideCamOffset = 100;
	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.BoundedDiscrete(min = 0, max = 45)
	public int slideTilt = 4;
	@ConfigEntry.Gui.Tooltip
	public boolean movementSounds = true;
	@ConfigEntry.Category("blood")
	@ConfigEntry.Gui.Tooltip
	public boolean fancyGoop = false;
	@ConfigEntry.Category("blood")
	@ConfigEntry.Gui.Tooltip
	public boolean wrapToEdges = false;
	@ConfigEntry.Category("blood")
	@ConfigEntry.Gui.Tooltip
	public boolean bloodOverlay = true;
	@ConfigEntry.Category("blood")
	@ConfigEntry.Gui.Tooltip
	public boolean danganronpa = false;
	@ConfigEntry.Gui.Excluded
	public String BGID = "ultracraft";
	@ConfigEntry.Category("debug")
	@ConfigEntry.Gui.Tooltip
	public boolean trailLines = false;
	@ConfigEntry.Category("debug")
	@ConfigEntry.Gui.Tooltip
	public boolean showPunchArea = false;
	public boolean repeatIntro = false;
	
	@ConfigEntry.Gui.Excluded
	public String lastVersion = "none";
	@ConfigEntry.Gui.Excluded
	public boolean startedBefore = false;
}
