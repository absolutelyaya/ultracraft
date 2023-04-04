package absolutelyaya.ultracraft.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.joml.Vector2i;

public abstract class AbstractWeaponItem extends Item
{
	public AbstractWeaponItem(Settings settings)
	{
		super(settings);
	}
	
	public abstract void onPrimaryFire(World world, PlayerEntity user);
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
	}
	
	public abstract Vector2i getHUDTexture();
	
	public boolean shouldAim()
	{
		return true;
	}
	
	public boolean shouldCancelHits()
	{
		return true;
	}
}
