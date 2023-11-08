package absolutelyaya.ultracraft.components.player;

import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public interface IWingedPlayerComponent extends ComponentV3, CommonTickingComponent
{
	void setWingState(byte state);
	
	void updateWingState();
	
	byte getWingState();
	
	void onDash();
	
	void cancelDash();
	
	void onDashJump();
	
	boolean isDashing();
	
	boolean wasDashing();
	
	boolean wasDashing(int i);
	
	int getDashingTicks();
	
	float getStamina();
	
	boolean consumeStamina();
	
	void replenishStamina(int i);
	
	void setSlamming(boolean b);
	
	boolean isSlamming();
	
	boolean shouldIgnoreSlowdown();
	
	void setIgnoreSlowdown(boolean b);
	
	void setSlideDir(Vec3d dir);
	
	Vec3d getSlideDir();
	
	void bloodHeal(float val);
	
	void setBloodHealCooldown(int ticks);
	
	void setAirControlIncreased(boolean b);
	
	boolean isAirControlIncreased();
	
	void setSharpshooterCooldown(int val);
	
	int getSharpshooterCooldown();
	
	void setPrimaryFiring(boolean firing);
	
	boolean isPrimaryFiring();
	
	AbstractWeaponItem getLastPrimaryWeapon();
	
	@NotNull
	GunCooldownManager getGunCooldownManager();
	
	float getSlamDamageCooldown();
	
	void setSlamDamageCooldown(int i);
	
	int getMagnets();
	
	void setMagnets(int i);
}
