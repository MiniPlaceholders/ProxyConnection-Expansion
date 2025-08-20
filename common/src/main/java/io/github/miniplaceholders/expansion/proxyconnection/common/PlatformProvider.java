package io.github.miniplaceholders.expansion.proxyconnection.common;

import io.github.miniplaceholders.api.Expansion;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

public abstract class PlatformProvider<S, M> {
    public static final String LEGACY_CHANNEL = "BungeeCord";
    public static final String MODERN_CHANNEL = "bungeecord:main";
    public static final Pattern SPLIT_PATTERN = Pattern.compile(", ");
    protected final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    protected S platformInstance;
    protected M miniInstance;
    protected final DataCache dataCache = new DataCache();

    protected PlatformProvider(S platformInstance, M miniInstance) {
        this.platformInstance = platformInstance;
        this.miniInstance = miniInstance;
    }

    public abstract Expansion.Builder provideBuilder();
}
