package absolutelyaya.ultracraft.accessor;

public interface ClientPlayerAccessor
{
	boolean Punch();
	
	float GetPunchProgress(float tickDelta);
	
	boolean IsPunching();
}
