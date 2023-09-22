package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.RawAnimation;

public abstract class AbstractRevolverItem extends AbstractWeaponItem implements GeoItem
{
	boolean b; //toggled on every shot; decides purely which shot animation should be used to allow for rapid firing
	final RawAnimation AnimationStop = RawAnimation.begin().then("nothing", Animation.LoopType.LOOP);
	final RawAnimation AnimationCharge = RawAnimation.begin().thenPlay("charging").thenLoop("charged");
	final RawAnimation AnimationSpin = RawAnimation.begin().thenPlay("spinup").thenLoop("spinning");
	final RawAnimation AnimationDischarge = RawAnimation.begin().thenPlay("discharge");
	final RawAnimation AnimationShot = RawAnimation.begin().thenPlay("shot");
	final RawAnimation AnimationShot2 = RawAnimation.begin().thenPlay("shot2");
	
	public AbstractRevolverItem(Settings settings, float recoil, float altRecoil)
	{
		super(settings, recoil, altRecoil);
	}
	
	@Override
	public boolean onPrimaryFire(World world, PlayerEntity user, Vec3d userVelocity)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(user).getGunCooldownManager();
		if(isCanFirePrimary(user))
		{
			if(world.isClient)
			{
				super.onPrimaryFire(world, user, userVelocity);
				return true;
			}
			world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 0.75f,
					0.9f + (user.getRandom().nextFloat() - 0.5f) * 0.2f);
			triggerAnim(user, GeoItem.getOrAssignId(user.getMainHandStack(), (ServerWorld)world), getControllerName(), b ? "shot" : "shot2");
			ServerHitscanHandler.performHitscan(user, ServerHitscanHandler.NORMAL, 1f);
			cdm.setCooldown(this, 6, GunCooldownManager.PRIMARY);
			b = !b;
			return true;
		}
		else
			return false;
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		if(!(entity instanceof PlayerEntity player))
			return;
		super.inventoryTick(stack, world, entity, slot, selected);
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(player).getGunCooldownManager();
		//Marksman Coin Tick
		int coins = getCoins(stack);
		if(coins < 4 && cdm.isUsable(this, GunCooldownManager.SECONDARY))
		{
			setCoins(stack, coins + 1);
			if(coins + 1 < 4)
				cdm.setCooldown(this, 200, GunCooldownManager.SECONDARY);
			player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.1f, 1.75f);
		}
		//Sharpshooter Charges Tick
		int charges = getCharges(stack);
		if(charges < 3 && cdm.isUsable(this, GunCooldownManager.TRITARY))
		{
			setCharges(stack, charges + 1);
			cdm.setCooldown(this, 200, GunCooldownManager.TRITARY);
			player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.1f, 1.5f);
		}
	}
	
	@Override
	Item[] getVariants()
	{
		return new Item[]{ItemRegistry.PIERCE_REVOLVER, ItemRegistry.MARKSMAN_REVOLVER, ItemRegistry.SHARPSHOOTER_REVOLVER};
	}
	
	@Override
	int getSwitchCooldown()
	{
		return 4;
	}
	
	public int getCoins(ItemStack stack)
	{
		if(!stack.hasNbt() || !stack.getNbt().contains("coins", NbtElement.INT_TYPE))
			stack.getOrCreateNbt().putInt("coins", 4);
		return stack.getNbt().getInt("coins");
	}
	
	public void setCoins(ItemStack stack, int i)
	{
		stack.getOrCreateNbt().putInt("coins", i);
	}
	
	public int getCharges(ItemStack stack)
	{
		if(!stack.hasNbt() || !stack.getNbt().contains("charges", NbtElement.INT_TYPE))
			stack.getOrCreateNbt().putInt("charges", 3);
		return stack.getNbt().getInt("charges");
	}
	
	public void setCharges(ItemStack stack, int i)
	{
		stack.getOrCreateNbt().putInt("charges", i);
	}
	
	@Override
	public Class<? extends AbstractWeaponItem> getCooldownClass()
	{
		return AbstractRevolverItem.class;
	}
}
