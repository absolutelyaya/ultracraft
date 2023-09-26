package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.components.IProgressionComponent;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
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
	
	protected boolean isCanFirePrimary(PlayerEntity user)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(user).getGunCooldownManager();
		return /*user.getAttackCooldownProgress(0.3f) >= 1f &&*/
					   (user instanceof WingedPlayerEntity && cdm.isUsable(this, GunCooldownManager.PRIMARY));
	}
	
	public boolean onPrimaryFire(World world, PlayerEntity user, Vec3d userVelocity)
	{
		((LivingEntityAccessor)user).addRecoil(recoil);
		return true;
	}
	
	public void onPrimaryFireStop(World world, PlayerEntity user)
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
		if(world.isClient && UltraComponents.WINGED_ENTITY.get(entity).isPrimaryFiring() && selected &&
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
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(MinecraftClient.getInstance().player).getGunCooldownManager();
		return !cdm.isUsable(getCooldownClass(stack), GunCooldownManager.PRIMARY);
	}
	
	@Override
	public int getItemBarStep(ItemStack stack)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(MinecraftClient.getInstance().player).getGunCooldownManager();
		return (int)(cdm.getCooldownPercent(getCooldownClass(stack), GunCooldownManager.PRIMARY) * 14);
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
	
	public boolean hasVariantBG()
	{
		return true;
	}
	
	abstract Item[] getVariants();
	
	abstract int getSwitchCooldown();
	
	static Item getNextVariant(ItemStack stack, IProgressionComponent progression)
	{
		if(!(stack.getItem() instanceof AbstractWeaponItem weapon))
			return null;
		Item[] variants = weapon.getVariants();
		int start = -1;
		for (int i = 0; i < variants.length; i++)
		{
			if(!stack.getItem().equals(variants[i]))
				continue;
			start = i;
		}
		if(start == -1)
			return null;
		for (int i = 1; i < variants.length; i++)
		{
			System.out.println((start + i) % (variants.length));
			Item item = variants[(start + i) % (variants.length)];
			if(!item.equals(stack.getItem()) && progression.isOwned(Registries.ITEM.getId(item)))
				return item;
		}
		return null;
	}
	
	public static void cycleVariant(PlayerEntity player)
	{
		if(player.getWorld().isClient)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			ClientPlayNetworking.send(PacketRegistry.CYCLE_WEAPON_VARIANT_PACKET_ID, buf);
			return;
		}
		ItemStack stack = player.getMainHandStack();
		if(!(stack.getItem() instanceof AbstractWeaponItem))
			return;
		IProgressionComponent progression = UltraComponents.PROGRESSION.get(player);
		Item nextItem = getNextVariant(stack, progression);
		if(nextItem == null)
			return;
		NbtCompound nbt = stack.getOrCreateNbt();
		if(nbt.contains("charging"))
			nbt.remove("charging"); //TODO: doesn't work
		ItemStack nextStack = new ItemStack(nextItem);
		nextStack.setNbt(nbt);
		player.getInventory().main.set(player.getInventory().selectedSlot, nextStack);
		if(nextItem instanceof AbstractWeaponItem weapon)
		{
			GunCooldownManager gcdm = UltraComponents.WINGED_ENTITY.get(player).getGunCooldownManager();
			int cd = weapon.getSwitchCooldown();
			if(gcdm.getCooldown(weapon, GunCooldownManager.PRIMARY) < cd)
				UltraComponents.WINGED_ENTITY.get(player).getGunCooldownManager().setCooldown(weapon, cd, GunCooldownManager.PRIMARY);
		}
	}
	
	public Class<? extends AbstractWeaponItem> getCooldownClass()
	{
		return getClass();
	}
	
	public Class<? extends AbstractWeaponItem> getCooldownClass(ItemStack stack)
	{
		if(stack.getItem() instanceof AbstractWeaponItem weapon)
			return weapon.getCooldownClass();
		return getClass();
	}
	
	public int getNbt(ItemStack stack, String nbt)
	{
		if(!stack.hasNbt() || !stack.getNbt().contains(nbt, NbtElement.INT_TYPE))
			stack.getOrCreateNbt().putInt(nbt, getNbtDefault(nbt));
		return stack.getNbt().getInt(nbt);
	}
	
	public void setNbt(ItemStack stack, String nbt, int i)
	{
		stack.getOrCreateNbt().putInt(nbt, i);
	}
	
	protected int getNbtDefault(String nbt)
	{
		return 0;
	}
}
