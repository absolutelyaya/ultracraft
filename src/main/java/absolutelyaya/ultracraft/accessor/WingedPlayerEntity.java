package absolutelyaya.ultracraft.accessor;

import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.entity.other.BackTank;
import net.minecraft.util.math.Vec3d;

public interface WingedPlayerEntity
{
	
	Vec3d[] getWingPose();
	
	void setWingPose(Vec3d[] pose);
	
	void startSlam();
	
	void endSlam(boolean strong);
	
	Vec3d getSlideDir();
	
	void updateSpeedGamerule();
	
	boolean hasJustJumped();
	
	void setFocusedTerminal(TerminalBlockEntity terminal);
	
	TerminalBlockEntity getFocusedTerminal();
	
	boolean isOpped();
	
	void setBackTank(BackTank tank);
	
	BackTank getBacktank();
}
