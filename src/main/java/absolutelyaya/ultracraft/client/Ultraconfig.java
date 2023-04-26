package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.rendering.UltraHudRenderer;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = Ultracraft.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/stone_bricks.png")
public class Ultraconfig implements ConfigData
{
	@ConfigEntry.Gui.Tooltip
	public boolean serverJoinInfo = true;
	@ConfigEntry.Gui.Tooltip(count = 2)
	public boolean freezeVFX = true;
	@ConfigEntry.Gui.Tooltip
	public boolean fishingJoke = true;
	@ConfigEntry.Gui.Tooltip(count = 3)
	public UltraHudRenderer.UltraHudVisibility ultraHudVisibility = UltraHudRenderer.UltraHudVisibility.ALWAYS;
	@ConfigEntry.Gui.Tooltip
	public boolean moveUltrahud = true;
	@ConfigEntry.Gui.Tooltip
	public boolean bloodOverlay = true;
	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.BoundedDiscrete(min = 16, max = 128)
	public int maxTrails = 64;
	@ConfigEntry.Gui.Tooltip(count = 2)
	public boolean trailParticles = true;
	@ConfigEntry.Category("goop")
	@ConfigEntry.Gui.Tooltip
	public boolean fancyGoop = false;
	@ConfigEntry.Category("goop")
	@ConfigEntry.Gui.Tooltip
	public boolean wrapToEdges = false;
	@ConfigEntry.Gui.Excluded
	public String BGID = "ultracraft";
	@ConfigEntry.Category("debug")
	@ConfigEntry.Gui.Tooltip
	public boolean trailLines = false;
	
	public String lastVersion = "none";
	public boolean startedBefore = false;
}
