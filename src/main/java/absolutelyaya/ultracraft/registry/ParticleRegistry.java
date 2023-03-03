package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ParticleRegistry
{
	public static final DefaultParticleType MALICIOUS_CHARGE = Registry.register(Registries.PARTICLE_TYPE,
			new Identifier(Ultracraft.MOD_ID, "malicious_charge"), FabricParticleTypes.simple());
	public static final DefaultParticleType DASH = Registry.register(Registries.PARTICLE_TYPE,
			new Identifier(Ultracraft.MOD_ID, "dash"), FabricParticleTypes.simple());
	public static final DefaultParticleType SLIDE = Registry.register(Registries.PARTICLE_TYPE,
			new Identifier(Ultracraft.MOD_ID, "slide"), FabricParticleTypes.simple());
	
	public static void init()
	{
	
	}
}