package io.github.miniplaceholders.expansion.proxyconnection.common;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DataCache {
    private final ConcurrentHashMap<@NotNull String, Integer> playerCountByServer = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<String> serverList = new CopyOnWriteArrayList<>();

    public void setPlayerCount(String server, int playerCount) {
        this.playerCountByServer.put(server, playerCount);
    }

    public int getPlayerCount(String server) {
        return this.playerCountByServer.getOrDefault(server, 0);
    }

    public int getProxyPlayerCount() {
        return this.playerCountByServer.reduceValuesToInt(100, i -> i, 0, Integer::sum);
    }

    public List<String> getServers() {
        return this.serverList;
    }

    public void updateServers(List<String> newServers) {
        serverList.clear();
        serverList.addAll(newServers);
    }
}
