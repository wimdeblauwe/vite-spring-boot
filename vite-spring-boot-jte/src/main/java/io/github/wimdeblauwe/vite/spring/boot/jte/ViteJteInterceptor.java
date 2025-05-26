package io.github.wimdeblauwe.vite.spring.boot.jte;

import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteDevServerConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteLinkResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Panos Bariamis (pbaris)
 */
public class ViteJteInterceptor implements HandlerInterceptor {

    private final ViteLinkResolver linkResolver;
    private final ViteConfigurationProperties properties;
    private final ViteDevServerConfigurationProperties devServerProperties;

    public ViteJteInterceptor(final ViteLinkResolver linkResolver,
                              final ViteConfigurationProperties properties,
                              final ViteDevServerConfigurationProperties devServerProperties) {

        this.linkResolver = linkResolver;
        this.properties = properties;
        this.devServerProperties = devServerProperties;
    }

    @Override
    public void postHandle(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response,
                           @NonNull final Object handler, @Nullable final ModelAndView modelAndView) throws Exception {

        if (modelAndView != null) {
            ViteJte.init(linkResolver, properties, devServerProperties);
        }
    }
}
