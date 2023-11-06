package absolutelyaya.ultracraft;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class EarlyRiser implements Runnable
{
	@Override
	public void run()
	{
		MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();
		
		String entityPose = remapper.mapClassName("intermediary", "net.minecraft.class_4050");
		ClassTinkerers.enumBuilder(entityPose).addEnum("DASH").addEnum("SLIDE").build();
		
		String gameRuleCategory = remapper.mapClassName("intermediary", "net.minecraft.class_1928$class_5198");
		ClassTinkerers.enumBuilder(gameRuleCategory, String.class)
				.addEnum("ULTRACRAFT", "gamerule.ultracraft.category.general")
				.addEnum("ULTRACRAFT_WORLD", "gamerule.ultracraft.category.world").build();
		
		String bossbarStyle = remapper.mapClassName("intermediary", "net.minecraft.class_1259$class_1261");
		ClassTinkerers.enumBuilder(bossbarStyle, String.class)
				.addEnum("ULTRA", "ultra")
				.addEnum("ULTRA-V2", "ultra-v2").build();
	}
}
