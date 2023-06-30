package absolutelyaya.ultracraft.accessor;

//This extra accessor for Projectile Entities exists because some Custom Projectiles, like Hell Bullets,
//Override some of the ProjectileEntityAccessor Methods, but can't be marked as abstract because they're entities
//and thus need to be instantiatable. I can't think of a better way to do The ChainParry Counter than a DataTracker int;
//having one mixed into all ProjectileEntities and then an additional one for overriding classes while ignoring the super class' one, seems kind of dumb.
//I'm mainly writing this in case I change stuff about Parrying or rewrite this at some point (which I should)
//and go "what the fuck was I thinking here ???", so here we are. This is what you were thinking yaya
public interface ChainParryAccessor
{
	int getParryCount();
	
	void setParryCount(int i);
}
