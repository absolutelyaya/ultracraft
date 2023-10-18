package absolutelyaya.ultracraft.mixin.client.gui;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.Ultraconfig;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.client.gui.screen.IntroScreen;
import absolutelyaya.ultracraft.client.gui.widget.TitleBGButton;
import absolutelyaya.ultracraft.client.rendering.TitleBGRenderer;
import absolutelyaya.ultracraft.client.rendering.TitleLimboBGRenderer;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen
{
    @Mutable @Shadow @Final private RotatingCubeMapRenderer backgroundRenderer;
    @Shadow @Final public static CubeMapRenderer PANORAMA_CUBE_MAP;
    
    @Shadow public abstract boolean mouseClicked(double mouseX, double mouseY, int button);
    
    @Shadow private @Nullable SplashTextRenderer splashText;
    private static final Ultraconfig config = UltracraftClient.getConfig();
    private static final Identifier BG_ICON_TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/misc/bg_icons.png");
    RotatingCubeMapRenderer ultraBG, defaultBG, limboBG;
    SoundInstance wind;
    int windTicks;
    
    protected TitleScreenMixin(Text title)
    {
        super(title);
    }
    
    @Inject(at = @At("TAIL"), method = "init()V")
    private void init(CallbackInfo info)
    {
        defaultBG = new RotatingCubeMapRenderer(PANORAMA_CUBE_MAP);
        ultraBG = new TitleBGRenderer(PANORAMA_CUBE_MAP);
        limboBG = new TitleLimboBGRenderer(PANORAMA_CUBE_MAP);
        
        LocalDateTime time = LocalDateTime.now();
        if(time.getHour() == 6 && time.getMinute() == 9)
            splashText = new SplashTextRenderer("No Sex :pensive:");
        else if(time.getHour() == 4 && time.getMinute() == 20)
            splashText = new SplashTextRenderer("WOOOOOO 420!"); //not encouraging drug use, it's just the funny number :D
        else if(time.getHour() == 3 && time.getMinute() == 33)
            splashText = new SplashTextRenderer("Something Wicked this way comes");
    }
    
    @Inject(method = "tick", at = @At("HEAD"))
    void onTick(CallbackInfo ci)
    {
        if((!IntroScreen.SEQUENCE_FINISHED && !config.lastVersion.equals(Ultracraft.VERSION) || config.repeatIntro) && !config.neverIntro)
        {
            client.setScreen(new IntroScreen());
            MinecraftClient.getInstance().getSoundManager().stop(wind);
        }
        
        if(wind == null)
            wind = PositionedSoundInstance.ambient(SoundRegistry.ELEVATOR_FALL.value(), 1f, 0.75f);
        else if (wind.canPlay() && windTicks <= 0 && backgroundRenderer.equals(ultraBG))
        {
            if (wind.getSound() != SoundManager.MISSING_SOUND)
                MinecraftClient.getInstance().getSoundManager().play(wind);
            windTicks += 190;
        }
        if(windTicks > 0)
            windTicks--;
    }
    
    @Inject(method = "init", at = @At("TAIL"))
    void onInit(CallbackInfo ci)
    {
        TitleBGButton limbo = addDrawableChild(new TitleBGButton(-24, 2,
                32, 32, 64, 0, 32, BG_ICON_TEXTURE, 128, 128,
                button -> setBG("limbo"), Text.translatable("narrator.button.background.limbo")));
        TitleBGButton ultracraft = addDrawableChild(new TitleBGButton(-24, 34,
                32, 32, 32, 0, 32, BG_ICON_TEXTURE, 128, 128,
                button -> setBG("ultracraft"), Text.translatable("narrator.button.background.ultracraft")));
        TitleBGButton vanilla = addDrawableChild(new TitleBGButton(-24, 66,
                32, 32, 0, 0, 32, BG_ICON_TEXTURE, 128, 128,
                button -> setBG("vanilla"), Text.translatable("narrator.button.background.vanilla")));
        switch(config.BGID)
        {
            case "limbo" -> limbo.onPress();
            case "ultracraft" -> ultracraft.onPress();
            default -> vanilla.onPress();
        }
    }
    
    @Inject(method = "render", at = @At("TAIL"))
    void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci)
    {
        if(config.BGID.equals("limbo"))
        {
            Text t = Text.translatable("screen.ultracraft.title.subtitle");
            context.drawText(textRenderer, t, width / 2 - textRenderer.getWidth(t) / 2, 70, 0xffffff00, true);
        }
    }
    
    void setBG(String bg)
    {
        MinecraftClient.getInstance().getSoundManager().stop(wind);
        windTicks = 0;
        backgroundRenderer = switch(bg)
        {
            case "limbo" -> limboBG;
            case "ultracraft" -> ultraBG;
            default -> defaultBG;
        };
        config.BGID = bg;
        UltracraftClient.saveConfig();
    }
}
