package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.particle.ParryIndicatorParticleEffect;
import absolutelyaya.ultracraft.particle.TeleportParticleEffect;
import absolutelyaya.ultracraft.particle.goop.GoopDropParticleEffect;
import absolutelyaya.ultracraft.particle.goop.GoopParticleEffect;
import absolutelyaya.ultracraft.particle.goop.GoopStringParticleEffect;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ParticleRegistry
{
	//simple
	public static final DefaultParticleType MALICIOUS_CHARGE = Registry.register(Registries.PARTICLE_TYPE,
			new Identifier(Ultracraft.MOD_ID, "malicious_charge"), FabricParticleTypes.simple());
	public static final DefaultParticleType DASH = Registry.register(Registries.PARTICLE_TYPE,
			new Identifier(Ultracraft.MOD_ID, "dash"), FabricParticleTypes.simple());
	public static final DefaultParticleType SLIDE = Registry.register(Registries.PARTICLE_TYPE,
			new Identifier(Ultracraft.MOD_ID, "slide"), FabricParticleTypes.simple());
	public static final DefaultParticleType GROUND_POUND = Registry.register(Registries.PARTICLE_TYPE,
			new Identifier(Ultracraft.MOD_ID, "groundpound"), FabricParticleTypes.simple());
	public static final DefaultParticleType EJECTED_CORE_FLASH = Registry.register(Registries.PARTICLE_TYPE,
			new Identifier(Ultracraft.MOD_ID, "ejected_core"), FabricParticleTypes.simple());
	public static final DefaultParticleType BLOOD_SPLASH = Registry.register(Registries.PARTICLE_TYPE,
			new Identifier(Ultracraft.MOD_ID, "blood_splash"), FabricParticleTypes.simple());
	public static final DefaultParticleType BLOOD_BUBBLE = Registry.register(Registries.PARTICLE_TYPE,
			new Identifier(Ultracraft.MOD_ID, "blood_bubble"), FabricParticleTypes.simple());
	public static final DefaultParticleType RIPPLE = Registry.register(Registries.PARTICLE_TYPE,
			new Identifier(Ultracraft.MOD_ID, "ripple"), FabricParticleTypes.simple());
	//complex
	public static final ParticleType<ParryIndicatorParticleEffect> PARRY_INDICATOR = Registry.register(Registries.PARTICLE_TYPE,
			new Identifier(Ultracraft.MOD_ID, "parry_indicator"), FabricParticleTypes.complex(new ParryIndicatorParticleEffect.Factory()));
	public static final ParticleType<TeleportParticleEffect> TELEPORT = Registry.register(Registries.PARTICLE_TYPE,
			new Identifier(Ultracraft.MOD_ID, "teleport"), FabricParticleTypes.complex(new TeleportParticleEffect.Factory()));
	//goop
	public static final ParticleType<GoopDropParticleEffect> GOOP_DROP =
			Registry.register(Registries.PARTICLE_TYPE, new Identifier(Ultracraft.MOD_ID, "goop_drop"),
					FabricParticleTypes.complex(new GoopDropParticleEffect.Factory()));
	public static final ParticleType<GoopParticleEffect> GOOP =
			Registry.register(Registries.PARTICLE_TYPE, new Identifier(Ultracraft.MOD_ID, "goop"),
					FabricParticleTypes.complex(new GoopParticleEffect.Factory()));
	public static final ParticleType<GoopStringParticleEffect> GOOP_STRING =
			Registry.register(Registries.PARTICLE_TYPE, new Identifier(Ultracraft.MOD_ID, "goop_string"),
					FabricParticleTypes.complex(new GoopStringParticleEffect.Factory()));
	
	public static void init()
	{
	
	}
}
