package absolutelyaya.ultracraft.compat;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.gson.AnimationJson;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

public class PlayerAnimator
{
	public static final int DASH_FORWARD = 0;
	public static final int DASH_JUMP = 1;
	public static final int DASH_BACK = 2;
	public static final int DASH_LEFT = 3;
	public static final int DASH_RIGHT = 4;
	public static final int START_SLIDE = 5;
	public static final int STOP_SLIDE = 6;
	public static final int SLAM_LOOP = 7;
	public static final int SLAM_IMPACT = 8;
	public static final int SLAM_JUMP = 9;
	public static final int SLAM_DIVE = 10;
	public static final int SLAMSTORE_DIVE = 11;
	public static final int PUNCH = 12;
	
	static List<KeyframeAnimation> ANIMATIONS;
	static boolean DISABLED = false;
	
	public static void init()
	{
		PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(new Identifier(Ultracraft.MOD_ID, "animation"), 42, (player) -> {
			ModifierLayer<IAnimation> animationLayer = new ModifierLayer<>();
			animationLayer.addModifierBefore(new MirrorModifier(player.getMainArm().equals(Arm.LEFT)));
			return animationLayer;
		});
		
		PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, animationStack) -> {
			ModifierLayer<IAnimation> layer = new ModifierLayer<>();
			animationStack.addAnimLayer(69, layer);
			PlayerAnimationAccess.getPlayerAssociatedData(player).set(new Identifier(Ultracraft.MOD_ID, "modify"), layer);
		});
		
		Optional<ModContainer> optionalContainer = FabricLoader.getInstance().getModContainer(Ultracraft.MOD_ID);
		if(optionalContainer.isEmpty())
			return;
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener()
		{
			@Override
			public Identifier getFabricId() {
				return new Identifier(Ultracraft.MOD_ID, "player_anim");
			}
			
			@Override
			public void reload(ResourceManager manager)
			{
				try
				{
					Optional<Resource> r = manager.getResource(new Identifier(Ultracraft.MOD_ID, "player_animation/player.animation.json"));
					if(r.isEmpty())
						throw new Exception("Internal player Animation File wasn't found.");
					ANIMATIONS = new AnimationJson().deserialize(JsonHelper.deserialize(new InputStreamReader(r.get().getInputStream())), null, null);
					DISABLED = false;
					r.stream().close();
				}
				catch (Exception e)
				{
					Ultracraft.LOGGER.error("Error while loading Player Animations", e);
					DISABLED = true;
				}
			}
		});
	}
	
	//TODO: Animations don't pause during Time-Freeze
	public static void playAnimation(AbstractClientPlayerEntity player, int animID, int fade, boolean firstPerson)
	{
		playAnimation(player, animID, fade, firstPerson, false);
	}
	
	public static void playAnimation(AbstractClientPlayerEntity player, int animID, int fade, boolean firstPerson, boolean fromServer)
	{
		if(player == null)
			return;
		if(!fromServer)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeInt(animID);
			buf.writeInt(fade);
			buf.writeBoolean(firstPerson);
			ClientPlayNetworking.send(PacketRegistry.ANIMATION_C2S_PACKET_ID, buf);
		}
		if(DISABLED)
			return;
		if(!firstPerson && player.isMainPlayer() && !MinecraftClient.getInstance().gameRenderer.getCamera().isThirdPerson())
			return;
		ModifierLayer<IAnimation> animationLayer = (ModifierLayer<IAnimation>)PlayerAnimationAccess.getPlayerAssociatedData(player)
															   .get(new Identifier(Ultracraft.MOD_ID, "animation"));
		
		KeyframeAnimation anim;
		if(animID >= 0)
			anim = ANIMATIONS.get(animID);
		else
			anim = null;
		
		if (animationLayer.getAnimation() != null && anim == null)
			animationLayer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fade, Ease.LINEAR), null);
		else if(anim != null)
		{
			animationLayer.replaceAnimationWithFade(AbstractFadeModifier.functionalFadeIn(fade, (modelName, type, value) -> value),
					new KeyframeAnimationPlayer(anim).setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
							.setFirstPersonConfiguration(new FirstPersonConfiguration())
			);
		}
	}
}
