package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.Ultraconfig;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.client.gui.screen.IntroScreen;
import absolutelyaya.ultracraft.client.gui.widget.TitleBGButton;
import absolutelyaya.ultracraft.client.rendering.TitleBGRenderer;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen
{
    @Mutable @Shadow @Final private RotatingCubeMapRenderer backgroundRenderer;
    @Shadow @Final public static CubeMapRenderer PANORAMA_CUBE_MAP;
    
    @Shadow public abstract boolean mouseClicked(double mouseX, double mouseY, int button);
    
    private static final Ultraconfig config = UltracraftClient.getConfigHolder().get();
    private static final Identifier BG_ICON_TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/misc/bg_icons.png");
    RotatingCubeMapRenderer ultrabg, defaultbg;
    SoundInstance wind;
    int windTicks;
    
    protected TitleScreenMixin(Text title)
    {
        super(title);
    }
    
    @Inject(at = @At("TAIL"), method = "init()V")
    private void init(CallbackInfo info)
    {
        defaultbg = new RotatingCubeMapRenderer(PANORAMA_CUBE_MAP);
        ultrabg = new TitleBGRenderer(PANORAMA_CUBE_MAP);
    }
    
    @Inject(method = "tick", at = @At("HEAD"))
    void onTick(CallbackInfo ci)
    {
        if(!IntroScreen.SEQUENCE_FINISHED && !config.lastVersion.equals(Ultracraft.VERSION))
            client.setScreen(new IntroScreen());
        
        if(wind == null)
            wind = PositionedSoundInstance.ambient(SoundRegistry.ELEVATOR_FALL.value(), 1f, 0.75f);
        else if (wind.canPlay() && windTicks <= 0 && backgroundRenderer.equals(ultrabg))
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
        TitleBGButton ultracraft = addDrawableChild(new TitleBGButton(-24, 2,
                32, 32, 32, 0, 32, BG_ICON_TEXTURE, 64, 64,
                button -> setBG("ultracraft"), Text.translatable("narrator.button.background.ultracraft")));
        TitleBGButton vanilla = addDrawableChild(new TitleBGButton(-24, 34,
                32, 32, 0, 0, 32, BG_ICON_TEXTURE, 64, 64,
                button -> setBG("vanilla"), Text.translatable("narrator.button.background.vanilla")));
        (config.BGID.equals("ultracraft") ? ultracraft : vanilla).onPress();
    }
    
    void setBG(String bg)
    {
        MinecraftClient.getInstance().getSoundManager().stop(wind);
        windTicks = 0;
        backgroundRenderer = bg.equals("ultracraft") ? ultrabg : defaultbg;
        config.BGID = bg;
        UltracraftClient.getConfigHolder().save();
    }
}
