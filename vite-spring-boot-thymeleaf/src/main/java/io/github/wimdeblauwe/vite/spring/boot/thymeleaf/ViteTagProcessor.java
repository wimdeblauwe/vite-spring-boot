package io.github.wimdeblauwe.vite.spring.boot.thymeleaf;

import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties.Mode;
import io.github.wimdeblauwe.vite.spring.boot.ViteLinkResolver;
import io.github.wimdeblauwe.vite.spring.boot.ViteManifestReader.ManifestEntry;
import io.github.wimdeblauwe.vite.spring.boot.thymeleaf.TagFactory.ScriptTags;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.AbstractModelVisitor;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Allows adding the entrypoints to your HTML. For example:
 * <pre>{@code
 * <vite:vite>
 *     <vite:entry value="/css/application.css"></vite:entry>
 *     <vite:entry value="/css/styles.scss"></vite:entry>
 *     <vite:entry value="/js/hello.ts"></vite:entry>
 * </vite:vite>
 * }</pre>
 * <p>
 * This code was written with some inspiration from <a
 * href="https://github.com/laravel/framework/blob/11.x/src/Illuminate/Foundation/Vite.php">Vite.php</a> (from the Laravel framework).
 */
public class ViteTagProcessor extends AbstractElementModelProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ViteTagProcessor.class);
  private static final String TAG_NAME = "vite";
  private static final String ENTRY_TAG_NAME = "entry";
  private static final int PRECEDENCE = 1000;

  private final ViteLinkResolver linkResolver;

  public ViteTagProcessor(String dialectPrefix,
      ViteLinkResolver linkResolver) {
    super(TemplateMode.HTML, dialectPrefix, TAG_NAME, true, null, false, PRECEDENCE);
    this.linkResolver = linkResolver;
  }

  @Override
  protected void doProcess(ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler) {
    IModelFactory modelFactory = context.getModelFactory();
    ViteModelVisitor visitor = new ViteModelVisitor(modelFactory);
    model.accept(visitor);
    model.reset();
    visitor.getHtmlEntries().forEach(model::add);
  }

  private boolean isCssPath(String value) {
    // TODO use regex ->        return preg_match('/\.(css|less|sass|scss|styl|stylus|pcss|postcss)$/', $path) === 1;
    return value.endsWith(".css") || value.endsWith(".scss");
  }

  private class ViteModelVisitor extends AbstractModelVisitor {

    private final TagFactory tagFactory;
    private final List<ITemplateEvent> htmlEntries = new ArrayList<>();
    private final Set<String> references = new HashSet<>();
    private final Set<String> visitedManifestKeys = new HashSet<>();
    private final Set<String> collectedCssFiles = new LinkedHashSet<>();

    public ViteModelVisitor(IModelFactory modelFactory) {
      this.tagFactory = new TagFactory(modelFactory);
    }

    public List<ITemplateEvent> getHtmlEntries() {
      return htmlEntries;
    }

    @Override
    public void visit(IOpenElementTag openElementTag) {
      String elementName = openElementTag.getElementDefinition().getElementName().getElementName();
      //noinspection StatementWithEmptyBody
      if (elementName.equals(TAG_NAME)) {
        // no-op
      } else if (elementName.equals(ENTRY_TAG_NAME)) {
        IAttribute valueAttribute = openElementTag.getAttribute("value");
        if (valueAttribute != null) {
          String value = valueAttribute.getValue();
          handleValue(value);
        }
      }
    }

    private void handleValue(String value) {
      LOGGER.debug("resolving {}", value);

      // In DEV mode, vite serves modules and resolves imports itself — emit a single tag for
      // the entry and let the dev server handle the rest of the import graph.
      if (linkResolver.getProperties().mode() == Mode.DEV) {
        linkResolver.resolveResource(value).ifPresent(resource -> {
          if (isCssPath(value)) {
            executeIfNotOutputtedYet(value, () -> htmlEntries.add(tagFactory.generateCssLinkTag(resource)));
          } else {
            executeIfNotOutputtedYet(value, () -> {
              ScriptTags scriptTags = tagFactory.generateScriptTags(resource);
              scriptTags.addTagsTo(htmlEntries);
            });
          }
        });
        return;
      }

      // In BUILD mode, only emit a <script> tag for the entry chunk. The browser follows ESM
      // imports automatically, so emitting <script> tags for imported chunks causes duplicate
      // downloads (and shows unrelated chunks for sibling entries that happen to share a
      // dependency). CSS still has to be linked explicitly because Vite collects styles from
      // the entire import graph at build time.
      ManifestEntry startEntry = linkResolver.getManifestEntry(value);
      if (startEntry == null) {
        LOGGER.warn("Could not resolve resource {} - Did you add it to vite.config.js?", value);
        return;
      }

      collectCssFromImportGraph(value);

      if (startEntry.file() != null && !isCssPath(startEntry.file())) {
        String entryUrl = linkResolver.resolveBuiltAssetPath(startEntry.file());
        executeIfNotOutputtedYet(value, () -> {
          ScriptTags scriptTags = tagFactory.generateScriptTags(entryUrl);
          scriptTags.addTagsTo(htmlEntries);
        });
      }

      for (String cssFile : collectedCssFiles) {
        String cssUrl = linkResolver.resolveBuiltAssetPath(cssFile);
        executeIfNotOutputtedYet(cssFile, () -> htmlEntries.add(tagFactory.generateCssLinkTag(cssUrl)));
      }
    }

    private void collectCssFromImportGraph(String manifestKey) {
      if (!visitedManifestKeys.add(manifestKey)) {
        return;
      }
      ManifestEntry entry = linkResolver.getManifestEntry(manifestKey);
      if (entry == null) {
        return;
      }
      if (entry.css() != null) {
        collectedCssFiles.addAll(entry.css());
      }
      if (entry.imports() != null) {
        for (String importedKey : entry.imports()) {
          collectCssFromImportGraph(importedKey);
        }
      }
    }

    private void executeIfNotOutputtedYet(String value, Runnable runnable) {
      boolean wasAdded = references.add(value);
      if (wasAdded) {
        runnable.run();
      }
    }
  }
}