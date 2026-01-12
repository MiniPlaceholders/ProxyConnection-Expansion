package io.github.miniplaceholders.expansion.proxyconnection;

import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.miniplaceholders.api.provider.ExpansionProvider;
import io.github.miniplaceholders.api.provider.LoadRequirement;
import io.github.miniplaceholders.api.provider.PlatformData;
import io.github.miniplaceholders.api.types.Platform;
import io.github.miniplaceholders.expansion.proxyconnection.common.PlatformProvider;
import io.github.miniplaceholders.expansion.proxyconnection.fabric.FabricProvider;
import io.github.miniplaceholders.expansion.proxyconnection.paper.PaperProvider;
import io.github.miniplaceholders.expansion.proxyconnection.sponge.SpongeProvider;
import team.unnamed.inject.Inject;

public final class ProxyConnectionExpansionProvider implements ExpansionProvider {
    @Inject
    private PlatformData data;

    @Override
    public Expansion provideExpansion() {
        final Object platformInstance = data.serverInstance();
        final Object miniInstance = data.complementInstance();
        final PlatformProvider<?, ?> provider = switch (MiniPlaceholders.platform()) {
            case PAPER -> new PaperProvider(platformInstance, miniInstance);
            case SPONGE -> new SpongeProvider(platformInstance, miniInstance);
            case FABRIC -> new FabricProvider(platformInstance, miniInstance);
            default -> throw new UnsupportedOperationException("Unsupported Platform");
        };
        return provider.provideBuilder()
                .author("MiniPlaceholders Contributors")
                .version(Constants.VERSION)
                .build();
    }

    @Override
    public LoadRequirement loadRequirement() {
        return LoadRequirement.anyOf(
            LoadRequirement.platform(Platform.PAPER),
            LoadRequirement.platform(Platform.SPONGE),
            LoadRequirement.allOf(
                LoadRequirement.platform(Platform.FABRIC),
                LoadRequirement.requiredComplement("fabricpluginmessaging")
            )
        );
    }
}
