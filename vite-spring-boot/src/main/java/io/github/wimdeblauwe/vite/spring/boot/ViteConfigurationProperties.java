package io.github.wimdeblauwe.vite.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;

import java.nio.file.Path;

/**
 * Configure the mode that Vite is running in.
 */
@ConfigurationProperties("vite")
public record ViteConfigurationProperties(
        @DefaultValue("build") Mode mode,
        @DefaultValue("classpath:/static/.vite/manifest.json") Resource manifest,
        Path devServerConfigFileLocation
) {

  public enum Mode {
    /**
     * Vite is running in live reload mode.
     */
    DEV,
    /**
     * Vite is running in build (production) mode.
     */
    BUILD
  }
}
