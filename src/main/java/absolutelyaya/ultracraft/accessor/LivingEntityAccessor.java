package absolutelyaya.ultracraft.accessor;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;

import java.util.function.Supplier;

public interface LivingEntityAccessor
{
	boolean punch();
	
	float getPunchProgress(float tickDelta);
	
	boolean IsPunching();
	
	void knuckleBlast();
	
	float getKnuckleBlastProgress(float tickDelta);
	
	void cancelPunch();
	
	boolean IsCanBleed();
	
	void setCanBleedSupplier(Supplier<Boolean> supplier);
	
	boolean takePunchKnockback();
	
	void setTakePunchKnockbackSupplier(Supplier<Boolean> supplier);
	
	void addRecoil(float recoil);
	
	float getRecoil();
	
	int getGravityReduction();
	
	boolean isRicochetHittable();
	
	void bleed(Vec3d pos, float halfheight, DamageSource source, float amount);
}
