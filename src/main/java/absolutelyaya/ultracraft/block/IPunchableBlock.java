package absolutelyaya.ultracraft.block;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public interface IPunchableBlock
{
	boolean onPunch(PlayerEntity puncher, BlockPos pos, boolean mainHand);
}
