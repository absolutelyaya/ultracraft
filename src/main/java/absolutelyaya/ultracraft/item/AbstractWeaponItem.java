package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2i;

public abstract class AbstractWeaponItem extends Item
{
	protected final float recoil, altRecoil;
	
	public AbstractWeaponItem(Settings settings, float recoil, float altRecoil)
	{
		super(settings);
		this.recoil = recoil;
		this.altRecoil = altRecoil;
	}
	
	public boolean onPrimaryFire(World world, PlayerEntity user, Vec3d userVelocity)
	{
		((LivingEntityAccessor)user).addRecoil(recoil);
		return true;
	}
	
	public void onPrimaryFireStop()
	{
	
	}
	
	public void onAltFire(World world, PlayerEntity user)
	{
		((LivingEntityAccessor)user).addRecoil(altRecoil);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if(world.isClient && entity instanceof WingedPlayerEntity winged && winged.isPrimaryFiring() && selected &&
				   onPrimaryFire(world, (PlayerEntity)entity, entity.getVelocity()))
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeByte(2);
			buf.writeVector3f(entity.getVelocity().toVector3f());
			ClientPlayNetworking.send(PacketRegistry.PRIMARY_SHOT_C2S_PACKET_ID, buf);
		}
	}
	
	public abstract Vector2i getHUDTexture();
	
	public boolean shouldAim()
	{
		return true;
	}
	
	public boolean shouldCancelPunching()
	{
		return true;
	}
	
	@Override
	public boolean isItemBarVisible(ItemStack stack)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity)MinecraftClient.getInstance().player).getGunCooldownManager();
		return !cdm.isUsable(stack.getItem(), GunCooldownManager.PRIMARY);
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
	
	abstract String getControllerName();
	
	public String getCountString(ItemStack stack)
	{
		return null;
	}
	
	@Override
	public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack)
	{
		return false;
	}
}
