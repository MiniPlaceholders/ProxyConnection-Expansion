package io.github.miniplaceholders.expansion.proxyconnection.sponge;

import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.expansion.proxyconnection.common.BungeeMessageTypes;
import io.github.miniplaceholders.expansion.proxyconnection.common.PlatformProvider;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.network.channel.raw.RawDataChannel;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class SpongeProvider extends PlatformProvider<Server, Object> {
    public SpongeProvider(Object platformInstance, Object miniInstance) {
        super((Server) platformInstance, miniInstance);
    }

    @Override
    public Expansion.Builder provideBuilder() {
        final RawDataChannel rawDataChannel = platformInstance.game()
                .channelManager()
                .ofType(ResourceKey.resolve(MODERN_CHANNEL), RawDataChannel.class);
        rawDataChannel.play()
                .addHandler((data, state) -> {
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
        executor.scheduleAtFixedRate(() -> {
            final var players = platformInstance.onlinePlayers().iterator();
            if (!players.hasNext()) {
                return;
            }
            final ServerPlayer player = players.next();
            for (String server : dataCache.getServers()) {
                rawDataChannel.play().sendTo(player, buf -> {
                    buf.writeUTF(BungeeMessageTypes.PLAYER_COUNT.rawType());
                    buf.writeUTF(server);
                });
            }
        }, 30, 7, TimeUnit.SECONDS);

        executor.scheduleAtFixedRate(() -> {
            final var players = platformInstance.onlinePlayers().iterator();
            if (!players.hasNext()) {
                return;
            }
            final ServerPlayer player = players.next();
            rawDataChannel.play().sendTo(player,
                    buf -> buf.writeUTF(BungeeMessageTypes.GET_SERVERS.rawType()));
        }, 1, 3, TimeUnit.MINUTES);

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
