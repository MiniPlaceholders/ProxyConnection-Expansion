package io.github.miniplaceholders.expansion.example;

import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.api.provider.ExpansionProvider;
import io.github.miniplaceholders.api.provider.LoadRequirement;
import net.kyori.adventure.text.minimessage.tag.Tag;

public final class ExampleExpansionProvider implements ExpansionProvider {

    @Override
    public Expansion provideExpansion() {
        return Expansion.builder("example")
                .author("MiniPlaceholders Contributors")
                .version(Constants.VERSION)
                .globalPlaceholder("some_placeholder", (queue, ctx) -> {
                    return Tag.preProcessParsed("Helloooooooo");
                }).build();
    }

    @Override
    public LoadRequirement loadRequirement() {
        return LoadRequirement.requiredComplement("someplugin", "SomePlugin");
    }
}
