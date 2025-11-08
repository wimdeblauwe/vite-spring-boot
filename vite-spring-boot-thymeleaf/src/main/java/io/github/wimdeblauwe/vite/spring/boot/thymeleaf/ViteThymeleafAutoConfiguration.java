package io.github.wimdeblauwe.vite.spring.boot.thymeleaf;

import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteDevServerConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteLinkResolver;
import io.github.wimdeblauwe.vite.spring.boot.ViteManifestReader;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

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
  public ViteManifestReader viteManifestReader(JsonMapper jsonMapper, ViteConfigurationProperties properties) {
    return new ViteManifestReader(jsonMapper, properties);
  }
}
