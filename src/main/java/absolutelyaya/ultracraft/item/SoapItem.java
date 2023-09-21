package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.entity.projectile.ThrownSoapEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2i;

public class SoapItem extends AbstractWeaponItem
{
	public SoapItem(Settings settings)
	{
		super(settings, 0, 0);
	}
	
	@Override
	public boolean onPrimaryFire(World world, PlayerEntity user, Vec3d userVelocity)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(user).getGunCooldownManager();
		if(!cdm.isUsable(this, GunCooldownManager.PRIMARY))
			return false;
		ItemStack stack = user.getMainHandStack();
		if(!(stack.getItem() instanceof SoapItem))
			return false;
		throwSoap(user, stack.copyWithCount(1));
		cdm.setCooldown(this, user.isCreative() ? 5 : 20, GunCooldownManager.PRIMARY);
		user.swingHand(Hand.MAIN_HAND);
		if(!user.isCreative())
			stack.decrement(1);
		return true;
	}
	
	public void onOffhandThrow(World world, PlayerEntity user)
	{
		ItemStack stack = user.getOffHandStack();
		if(!(stack.getItem() instanceof SoapItem))
			return;
		throwSoap(user, stack.copyWithCount(1));
		if(!user.isCreative())
			stack.decrement(1);
	}
	
	void throwSoap(PlayerEntity player, ItemStack stack)
	{
		ThrownSoapEntity.spawn(player, stack);
	}
	
	@Override
	public Vector2i getHUDTexture()
	{
		return null;
	}
	
	@Override
	String getControllerName()
	{
		return null;
	}
	
	@Override
	public boolean hasVariantBG()
	{
		return false;
	}
	
	@Override
	Item[] getVariants()
	{
		return new Item[0];
	}
	
	@Override
	int getSwitchCooldown()
	{
		return 0;
	}
	
	@Override
	public boolean shouldAim()
	{
		return false;
	}
}
