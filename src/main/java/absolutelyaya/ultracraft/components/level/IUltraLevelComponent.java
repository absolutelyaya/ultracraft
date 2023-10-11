package absolutelyaya.ultracraft.components.level;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;
import java.util.UUID;

public interface IUltraLevelComponent extends ComponentV3
{
	boolean isHivelWhitelistActive();
	
	void setHivelWhitelistActive(boolean v);
	
	Map<UUID, String> getHivelWhitelist();
	
	boolean isPlayerAllowedToHivel(PlayerEntity player);
	
	boolean isGraffitiWhitelistActive();
	
	void setGraffitiWhitelistActive(boolean v);
	
	Map<UUID, String> getGraffitiWhitelist();
	
	boolean isPlayerAllowedToGraffiti(PlayerEntity player);
}
