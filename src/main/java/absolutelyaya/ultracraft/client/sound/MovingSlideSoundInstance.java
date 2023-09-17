package absolutelyaya.ultracraft.client.sound;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class MovingSlideSoundInstance extends MovingPlayerSoundInstance
{
	public MovingSlideSoundInstance(PlayerEntity owner)
	{
		super(SoundEvents.ENTITY_MINECART_RIDING, owner);
		pitch = 2.1f;
	}
	
	@Override
	public void tick()
	{
		if(owner.isRemoved() || !UltracraftClient.getConfigHolder().get().movementSounds)
			setDone();
		x = owner.getX();
		y = owner.getY();
		z = owner.getZ();
		if(UltraComponents.WING_DATA.get(owner).isVisible() && owner.isSprinting() && owner.isOnGround())
		{
			float speed = (float)owner.getVelocity().length();
			pitch = speed * 4f;
			volume = MathHelper.clamp(speed / 2f, 0f, 0.75f);
		}
		else
			volume = 0;
	}
}
