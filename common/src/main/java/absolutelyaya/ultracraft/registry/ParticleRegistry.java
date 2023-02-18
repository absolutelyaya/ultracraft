package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.registry.Registry;

public class ParticleRegistry
{
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Ultracraft.MOD_ID, Registry.PARTICLE_TYPE_KEY);
	public static final RegistrySupplier<DefaultParticleType> MALICIOUS_CHARGE = PARTICLE_TYPES.register("malicious_charge",
			() -> new DefaultParticleType(false));
	
	public static void init()
	{
		PARTICLE_TYPES.register();
	}
}
