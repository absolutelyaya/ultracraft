package absolutelyaya.ultracraft.accessor;

public interface ClientPlayerAccessor
{
	void Punch();
	
	float GetPunchProgress(float tickDelta);
	
	boolean IsPunching();
}
