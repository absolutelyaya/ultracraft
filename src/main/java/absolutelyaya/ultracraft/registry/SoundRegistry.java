package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundRegistry
{
	public static final RegistryEntry.Reference<SoundEvent> THE_FIRE_IS_GONE = registerReference(new Identifier(Ultracraft.MOD_ID, "music.the_fire_is_gone"));
	public static final RegistryEntry.Reference<SoundEvent> ELEVATOR_FALL = registerReference(new Identifier(Ultracraft.MOD_ID, "misc.elevator_fall"));
	
	public static void register()
	{
	
	}
	
	private static RegistryEntry.Reference<SoundEvent> registerReference(Identifier id)
	{
		return Registry.registerReference(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}
}
