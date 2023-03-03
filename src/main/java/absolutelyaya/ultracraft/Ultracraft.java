package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.registry.*;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;

public class Ultracraft implements ModInitializer
{
    public static final String MOD_ID = "ultracraft";
    public static final Logger LOGGER = LogUtils.getLogger();
    static int freezeTicks;
    public static Option FreezeOption = Option.FREE;
    public static Option HiVelOption = Option.FREE;
    
    @Override
    public void onInitialize()
    {
        EntityRegistry.register();
        BlockRegistry.registerBlocks();
        BlockEntityRegistry.register();
        ItemRegistry.register();
        PacketRegistry.registerC2S();
        BlockTagRegistry.register();
        SoundRegistry.register();
        KeybindRegistry.register();
    
        ServerTickEvents.END_SERVER_TICK.register(minecraft -> {
            if(freezeTicks > 0)
                freezeTicks--;
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            ((WingedPlayerEntity)newPlayer).setWingsVisible(((WingedPlayerEntity)oldPlayer).isWingsVisible());
            for (ServerPlayerEntity p : ((ServerWorld)newPlayer.world).getPlayers())
            {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeBoolean(((WingedPlayerEntity)oldPlayer).isWingsVisible());
                ServerPlayNetworking.send(p, PacketRegistry.RESPAWN_PACKET_ID, buf);
            }
        });
        
        LOGGER.info("Ultracraft initialized.");
    }
    
    public static boolean isTimeFrozen()
    {
        return freezeTicks > 0;
    }
    
    public static void freeze(int ticks)
    {
        if(!UltracraftClient.isFreezeEnabled())
            return;
        freezeTicks += ticks;
        LOGGER.info("Freezing for " + ticks + " ticks. (Intentional Visual Effect! Do not report!)");
    }
    
    public enum Option
    {
        FORCE_ON,
        FORCE_OFF,
        FREE
    }
}
