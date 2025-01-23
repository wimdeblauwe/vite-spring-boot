package io.github.wimdeblauwe.vite.spring.boot;


import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties.Mode;
import io.github.wimdeblauwe.vite.spring.boot.ViteManifestReader.ManifestEntry;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      return Optional.of(devServerProperties.baseUrl() + "/" + prependWithStatic(resource));
    } else {
      String bundledPath = manifestReader.getBundledPath(prependWithStatic(resource));
      if (bundledPath == null) {
        LOGGER.warn("Could not resolve resource {} - Did you add it to vite.config.js?", resource);
        return Optional.empty();
      }

      return Optional.of("/" + bundledPath);
    }
  }

  public ManifestEntry getManifestEntry(String resource) {
    ManifestEntry manifestEntry = manifestReader.getManifestEntry(prependWithStatic(resource));
    if (manifestEntry == null) {
      // imported resources don't have the /static prefix
      manifestEntry = manifestReader.getManifestEntry(resource);
    }
    return manifestEntry;
  }

  private String prependWithStatic(String resource) {
    if (resource.startsWith("/")) {
      return "static" + resource;
    } else {
      return "static/" + resource;
    }
  }
}
