package absolutelyaya.ultracraft.compat;

import absolutelyaya.ultracraft.Ultracraft;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.gson.AnimationJson;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.spongepowered.include.com.google.gson.JsonDeserializationContext;
import org.spongepowered.include.com.google.gson.JsonElement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

public class PlayerAnimator
{
	static List<KeyframeAnimation> ANIMATIONS;
	
	public static void init()
	{
		PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(new Identifier(Ultracraft.MOD_ID, "animation"), 42, (player) -> {
			if (player instanceof ClientPlayerEntity) {
				//animationStack.addAnimLayer(42, testAnimation); //Add and save the animation container for later use.
				ModifierLayer<IAnimation> testAnimation =  new ModifierLayer<>();
				testAnimation.addModifierBefore(new MirrorModifier(player.getMainArm().equals(Arm.LEFT))); //Mirror the animation
				return testAnimation;
			}
			return null;
		});
		
		PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, animationStack) -> {
			ModifierLayer<IAnimation> layer = new ModifierLayer<>();
			animationStack.addAnimLayer(69, layer);
			PlayerAnimationAccess.getPlayerAssociatedData(player).set(new Identifier(Ultracraft.MOD_ID, "test"), layer);
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
	
	public static void playAnimation(int animID, int fade)
	{
		if(MinecraftClient.getInstance().player == null)
			return;
		ModifierLayer<IAnimation> testAnimation;
		if (new Random().nextBoolean())
			testAnimation = (ModifierLayer<IAnimation>)PlayerAnimationAccess.getPlayerAssociatedData(MinecraftClient.getInstance().player)
															   .get(new Identifier(Ultracraft.MOD_ID, "animation"));
		else
			testAnimation = (ModifierLayer<IAnimation>)PlayerAnimationAccess.getPlayerAssociatedData(MinecraftClient.getInstance().player)
															   .get(new Identifier(Ultracraft.MOD_ID, "test"));
		
		KeyframeAnimation anim;
		if(animID >= 0)
			anim = ANIMATIONS.get(animID);
		else
			anim = null;
		
		if (testAnimation.getAnimation() != null && anim == null)
		{
			//It will fade out from the current animation, null as newAnimation means no animation.
			testAnimation.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fade, Ease.LINEAR), null);
		}
		else if(anim != null)
		{
			//Fade from current animation to a new one.
			//Will not fade if there is no animation currently.
			testAnimation.replaceAnimationWithFade(AbstractFadeModifier.functionalFadeIn(fade, (modelName, type, value) -> value),
					new KeyframeAnimationPlayer(anim).setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
							.setFirstPersonConfiguration(new FirstPersonConfiguration().setShowRightArm(true).setShowLeftItem(false))
			);
		}
	}
}
