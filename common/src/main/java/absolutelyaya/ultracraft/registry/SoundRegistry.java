package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public class SoundRegistry
{
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Ultracraft.MOD_ID, Registry.SOUND_EVENT_KEY);
	public static final Supplier<SoundEvent> THE_FIRE_IS_GONE = SOUND_EVENTS.register("the_fire_is_gone",
			() -> new SoundEvent(new Identifier(Ultracraft.MOD_ID, "music/the_fire_is_gone")));
	
	public static void register()
	{
		SOUND_EVENTS.register();
	}
}
