package io.github.wimdeblauwe.vite.spring.boot.jte;

import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteDevServerConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteLinkResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Panos Bariamis (pbaris)
 */
public class ViteJteWebConfiguration implements WebMvcConfigurer {

    private final ViteLinkResolver linkResolver;
    private final ViteConfigurationProperties properties;
    private final ViteDevServerConfigurationProperties devServerProperties;

    public ViteJteWebConfiguration(final ViteLinkResolver linkResolver,
                                final ViteConfigurationProperties properties,
                                final ViteDevServerConfigurationProperties devServerProperties) {

        this.linkResolver = linkResolver;
        this.properties = properties;
        this.devServerProperties = devServerProperties;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new ViteJteInterceptor(linkResolver, properties, devServerProperties));
    }
}
