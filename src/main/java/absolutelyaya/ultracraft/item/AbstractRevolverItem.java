package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.ServerHitscanHandler;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;

public abstract class AbstractRevolverItem extends AbstractWeaponItem implements GeoItem
{
	boolean b; //toggled on every shot; decides purely which shot animation should be used to allow for rapid firing
	
	public AbstractRevolverItem(Settings settings, float recoil, float altRecoil)
	{
		super(settings, recoil, altRecoil);
	}
	
	@Override
	public boolean onPrimaryFire(World world, PlayerEntity user)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity)user).getGunCooldownManager();
		if(cdm.isUsable(this, 0))
		{
			if(world.isClient)
			{
				super.onPrimaryFire(world, user);
				return true;
			}
			world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 0.75f,
					0.9f + (user.getRandom().nextFloat() - 0.5f) * 0.2f);
			triggerAnim(user, GeoItem.getOrAssignId(user.getMainHandStack(), (ServerWorld)world), getControllerName(), b ? "shot" : "shot2");
			ServerHitscanHandler.performHitscan(user, (byte)0, 1f);
			cdm.setCooldown(this, 6, GunCooldownManager.PRIMARY);
			b = !b;
			return true;
		}
		else
			return false;
	}
}
