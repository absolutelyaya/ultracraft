package absolutelyaya.ultracraft.entity.goal;

import absolutelyaya.ultracraft.accessor.IAnimatedEnemy;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;

public abstract class TimedAttackGoal<T extends HostileEntity & IAnimatedEnemy> extends Goal
{
	protected final T mob;
	protected final int randomCooldownRange = 40;
	protected LivingEntity target;
	protected int timer, baseCooldown = 40;
	final protected byte animIdle, animAttack;
	final protected int attackLength;
	
	public TimedAttackGoal(T mob, byte animIdle, byte animAttack, int attackLength)
	{
		this.mob = mob;
		this.animIdle = animIdle;
		this.animAttack = animAttack;
		this.attackLength = attackLength;
	}
	
	@Override
	public boolean canStart()
	{
		if(mob.getCooldown() > 0 || mob.getAnimation() != animIdle)
			return false;
		return mob.getTarget() != null && mob.canSee(mob.getTarget());
	}
	
	@Override
	public void start()
	{
		timer = 0;
		target = mob.getTarget();
		mob.setAnimation(animAttack);
	}
	
	@Override
	public void tick()
	{
		for (int i = 0; i < mob.getAnimSpeedMult(); i++)
			process();
	}
	
	protected void process()
	{
		timer++;
		mob.bodyYaw = mob.headYaw;
	}
	
	@Override
	public boolean shouldContinue()
	{
		return target != null && target.isAlive() && timer < attackLength;
	}
	
	@Override
	public boolean canStop()
	{
		return !shouldContinue();
	}
	
	@Override
	public boolean shouldRunEveryTick()
	{
		return true;
	}
	
	@Override
	public void stop()
	{
		if(mob.getAnimation() == animAttack || mob.getAnimation() != animIdle)
			mob.setAnimation(animIdle);
		mob.setCooldown((int)((baseCooldown + mob.getRandom().nextFloat() * randomCooldownRange) / mob.getAnimSpeedMult()));
	}
}
