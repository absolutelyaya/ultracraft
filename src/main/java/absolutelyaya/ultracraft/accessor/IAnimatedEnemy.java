package absolutelyaya.ultracraft.accessor;

public interface IAnimatedEnemy
{
	void setAnimation(byte id);
	
	byte getAnimation();
	
	float getAnimSpeedMult();
	
	void setCooldown(int cooldown);
	
	int getCooldown();
	
	boolean isHeadFixed();
}
