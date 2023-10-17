package absolutelyaya.ultracraft.components.player;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;

public interface IArmComponent extends ComponentV3, AutoSyncedComponent
{
	byte getActiveArm();
	
	void setActiveArm(byte i);
	
	void cycleArms();
	
	void sync();
	
	byte getUnlockedArmCount();
	
	boolean isFeedbacker();
	
	boolean isKnuckleblaster();
	
	void setArmVisible(boolean v);
	
	boolean isVisible();
	
	void setPunchPressed(boolean v);
	
	boolean isPunchPressed();
}
