package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TerminalBlockEntity extends BlockEntity implements GeoBlockEntity
{
	float displayVisibility;
	AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	UUID owner;
	List<WingedPlayerEntity> focusedPlayers = new ArrayList<>();
	List<String> lines = new ArrayList<>();
	int textColor;
	
	public TerminalBlockEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.TERMINAL, pos, state);
		textColor = 0xffff9dff;
		lines.add("+--------------+");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("  yaya's Terminal");
		lines.add("+--------------+");
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
	
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
		displayVisibility = f;
		if(getWorld().getBlockState(getPos()).isOf(BlockRegistry.TERMINAL_DISPLAY))
			getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(TerminalDisplayBlock.GLOWS,displayVisibility == 1f));
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
		return textColor;
	}
}
