package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.fluid.BloodFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class FluidRegistry
{
	public static final FlowableFluid STILL_BLOOD = Registry.register(Registries.FLUID, new Identifier(Ultracraft.MOD_ID, "blood"), new BloodFluid.Still());
	public static final FlowableFluid Flowing_BLOOD = Registry.register(Registries.FLUID, new Identifier(Ultracraft.MOD_ID, "blood_flowing"), new BloodFluid.Flowing());
	
	public static void register()
	{
	
	}
}
