package absolutelyaya.ultracraft.client.sound;

import absolutelyaya.ultracraft.entity.machine.SwordsmachineEntity;
import absolutelyaya.ultracraft.registry.SoundRegistry;

public class MovingSwordsmachineSoundInstance extends MovingPlayerSoundInstance
{
	public MovingSwordsmachineSoundInstance(SwordsmachineEntity owner)
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
		volume = ((SwordsmachineEntity)owner).getMachineSwordVolume();
		pitch = ((SwordsmachineEntity)owner).getMachineSwordPitch();
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
