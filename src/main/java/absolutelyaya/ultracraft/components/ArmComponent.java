package absolutelyaya.ultracraft.components;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class ArmComponent implements IArmComponent
{
	static final Identifier[] armIDs = new Identifier[]
		{
			new Identifier(Ultracraft.MOD_ID, "feedbacker"),
			new Identifier(Ultracraft.MOD_ID, "knuckleblaster")
		};
	
	final PlayerEntity provider;
	byte activeArm = 0;
	
	public ArmComponent(PlayerEntity provider)
	{
		this.provider = provider;
	}
	
	@Override
	public byte getActiveArm()
	{
		return activeArm;
	}
	
	@Override
	public void setActiveArm(byte i)
	{
		if(armIDs.length <= i)
		{
			Ultracraft.LOGGER.warn("Tried equipping invalid Arm (index " + i + " out of bounds)");
			return;
		}
		IProgressionComponent progression = UltraComponents.PROGRESSION.get(provider);
		if(progression.isOwned(armIDs[i]))
			activeArm = i;
		else
			activeArm = -1;
		sync();
	}
	
	@Override
	public void cycleArms()
	{
		IProgressionComponent progression = UltraComponents.PROGRESSION.get(provider);
		byte start = activeArm;
		for (int i = 1; i < armIDs.length; i++)
		{
			int idx = (start + i) % (armIDs.length);
			Identifier id = armIDs[idx];
			if(activeArm != idx && progression.isUnlocked(id))
			{
				activeArm = (byte)idx;
				sync();
				return;
			}
		}
	}
	
	@Override
	public void sync()
	{
		UltraComponents.ARMS.sync(provider);
	}
	
	@Override
	public void readFromNbt(NbtCompound tag)
	{
		if(tag.contains("active", NbtElement.BYTE_TYPE))
			activeArm = tag.getByte("active");
	}
	
	@Override
	public void writeToNbt(NbtCompound tag)
	{
		tag.putByte("active", activeArm);
	}
}
