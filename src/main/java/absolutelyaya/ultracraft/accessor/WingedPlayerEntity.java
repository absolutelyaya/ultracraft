package absolutelyaya.ultracraft.accessor;

import absolutelyaya.ultracraft.client.GunCooldownManager;
import org.jetbrains.annotations.NotNull;

public interface WingedPlayerEntity
{
	void setWingState(byte state);
	
	byte getWingState();
	
	byte getLastState();
	
	float getWingAnimTime();
	
	void setWingAnimTime(float f);
	
	void setWingsVisible(boolean b);
	
	boolean isWingsVisible();
	
	void onDash();
	
	void onDashJump();
	
	boolean isDashing();
	
	boolean wasDashing();
	
	int getStamina();
	
	boolean consumeStamina();
	
	int getWingHintDisplayTicks();
	
	@NotNull
	GunCooldownManager getGunCooldownManager();
}
