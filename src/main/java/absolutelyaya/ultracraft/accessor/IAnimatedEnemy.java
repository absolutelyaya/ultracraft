package absolutelyaya.ultracraft.accessor;

public interface IAnimatedEnemy
{
	void setAnimation(byte id);
	
	byte getAnimation();
	
	int getAnimSpeedMult();
	
	void setCooldown(int cooldown);
	
	int getCooldown();
	
	boolean isHeadFixed();
}
