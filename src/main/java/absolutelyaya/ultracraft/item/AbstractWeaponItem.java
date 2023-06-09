package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2i;

public abstract class AbstractWeaponItem extends Item
{
	protected final float recoil;
	
	public AbstractWeaponItem(Settings settings, float recoil)
	{
		super(settings);
		this.recoil = recoil;
	}
	
	public boolean onPrimaryFire(World world, PlayerEntity user, Vec3d userVelocity)
	{
		((LivingEntityAccessor)user).addRecoil(recoil);
		return true;
	}
	
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
	
	@Override
	public boolean isItemBarVisible(ItemStack stack)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity)MinecraftClient.getInstance().player).getGunCooldownManager();
		return cdm.getCooldown(stack.getItem(), GunCooldownManager.PRIMARY) > 0;
	}
	
	@Override
	public int getItemBarStep(ItemStack stack)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity)MinecraftClient.getInstance().player).getGunCooldownManager();
		return (int)(cdm.getCooldownPercent(stack.getItem(), GunCooldownManager.PRIMARY) * 14);
	}
	
	@Override
	public int getItemBarColor(ItemStack stack)
	{
		return 0x28ccdf;
	}
}
