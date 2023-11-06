package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.components.level.IUltraLevelComponent;
import absolutelyaya.ultracraft.components.level.UltraLevelComponent;
import absolutelyaya.ultracraft.components.player.*;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;
import net.minecraft.util.Identifier;

public final class UltraComponents implements EntityComponentInitializer, LevelComponentInitializer
{
	public static final ComponentKey<IWingDataComponent> WING_DATA =
			ComponentRegistry.getOrCreate(new Identifier(Ultracraft.MOD_ID, "wing_data"), IWingDataComponent.class);
	public static final ComponentKey<IWingedPlayerComponent> WINGED_ENTITY =
			ComponentRegistry.getOrCreate(new Identifier(Ultracraft.MOD_ID, "winged"), IWingedPlayerComponent.class);
	public static final ComponentKey<IProgressionComponent> PROGRESSION =
			ComponentRegistry.getOrCreate(new Identifier(Ultracraft.MOD_ID, "progression"), IProgressionComponent.class);
	public static final ComponentKey<IEasterComponent> EASTER =
			ComponentRegistry.getOrCreate(new Identifier(Ultracraft.MOD_ID, "easter"), IEasterComponent.class);
	public static final ComponentKey<IArmComponent> ARMS =
			ComponentRegistry.getOrCreate(new Identifier(Ultracraft.MOD_ID, "arms"), IArmComponent.class);
	public static final ComponentKey<IUltraLevelComponent> GLOBAL =
			ComponentRegistry.getOrCreate(new Identifier(Ultracraft.MOD_ID, "global_data"), IUltraLevelComponent.class);
	
	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry)
	{
		registry.registerForPlayers(WING_DATA, WingDataComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
		registry.registerForPlayers(WINGED_ENTITY, WingedPlayerComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
		registry.registerForPlayers(PROGRESSION, ProgressionComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
		registry.registerForPlayers(EASTER, EasterComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
		registry.registerForPlayers(ARMS, ArmComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
	}
	
	@Override
	public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry)
	{
		registry.register(GLOBAL, UltraLevelComponent::new);
	}
}
