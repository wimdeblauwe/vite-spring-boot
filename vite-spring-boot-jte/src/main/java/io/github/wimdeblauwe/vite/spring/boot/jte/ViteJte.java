package io.github.wimdeblauwe.vite.spring.boot.jte;

import static io.github.wimdeblauwe.vite.spring.boot.jte.TagFactory.generateScriptTag;

import java.util.Arrays;
import java.util.Objects;

import gg.jte.Content;
import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteDevServerConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteLinkResolver;

/**
 * @author Panos Bariamis (pbaris)
 */
public class ViteJte {

    private static ViteLinkResolver linkResolver;
    private static ViteConfigurationProperties properties;
    private static ViteDevServerConfigurationProperties devServerProperties;

    public static void init(final ViteLinkResolver linkResolver,
                     final ViteConfigurationProperties properties,
                     final ViteDevServerConfigurationProperties devServerProperties) {

        ViteJte.linkResolver = linkResolver;
        ViteJte.properties = properties;
        ViteJte.devServerProperties = devServerProperties;
    }

    @SuppressWarnings("unused")
    public static Content viteClient() {
        if (properties.mode() == ViteConfigurationProperties.Mode.BUILD) {
            return null;
        }

        return output -> output.writeContent(generateScriptTag(devServerProperties.baseUrl() + "/@vite/client"));
    }

    @SuppressWarnings("unused")
    public static Content viteReactRefresh() {
        if (properties.mode() == ViteConfigurationProperties.Mode.BUILD) {
            return null;
        }

        String scriptContent = """
              import RefreshRuntime from '%s/@react-refresh'
              RefreshRuntime.injectIntoGlobalHook(window)
              window.$RefreshReg$ = () => {}
              window.$RefreshSig$ = () => (type) => type
              window.__vite_plugin_react_preamble_installed__ = true
            """.formatted(devServerProperties.baseUrl());

        return output -> output.writeContent(TagFactory.generateScriptTagWithContent(scriptContent));
    }

    @SuppressWarnings("unused")
    public static Content viteEntries(final String... entries) {
        var handler = new ViteJteEntriesHandler(linkResolver);

        Arrays.stream(entries)
            .filter(Objects::nonNull)
            .forEach(handler::handleEntry);

        return output -> handler.getHtmlEntries().forEach(output::writeContent);
    }
}
