package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.joml.*;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TerminalBlockEntity extends BlockEntity implements GeoBlockEntity
{
	float displayVisibility = 0f, inactivity = 600f;
	AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	UUID owner = UUID.fromString("4a23954b-551c-4e2b-ac52-eb2e1ccbe443");
	List<WingedPlayerEntity> focusedPlayers = new ArrayList<>();
	List<String> lines = new ArrayList<>();
	int textColor, colorOverride = -1;
	Vector2d cursor = new Vector2d(0f, 0f);
	String lastHovered;
	Tab tab = Tab.MAIN_MENU;
	Vector2f normalWindowSize = new Vector2f(100f, 100f), curWindowSize = new Vector2f(normalWindowSize), sizeOverride = null;
	
	public TerminalBlockEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.TERMINAL, pos, state);
		textColor = 0xffff9dff;
		lines.add("+--------------+");
		lines.add("  yaya's Terminal");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("+--------------+");
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
	
	}
	
	public void onHit()
	{
		inactivity = 0f;
		if(lastHovered != null)
		{
			switch(lastHovered)
			{
				case "customize" -> setTab(Tab.CUSTOMIZATION);
				case "bestiary" -> setTab(Tab.COMING_SOON);
				case "weapons" -> setTab(Tab.WEAPONS);
				case "mainmenu" -> setTab(Tab.MAIN_MENU);
				default -> Ultracraft.LOGGER.error("Undefined Behavior for Terminal button '" + lastHovered + "'");
			}
		}
	}
	
	void onBlockBreak()
	{
		for (WingedPlayerEntity p : focusedPlayers)
			p.setFocusedTerminal(null);
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	public float getRotation()
	{
		if(!getWorld().getBlockState(getPos()).isOf(BlockRegistry.TERMINAL_DISPLAY))
			return 0;
		return switch(getWorld().getBlockState(getPos()).get(TerminalDisplayBlock.FACING))
		{
			default -> 0;
			case EAST -> 90;
			case SOUTH -> 180;
			case WEST -> 270;
		};
	}
	
	public void setDisplayVisibility(float f)
	{
		displayVisibility = MathHelper.clamp(f, 0f, 1f);
		if(getWorld().getBlockState(getPos()).isOf(BlockRegistry.TERMINAL_DISPLAY))
			getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(TerminalDisplayBlock.GLOWS,displayVisibility > 0.25f));
		if(displayVisibility == 0f && !tab.equals(Tab.MAIN_MENU))
			setTab(Tab.MAIN_MENU);
	}
	
	public float getDisplayVisibility()
	{
		return displayVisibility;
	}
	
	public List<String> getLines()
	{
		return lines;
	}
	
	public int getTextColor()
	{
		if(colorOverride != -1)
			return colorOverride;
		return textColor;
	}
	
	public float getInactivity()
	{
		return inactivity;
	}
	
	public void setInactivity(float f)
	{
		if(!tab.equals(Tab.MAIN_MENU))
			f = 0f;
		inactivity = MathHelper.clamp(f, 0f, 600f);
		if(inactivity > 30f)
			lastHovered = null;
	}
	
	public boolean isFocused(WingedPlayerEntity p)
	{
		return focusedPlayers.contains(p);
	}
	
	public void unFocus(WingedPlayerEntity p)
	{
		focusedPlayers.remove(p);
	}
	
	public UUID getOwner()
	{
		return owner;
	}
	
	public void setCursor(Vector2d cursor)
	{
		this.cursor = new Vector2d(MathHelper.clamp(cursor.x, -curWindowSize.x / 200f + 0.5, Math.max(curWindowSize.x / 100f - 0.5, 1)), MathHelper.clamp(cursor.y, 0, curWindowSize.y / 100f));
	}
	
	public Vector2d getCursor()
	{
		return cursor;
	}
	
	public String getLastHovered()
	{
		return lastHovered;
	}
	
	public void setLastHovered(String lastHovered)
	{
		this.lastHovered = lastHovered;
	}
	
	public void setTab(Tab tab)
	{
		this.tab = tab;
		switch(tab)
		{
			case MAIN_MENU ->
			{
				colorOverride = -1;
				sizeOverride = null;
			}
			case COMING_SOON -> colorOverride = 0xffffff00;
			case WEAPONS, BESTIARY -> sizeOverride = new Vector2f(200, 100);
		}
	}
	
	public Tab getTab()
	{
		return tab;
	}
	
	public Vector2f getCurWindowSize()
	{
		return curWindowSize;
	}
	
	public void setCurWindowSize(Vector2f curWindowSize)
	{
		this.curWindowSize = curWindowSize;
	}
	
	public Vector2f getSizeOverride()
	{
		return sizeOverride;
	}
	
	public Vector2f getNormalWindowSize()
	{
		return normalWindowSize;
	}
	
	public enum Tab
	{
		MAIN_MENU,
		COMING_SOON,
		WEAPONS,
		BESTIARY,
		CUSTOMIZATION
	}
}
