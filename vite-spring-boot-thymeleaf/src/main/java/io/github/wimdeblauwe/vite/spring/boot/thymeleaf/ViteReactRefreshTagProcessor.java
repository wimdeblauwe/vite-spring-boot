package io.github.wimdeblauwe.vite.spring.boot.thymeleaf;

import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties.Mode;
import io.github.wimdeblauwe.vite.spring.boot.ViteDevServerConfigurationProperties;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Allows adding the Vite React refresh code in the &lt;head&gt; of the HTML.
 * This is needed when you want to render React components on your Thymeleaf page to have HMR working fine.
 * Example:
 *
 * <pre>{@code
 * <head>
 *  <vite:react-refresh></vite:react-refresh>
 * </head>
 * }</pre>
 * <p>
 * Note that this will only inject the React refresh code when running in {@link Mode#DEV} mode.
 */
public class ViteReactRefreshTagProcessor extends AbstractElementModelProcessor {

  private final ViteConfigurationProperties properties;
  private final ViteDevServerConfigurationProperties devServerProperties;

  public ViteReactRefreshTagProcessor(String dialectPrefix,
                                      ViteConfigurationProperties properties,
                                      ViteDevServerConfigurationProperties devServerProperties) {
    super(TemplateMode.HTML, dialectPrefix, "react-refresh", true, null, false, 10_000);
    this.properties = properties;
    this.devServerProperties = devServerProperties;
  }

  @Override
  protected void doProcess(ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler) {
    IModelFactory modelFactory = context.getModelFactory();

    // Clear the existing content of the model
    model.reset();

    if (properties.mode() == Mode.DEV) {
      // Add the new script element
      model.add(modelFactory.createOpenElementTag("script", "type", "module"));
      String content = """
                  import RefreshRuntime from '%s/@react-refresh'
                  RefreshRuntime.injectIntoGlobalHook(window)
                  window.$RefreshReg$ = () => {}
                  window.$RefreshSig$ = () => (type) => type
                  window.__vite_plugin_react_preamble_installed__ = true
              """.formatted(devServerProperties.baseUrl());

      model.add(modelFactory.createText(content));
      model.add(modelFactory.createCloseElementTag("script"));
    }
  }

}
