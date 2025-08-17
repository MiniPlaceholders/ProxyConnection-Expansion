package io.github.miniplaceholders.expansion.proxyconnection.fabric;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.pokeskies.fabricpluginmessaging.PluginMessageEvent;
import com.pokeskies.fabricpluginmessaging.PluginMessagePacket;
import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.expansion.proxyconnection.common.BungeeMessageTypes;
import io.github.miniplaceholders.expansion.proxyconnection.common.PlatformProvider;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class FabricProvider extends PlatformProvider<MinecraftServer, Object> {
    public FabricProvider(Object platformInstance, Object miniInstance) {
        super((MinecraftServer) platformInstance, miniInstance);
    }

    @Override
    public Expansion.Builder provideBuilder() {
        executor.schedule(() -> {
            final var players = platformInstance.getPlayerList().getPlayers().iterator();
            if (!players.hasNext()) {
                return;
            }
            final ServerPlayer player = players.next();
            for (String server : dataCache.getServers()) {
                final ByteArrayDataOutput output = ByteStreams.newDataOutput();
                output.writeUTF(BungeeMessageTypes.PLAYER_COUNT.rawType());
                output.writeUTF(server);
                ServerPlayNetworking.send(player, new PluginMessagePacket(output.toByteArray()));
            }
        }, 5, TimeUnit.SECONDS);

        executor.schedule(() -> {
            final var players = platformInstance.getPlayerList().getPlayers().iterator();
            if (!players.hasNext()) {
                return;
            }
            final ServerPlayer player = players.next();
            final ByteArrayDataOutput output = ByteStreams.newDataOutput();
            output.writeUTF(BungeeMessageTypes.GET_SERVERS.rawType());
            ServerPlayNetworking.send(player, new PluginMessagePacket(output.toByteArray()));
        }, 10, TimeUnit.MINUTES);

        PluginMessageEvent.EVENT.register((payload, context) -> {
            final ByteArrayDataInput data  = ByteStreams.newDataInput(payload.getData());
            final String subchannel = data.readUTF();
            if (BungeeMessageTypes.PLAYER_COUNT.compareProvided(subchannel)) {
                final String server = data.readUTF();
                final int playerCount = data.readInt();
                dataCache.setPlayerCount(server, playerCount);
                return;
            }

            if (BungeeMessageTypes.GET_SERVERS.compareProvided(subchannel)) {
                final String serversString = data.readUTF();
                if (serversString.isEmpty()) {
                    return;
                }
                String[] servers = SPLIT_PATTERN.split(serversString);
                dataCache.updateServers(List.of(servers));
            }
        });
        return Expansion.builder("proxyconnection")
                .globalPlaceholder("player_count", (queue, context) -> {
                    if (queue.hasNext()) {
                        final String server = queue.pop().value();
                        final int serverPlayerCount = dataCache.getPlayerCount(server);
                        return Tag.preProcessParsed(Integer.toString(serverPlayerCount));
                    }
                    final int proxyPlayerCount = dataCache.getProxyPlayerCount();
                    return Tag.preProcessParsed(Integer.toString(proxyPlayerCount));
                });
    }
}
