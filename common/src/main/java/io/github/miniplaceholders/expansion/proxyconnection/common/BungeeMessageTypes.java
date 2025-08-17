package io.github.miniplaceholders.expansion.proxyconnection.common;

import java.util.Objects;

/**
 * GET Bungee Message Types
 *
 * @implNote Unused types have been added for possible future expansion of the project.
 */
public enum BungeeMessageTypes {
    PLAYER_COUNT("PlayerCount"),
    PLAYER_LIST("PlayerList"),
    GET_SERVERS("GetServers"),
    SERVER_IP("ServerIp");

    private final String rawType;
    BungeeMessageTypes(String rawType) {
        this.rawType = rawType;
    }

    public boolean compareProvided(final String o) {
        return Objects.equals(this.rawType, o);
    }

    public String rawType() {
        return this.rawType;
    }
}
