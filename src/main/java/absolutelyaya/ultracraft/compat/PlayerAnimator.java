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
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	
	static List<KeyframeAnimation> ANIMATIONS;
	
	public static void init()
	{
		PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(new Identifier(Ultracraft.MOD_ID, "animation"), 42, (player) -> {
			if (player instanceof ClientPlayerEntity)
			{
				ModifierLayer<IAnimation> testAnimation =  new ModifierLayer<>();
				testAnimation.addModifierBefore(new MirrorModifier(player.getMainArm().equals(Arm.LEFT)));
				return testAnimation;
			}
			return null;
		});
		
		PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, animationStack) -> {
			ModifierLayer<IAnimation> layer = new ModifierLayer<>();
			animationStack.addAnimLayer(69, layer);
			PlayerAnimationAccess.getPlayerAssociatedData(player).set(new Identifier(Ultracraft.MOD_ID, "modify"), layer);
		});
		
		Optional<ModContainer> optionalContainer = FabricLoader.getInstance().getModContainer(Ultracraft.MOD_ID);
		if(optionalContainer.isEmpty())
			return;
		ModContainer container = optionalContainer.get();
		Optional<Path> internalPresetsDir = container.findPath("assets/ultracraft/player_animation");
		
		try
		{
			ANIMATIONS = new AnimationJson().deserialize(JsonHelper.deserialize(Files.readString(Paths.get(internalPresetsDir.get() + "/player.animation.json"))),
					null, null);
		}
		catch (IOException e)
		{
			Ultracraft.LOGGER.error("Error while loading Player Animations", e);
		}
	}
	
	public static void playAnimation(ClientPlayerEntity player, int animID, int fade, boolean firstPerson)
	{
		if(MinecraftClient.getInstance().player == null)
			return;
		ModifierLayer<IAnimation> testAnimation = (ModifierLayer<IAnimation>)PlayerAnimationAccess.getPlayerAssociatedData(player)
															   .get(new Identifier(Ultracraft.MOD_ID, "animation"));
		
		KeyframeAnimation anim;
		if(animID >= 0)
			anim = ANIMATIONS.get(animID);
		else
			anim = null;
		
		if (testAnimation.getAnimation() != null && anim == null)
			testAnimation.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fade, Ease.LINEAR), null);
		else if(anim != null)
		{
			testAnimation.replaceAnimationWithFade(AbstractFadeModifier.functionalFadeIn(fade, (modelName, type, value) -> value),
					new KeyframeAnimationPlayer(anim).setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
							.setFirstPersonConfiguration(new FirstPersonConfiguration().setShowRightArm(firstPerson).setShowLeftItem(false))
			);
		}
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeInt(animID);
		buf.writeInt(fade);
		ClientPlayNetworking.send(PacketRegistry.ANIMATION_C2S_PACKET_ID, buf);
	}
}
