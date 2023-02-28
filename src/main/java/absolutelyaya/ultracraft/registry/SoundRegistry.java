package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class SoundRegistry
{
	public static final SoundEvent THE_FIRE_IS_GONE = Registry.register(Registries.SOUND_EVENT,
			new Identifier(Ultracraft.MOD_ID, "music.the_fire_is_gone"),
			SoundEvent.of(new Identifier(Ultracraft.MOD_ID, "music.the_fire_is_gone")));
	
	public static void register()
	{
	
	}
}
