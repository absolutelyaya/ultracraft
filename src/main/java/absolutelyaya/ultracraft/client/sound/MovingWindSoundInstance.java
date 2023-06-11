package absolutelyaya.ultracraft.client.sound;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class MovingWindSoundInstance extends MovingPlayerSoundInstance
{
	float desiredVolume, desiredPitch;
	
	public MovingWindSoundInstance(PlayerEntity owner)
	{
		super(SoundRegistry.WIND_LOOP.value(), owner);
	}
	
	@Override
	public void tick()
	{
		if(owner == null || owner.isRemoved() || !UltracraftClient.getConfigHolder().get().movementSounds)
			setDone();
		WingedPlayerEntity winged = (WingedPlayerEntity)owner;
		if(!winged.isWingsActive())
		{
			desiredVolume = volume = 0;
			return;
		}
		x = owner.getX();
		y = owner.getY();
		z = owner.getZ();
		float speed = (float)owner.getVelocity().length();
		if(winged.isGroundPounding())
		{
			pitch = 1f;
			desiredVolume = 0.75f;
		}
		else if (speed > 0.5f)
		{
			desiredPitch = MathHelper.clamp(speed / 6f, 1f, 1.25f);
			desiredVolume = MathHelper.clamp(speed / 6f, 0f, 0.75f);
		}
		else
			desiredVolume = 0;
		
		volume = MathHelper.lerp(0.3f, volume, desiredVolume);
		pitch = MathHelper.lerp(0.3f, pitch, desiredPitch);
	}
}
