package absolutelyaya.ultracraft.client.sound;

import absolutelyaya.ultracraft.entity.projectile.ThrownMachineSwordEntity;
import absolutelyaya.ultracraft.registry.SoundRegistry;

public class MovingMachineSwordSoundInstance extends MovingPlayerSoundInstance
{
	public MovingMachineSwordSoundInstance(ThrownMachineSwordEntity owner)
	{
		super(SoundRegistry.MACHINESWORD_LOOP.value(), owner);
		pitch = 1.5f;
		volume = 1f;
	}
	
	@Override
	public void tick()
	{
		if(owner.isRemoved())
			setDone();
		x = owner.getX();
		y = owner.getY();
		z = owner.getZ();
	}
	
	@Override
	public boolean shouldAlwaysPlay()
	{
		return false;
	}
}
