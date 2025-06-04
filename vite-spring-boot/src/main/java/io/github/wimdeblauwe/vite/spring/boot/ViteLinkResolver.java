package io.github.wimdeblauwe.vite.spring.boot;


import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties.Mode;
import io.github.wimdeblauwe.vite.spring.boot.ViteManifestReader.ManifestEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Knows how to resolve a resource link to either the live reload server URL, or the asset path.
 */
public class ViteLinkResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(ViteLinkResolver.class);

  private final ViteConfigurationProperties properties;
  private final ViteDevServerConfigurationProperties devServerProperties;
  private final ViteManifestReader manifestReader;

  public ViteLinkResolver(ViteConfigurationProperties properties,
                          ViteDevServerConfigurationProperties devServerProperties,
                          ViteManifestReader manifestReader) {
    this.properties = properties;
    this.devServerProperties = devServerProperties;
    this.manifestReader = manifestReader;
  }

  public Optional<String> resolveResource(String resource) {
    if (properties.mode() == Mode.DEV) {
      if (devServerProperties.host() == null) {
        LOGGER.warn("vite-dev-server-config.host has not been set - "
                    + "Please run `npm run dev` and restart the application to properly resolve resource {}",
                    resource);
      }
      StringBuilder builder = new StringBuilder(devServerProperties.baseUrl());
      if (properties.devModeContextPath() != null) {
        builder.append(properties.devModeContextPath())
            .append("/");
      } else {
        builder.append("/");
      }
      builder.append(prependWithPrefix(resource));
      return Optional.of(builder.toString());
    } else {
      String bundledPath = manifestReader.getBundledPath(prependWithPrefix(resource));
      if (bundledPath == null) {
        LOGGER.warn("Could not resolve resource {} - Did you add it to vite.config.js?", resource);
        return Optional.empty();
      }

      StringBuilder builder = new StringBuilder();
      if (properties.buildModeContextPath() != null) {
        builder.append(properties.buildModeContextPath())
            .append("/");
      } else {
        builder.append("/");
      }
      builder.append(bundledPath);
      return Optional.of(builder.toString());
    }
  }

  public ManifestEntry getManifestEntry(String resource) {
    ManifestEntry manifestEntry = manifestReader.getManifestEntry(prependWithPrefix(resource));
    if (manifestEntry == null) {
      // imported resources don't have the /static prefix
      manifestEntry = manifestReader.getManifestEntry(resource);
    }
    return manifestEntry;
  }

  private String prependWithPrefix(String resource) {
    if (resource.startsWith("/")) {
      return properties.viteEntriesPrefix() + resource;
    } else {
      return properties.viteEntriesPrefix() + "/" + resource;
    }
  }
}
