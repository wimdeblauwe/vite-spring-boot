package io.github.wimdeblauwe.vite.spring.boot.jte;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteDevServerConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteLinkResolver;
import io.github.wimdeblauwe.vite.spring.boot.ViteManifestReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author Panos Bariamis (pbaris)
 */
@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties({ViteConfigurationProperties.class, ViteDevServerConfigurationProperties.class})
public class ViteJteAutoConfiguration {

    @Bean
    public ViteLinkResolver viteLinkResolver(final ViteConfigurationProperties properties,
                                             final ViteDevServerConfigurationProperties serverProperties,
                                             final ViteManifestReader manifestReader) {

        return new ViteLinkResolver(properties, serverProperties, manifestReader);
    }

    @Bean
    public ViteManifestReader viteManifestReader(final ObjectMapper objectMapper, final ViteConfigurationProperties properties) {
        return new ViteManifestReader(objectMapper, properties);
    }

    @Bean
    public ViteJteWebConfiguration viteWebConfiguration(final ViteLinkResolver linkResolver,
                                                        final ViteConfigurationProperties properties,
                                                        final ViteDevServerConfigurationProperties devServerProperties) {

        return new ViteJteWebConfiguration(linkResolver, properties, devServerProperties);
    }
}
