package absolutelyaya.ultracraft.fabric;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.PacketByteBuf;

public class UltracraftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Ultracraft.init();
        
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if(server.isRemote())
            {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeEnumConstant(Ultracraft.FreezeOption);
                buf.writeEnumConstant(Ultracraft.HiVelOption);
                NetworkManager.sendToPlayer(handler.player, PacketRegistry.SERVER_OPTIONS_PACKET_ID, buf);
            }
        });
    
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            Ultracraft.FreezeOption = Ultracraft.Option.FREE;
            Ultracraft.HiVelOption = Ultracraft.Option.FREE;
        });
    }
}
