package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.components.*;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public final class UltraComponents implements EntityComponentInitializer
{
	public static final ComponentKey<IWingDataComponent> WING_DATA =
			ComponentRegistry.getOrCreate(new Identifier(Ultracraft.MOD_ID, "wing_data"), IWingDataComponent.class);
	public static final ComponentKey<IWingedPlayerComponent> WINGED_ENTITY =
			ComponentRegistry.getOrCreate(new Identifier(Ultracraft.MOD_ID, "winged"), IWingedPlayerComponent.class);
	public static final ComponentKey<IProgressionComponent> PROGRESSION =
			ComponentRegistry.getOrCreate(new Identifier(Ultracraft.MOD_ID, "progression"), IProgressionComponent.class);
	
	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry)
	{
		registry.registerForPlayers(WING_DATA, WingDataComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
		registry.registerForPlayers(WINGED_ENTITY, WingedPlayerComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
		registry.registerForPlayers(PROGRESSION, ProgressionComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
	}
}
