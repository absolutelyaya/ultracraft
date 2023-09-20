package absolutelyaya.ultracraft.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.util.Identifier;

import java.util.List;

public interface IProgressionComponent extends ComponentV3, AutoSyncedComponent
{
	void lock(Identifier id);
	
	void unlock(Identifier id);
	
	boolean isUnlocked(Identifier id);
	
	List<Identifier> getUnlockedList();
	
	void disown(Identifier id);
	
	void obtain(Identifier id);
	
	boolean isOwned(Identifier id);
	
	List<Identifier> getOwnedList();
	
	void reset();
	
	void sync();
}
