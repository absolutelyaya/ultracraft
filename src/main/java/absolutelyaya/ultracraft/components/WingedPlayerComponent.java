package absolutelyaya.ultracraft.components;

import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.registry.StatusEffectRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class WingedPlayerComponent implements IWingedPlayerComponent, AutoSyncedComponent
{
	PlayerEntity provider;
	GunCooldownManager gunCDM;
	boolean slamming, ignoreSlowdown, primaryFiring, airControlIncreased;
	byte wingState, lastState;
	int dashingTicks = -2, slamDamageCooldown, stamina, bloodHealCooldown, sharpshooterCooldown, magnets;
	AbstractWeaponItem lastPrimaryWeapon;
	
	public WingedPlayerComponent(PlayerEntity provider)
	{
		this.provider = provider;
		gunCDM = new GunCooldownManager(provider);
	}
	
	@Override
	public void setWingState(byte state)
	{
		if(wingState != state)
		{
			lastState = wingState;
			wingState = state;
		}
	}
	
	@Override
	public void updateWingState()
	{
		if(isDashing())
			setWingState((byte)0);
		else if (provider.isSprinting())
			setWingState((byte)2);
		else if ((wingState == 0 && provider.isOnGround()) || (wingState == 2 && !provider.isSprinting()))
			setWingState((byte)1);
	}
	
	@Override
	public byte getWingState()
	{
		return wingState;
	}
	
	@Override
	public void onDash()
	{
		dashingTicks = 3;
		provider.getWorld().playSound(null, provider.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.75f, 1.6f);
	}
	
	@Override
	public void cancelDash()
	{
		dashingTicks = -2;
	}
	
	@Override
	public void onDashJump()
	{
		dashingTicks = -2;
		provider.getWorld().playSound(null, provider.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.75f, 1.6f);
	}
	
	@Override
	public boolean isDashing()
	{
		return dashingTicks > 0;
	}
	
	@Override
	public boolean wasDashing()
	{
		return dashingTicks + 1 >= 0;
	}
	
	@Override
	public boolean wasDashing(int i)
	{
		return dashingTicks + i >= 0;
	}
	
	@Override
	public int getDashingTicks()
	{
		return dashingTicks;
	}
	
	@Override
	public int getStamina()
	{
		return stamina;
	}
	
	@Override
	public boolean consumeStamina()
	{
		if(provider.isCreative())
			return true;
		if(stamina >= 30)
		{
			stamina = Math.max(stamina - 30, 0);
			return true;
		}
		else
			provider.playSound(SoundEvents.BLOCK_ANVIL_LAND, 0.5f, 1.8f);
		return false;
	}
	
	@Override
	public void replenishStamina(int i)
	{
		stamina = Math.min(stamina + 30 * i, 90);
	}
	
	@Override
	public void setSlamming(boolean b)
	{
		slamming = b;
	}
	
	@Override
	public boolean isSlamming()
	{
		return slamming;
	}
	
	@Override
	public boolean shouldIgnoreSlowdown()
	{
		return ignoreSlowdown;
	}
	
	@Override
	public void setIgnoreSlowdown(boolean b)
	{
		ignoreSlowdown = b;
	}
	
	@Override
	public void setSlideDir(Vec3d dir)
	{
	
	}
	
	@Override
	public Vec3d getSlideDir()
	{
		return null;
	}
	
	@Override
	public void bloodHeal(float val)
	{
		if(bloodHealCooldown == 0)
			provider.heal(val);
	}
	
	@Override
	public void setBloodHealCooldown(int ticks)
	{
		bloodHealCooldown = ticks;
	}
	
	@Override
	public void setAirControlIncreased(boolean b)
	{
		airControlIncreased = b;
	}
	
	@Override
	public boolean isAirControlIncreased()
	{
		return airControlIncreased;
	}
	
	@Override
	public void setSharpshooterCooldown(int val)
	{
		sharpshooterCooldown = val;
	}
	
	@Override
	public int getSharpshooterCooldown()
	{
		return sharpshooterCooldown;
	}
	
	@Override
	public void setPrimaryFiring(boolean firing)
	{
		boolean last = primaryFiring;
		primaryFiring = firing;
		if(firing)
		{
			if(provider.getInventory().getMainHandStack().getItem() instanceof AbstractWeaponItem w)
			{
				lastPrimaryWeapon = w;
				if(!last)
					w.onPrimaryFireStart(provider.getWorld(), provider);
			}
		}
		else
		{
			if(lastPrimaryWeapon != null)
				lastPrimaryWeapon.onPrimaryFireStop(provider.getWorld(), provider);
			lastPrimaryWeapon = null;
		}
	}
	
	@Override
	public boolean isPrimaryFiring()
	{
		return primaryFiring;
	}
	
	@Override
	public AbstractWeaponItem getLastPrimaryWeapon()
	{
		return lastPrimaryWeapon;
	}
	
	@Override
	public @NotNull GunCooldownManager getGunCooldownManager()
	{
		return gunCDM;
	}
	
	@Override
	public float getSlamDamageCooldown()
	{
		return slamDamageCooldown;
	}
	
	@Override
	public void setSlamDamageCooldown(int i)
	{
		slamDamageCooldown = i;
	}
	
	public void setMagnets(int i)
	{
		magnets = i;
	}
	
	public int getMagnets()
	{
		return magnets;
	}
	
	@Override
	public void readFromNbt(NbtCompound tag)
	{
	
	}
	
	@Override
	public void writeToNbt(NbtCompound tag)
	{
	
	}
	
	@Override
	public void tick()
	{
		gunCDM.tickCooldowns();
		if(dashingTicks > -60)
			dashingTicks--;
		if(slamDamageCooldown > 0)
			slamDamageCooldown--;
		if(bloodHealCooldown > 0)
			bloodHealCooldown--;
		if(sharpshooterCooldown > 0)
			sharpshooterCooldown--;
		updateWingState();
		StatusEffectInstance chilled = provider.getStatusEffect(StatusEffectRegistry.CHILLED);
		if(stamina < 90 && !provider.isSprinting() && !(chilled != null && provider.age % (chilled.getAmplifier() + 1) != 0))
		{
			stamina++;
			if(stamina % 30 == 0)
				provider.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 1f + stamina / 30f * 0.1f);
		}
	}
}
