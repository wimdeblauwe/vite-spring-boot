package io.github.wimdeblauwe.vite.spring.boot.thymeleaf;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteDevServerConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteLinkResolver;
import io.github.wimdeblauwe.vite.spring.boot.ViteManifestReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties({
    ViteConfigurationProperties.class,
    ViteDevServerConfigurationProperties.class})
public class ViteThymeleafAutoConfiguration {

  @Bean
  public ViteDialect viteDialect(
      ViteConfigurationProperties properties,
      ViteDevServerConfigurationProperties serverProperties,
      ViteLinkResolver linkResolver) {
    return new ViteDialect(properties, serverProperties, linkResolver);
  }

  @Bean
  public ViteLinkResolver viteLinkResolver(ViteConfigurationProperties properties,
      ViteDevServerConfigurationProperties serverProperties,
      ViteManifestReader manifestReader) {
    return new ViteLinkResolver(properties, serverProperties, manifestReader);
  }

  @Bean
  public ViteManifestReader viteManifestReader(ResourceLoader resourceLoader, ObjectMapper objectMapper, ViteConfigurationProperties properties) {
    return new ViteManifestReader(resourceLoader, objectMapper, properties);
  }
}
