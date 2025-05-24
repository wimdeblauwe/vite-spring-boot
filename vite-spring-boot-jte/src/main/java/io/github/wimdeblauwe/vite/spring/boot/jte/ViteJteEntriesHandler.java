package io.github.wimdeblauwe.vite.spring.boot.jte;

import static io.github.wimdeblauwe.vite.spring.boot.jte.TagFactory.generateCssLinkTag;
import static io.github.wimdeblauwe.vite.spring.boot.jte.TagFactory.generateScriptTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import io.github.wimdeblauwe.vite.spring.boot.ViteLinkResolver;
import io.github.wimdeblauwe.vite.spring.boot.ViteManifestReader.ManifestEntry;

/**
 * @author Panos Bariamis (pbaris)
 */
class ViteJteEntriesHandler {
    private static final Pattern CSS = Pattern.compile(".*\\.(css|less|sass|scss|styl|stylus|pcss|postcss)$");

    private final ViteLinkResolver linkResolver;
    private final List<String> htmlEntries = new ArrayList<>();
    private final Set<String> references = new HashSet<>();

    ViteJteEntriesHandler(final ViteLinkResolver linkResolver) {
        this.linkResolver = linkResolver;
    }

    void handleEntry(final String entry) {
        addEntryIfMissing(entry, true);

        ManifestEntry manifestEntry = linkResolver.getManifestEntry(entry);
        if (manifestEntry != null) {
            manifestEntry.css().forEach(linkedCss -> addEntryIfMissing(linkedCss, true));
            manifestEntry.imports().forEach(this::handleImportedResource);
        }
    }

    List<String> getHtmlEntries() {
        return htmlEntries;
    }

    private void addEntryIfMissing(final String entry, final boolean resolve) {
        if (references.add(entry)) {
            if (resolve) {
                linkResolver.resolveResource(entry).ifPresent(resource ->
                    htmlEntries.add(CSS.matcher(entry).find() ? generateCssLinkTag(resource) : generateScriptTag(resource)));

            } else {
                htmlEntries.add(CSS.matcher(entry).find() ? generateCssLinkTag(entry) : generateScriptTag(entry));
            }
        }
    }

    private void handleImportedResource(final String resource) {
        ManifestEntry manifestEntry = linkResolver.getManifestEntry(resource);
        String file = "/" + manifestEntry.file();
        addEntryIfMissing(file, false);

        manifestEntry.css().forEach(linkedCss -> addEntryIfMissing(linkedCss, false));
        manifestEntry.imports().forEach(this::handleImportedResource);
    }
}
