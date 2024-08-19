package io.github.wimdeblauwe.vite.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration of the Vite Dev Server (for live reloading).
 * <p>
 * This does normally not be set manually, as the {@link ViteServerConfigurationPropertiesContextInitializer} does this automatically.
 */
@ConfigurationProperties(prefix = ViteDevServerConfigurationProperties.PREFIX)
public record ViteDevServerConfigurationProperties(String host, int port) {

  public static final String PREFIX = "vite-dev-server-config";

  public String baseUrl() {
    return "//" + host + ":" + port;
  }
}
