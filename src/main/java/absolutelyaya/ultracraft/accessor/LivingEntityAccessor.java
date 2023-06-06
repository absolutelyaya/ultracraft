package absolutelyaya.ultracraft.accessor;

import java.util.function.Supplier;

public interface LivingEntityAccessor
{
	boolean punch();
	
	float GetPunchProgress(float tickDelta);
	
	boolean IsPunching();
	
	boolean IsCanBleed();
	
	void SetCanBleedSupplier(Supplier<Boolean> supplier);
	
	boolean takePunchKnockback();
	
	void SetTakePunchKnockbackSupplier(Supplier<Boolean> supplier);
	
	void addRecoil(float recoil);
	
	float getRecoil();
	
	int getGravityReduction();
}
