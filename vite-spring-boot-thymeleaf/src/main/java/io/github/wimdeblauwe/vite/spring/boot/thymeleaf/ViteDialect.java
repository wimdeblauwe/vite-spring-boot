package io.github.wimdeblauwe.vite.spring.boot.thymeleaf;

import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteDevServerConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteLinkResolver;
import java.util.Set;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

public class ViteDialect extends AbstractProcessorDialect {

  private final ViteConfigurationProperties properties;
  private final ViteDevServerConfigurationProperties serverProperties;
  private final ViteLinkResolver linkResolver;

  public ViteDialect(
      ViteConfigurationProperties properties,
      ViteDevServerConfigurationProperties serverProperties,
      ViteLinkResolver linkResolver) {
    super("Vite Dialect", "vite", 1000);
    this.properties = properties;
    this.serverProperties = serverProperties;
    this.linkResolver = linkResolver;
  }

  @Override
  public Set<IProcessor> getProcessors(String dialectPrefix) {
    return Set.of(
        new ViteClientTagProcessor(dialectPrefix, properties, serverProperties),
        new ViteTagProcessor(dialectPrefix, linkResolver)
    );
  }
}
