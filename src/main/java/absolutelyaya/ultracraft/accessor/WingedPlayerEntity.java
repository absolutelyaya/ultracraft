package absolutelyaya.ultracraft.accessor;

import absolutelyaya.ultracraft.client.GunCooldownManager;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public interface WingedPlayerEntity
{
	void setWingState(byte state);
	
	byte getWingState();
	
	float getWingAnimTime();
	
	void setWingAnimTime(float f);
	
	Vec3d[] getWingPose();
	
	void setWingPose(Vec3d[] pose);
	
	void setWingsVisible(boolean b);
	
	boolean isWingsActive();
	
	void onDash();
	
	void cancelDash();
	
	void onDashJump();
	
	boolean isDashing();
	
	boolean wasDashing();
	
	boolean wasDashing(int i);
	
	int getDashingTicks();
	
	int getStamina();
	
	boolean consumeStamina();
	
	int getWingHintDisplayTicks();
	
	@NotNull
	GunCooldownManager getGunCooldownManager();
	
	void startSlam();
	
	void endSlam(boolean strong);
	
	boolean isGroundPounding();
	
	boolean shouldIgnoreSlowdown();
	
	void setIgnoreSlowdown(boolean b);
	
	Vec3d getSlideDir();
	
	void updateSpeedGamerule();
}
