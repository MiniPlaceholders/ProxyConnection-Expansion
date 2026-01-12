package io.github.miniplaceholders.expansion.proxyconnection.paper;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.expansion.proxyconnection.common.BungeeMessageTypes;
import io.github.miniplaceholders.expansion.proxyconnection.common.PlatformProvider;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class PaperProvider extends PlatformProvider<Server, JavaPlugin> {
    private final PluginMessageListenerWrapper listenerWrapper;

    public PaperProvider(Object platformInstance, Object miniInstance) {
        super((Server) platformInstance, (JavaPlugin) miniInstance);
        this.listenerWrapper = new PluginMessageListenerWrapper();
    }

    @Override
    public Expansion.Builder provideBuilder() {
        platformInstance.getMessenger().registerOutgoingPluginChannel(miniInstance, LEGACY_CHANNEL);
        platformInstance.getMessenger().registerIncomingPluginChannel(miniInstance, LEGACY_CHANNEL, listenerWrapper);
        executor.scheduleAtFixedRate(() -> {
            final var players = platformInstance.getOnlinePlayers().iterator();
            if (!players.hasNext()) {
                return;
            }
            final Player player = players.next();
            for (String server : dataCache.getServers()) {
                final ByteArrayDataOutput output = ByteStreams.newDataOutput();
                output.writeUTF(BungeeMessageTypes.PLAYER_COUNT.rawType());
                output.writeUTF(server);
                player.sendPluginMessage(miniInstance, LEGACY_CHANNEL, output.toByteArray());
            }
        }, 30, 7, TimeUnit.SECONDS);

        executor.scheduleAtFixedRate(() -> {
            final var players = platformInstance.getOnlinePlayers().iterator();
            if (!players.hasNext()) {
                return;
            }
            final Player player = players.next();
            final ByteArrayDataOutput output = ByteStreams.newDataOutput();
            output.writeUTF(BungeeMessageTypes.GET_SERVERS.rawType());
            player.sendPluginMessage(miniInstance, LEGACY_CHANNEL, output.toByteArray());
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

    private final class PluginMessageListenerWrapper implements PluginMessageListener {
        @Override
        public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
            if (!channel.equals(LEGACY_CHANNEL)) {
                return;
            }
            final ByteArrayDataInput in = ByteStreams.newDataInput(message);
            final String subchannel = in.readUTF();
            if (BungeeMessageTypes.PLAYER_COUNT.compareProvided(subchannel)) {
                final String server = in.readUTF();
                final int playerCount = in.readInt();
                PaperProvider.this.dataCache.setPlayerCount(server, playerCount);
                return;
            }

            if (BungeeMessageTypes.GET_SERVERS.compareProvided(subchannel)) {
                final String serversString = in.readUTF();
                if (serversString.isEmpty()) {
                    return;
                }
                String[] servers = SPLIT_PATTERN.split(serversString);
                PaperProvider.this.dataCache.updateServers(List.of(servers));
            }
        }
    }
}
