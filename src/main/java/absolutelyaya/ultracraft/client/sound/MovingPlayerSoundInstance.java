package absolutelyaya.ultracraft.client.sound;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public abstract class MovingPlayerSoundInstance extends MovingSoundInstance
{
	protected Entity owner;
	
	public MovingPlayerSoundInstance(SoundEvent event, Entity owner)
	{
		super(event, SoundCategory.PLAYERS, SoundInstance.createRandom());
		this.owner = owner;
		repeat = true;
		repeatDelay = 0;
		volume = 0;
		pitch = 1f;
		x = owner.getX();
		y = owner.getY();
		z = owner.getZ();
	}
	
	@Override
	public boolean shouldAlwaysPlay()
	{
		return true;
	}
}
