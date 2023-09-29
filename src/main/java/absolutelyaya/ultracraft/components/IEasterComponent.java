package absolutelyaya.ultracraft.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;

//This Component contains data that's only used for Jokes and Secrets. I didn't want to mix that data with actually important stuff yk
public interface IEasterComponent extends ComponentV3, AutoSyncedComponent
{
	int getFishes();
	
	void addFish();
	
	int getPlushies();
	
	void addPlushie();
	
	void removePlushie();
	
	void sync();
}
