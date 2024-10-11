package io.github.wimdeblauwe.vite.spring.boot.thymeleaf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.model.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TagFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(TagFactory.class);

  private final IModelFactory modelFactory;

  public TagFactory(IModelFactory modelFactory) {
    this.modelFactory = modelFactory;
  }

  public ScriptTags generateScriptTags(String srcValue) {
    LOGGER.debug("Generating script tag for {}", srcValue);
    // Use LinkedHashMap for predicable order in the HTML
    LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
    attributes.put("type", "module");
    attributes.put("src", srcValue);

    IOpenElementTag scriptOpenTag = modelFactory.createOpenElementTag("script",
        attributes,
        AttributeValueQuotes.DOUBLE, false);
    ICloseElementTag scriptCloseTag = modelFactory.createCloseElementTag("script");
    return new ScriptTags(scriptOpenTag, scriptCloseTag);
  }

  public IStandaloneElementTag generateCssLinkTag(String hrefValue) {
    LOGGER.debug("Generating CSS tag for {}", hrefValue);
    // Use LinkedHashMap for predicable order in the HTML
    LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
    attributes.put("rel", "stylesheet");
    attributes.put("href", hrefValue);

    return modelFactory.createStandaloneElementTag("link",
        attributes,
        AttributeValueQuotes.DOUBLE, false, false);
  }

  public ScriptWithTextTags generateScriptWithContentTags(String content) {
    LOGGER.debug("Generating script tag for {}", content);
    IOpenElementTag scriptOpenTag = modelFactory.createOpenElementTag("script",
            Map.of("type", "module"),
            AttributeValueQuotes.DOUBLE, false);
    IText text = modelFactory.createText(content);
    ICloseElementTag scriptCloseTag = modelFactory.createCloseElementTag("script");
    return new ScriptWithTextTags(scriptOpenTag, text, scriptCloseTag);
  }

  public record ScriptTags(IOpenElementTag openTag, ICloseElementTag closeTag) {

    public void addTagsTo(List<ITemplateEvent> htmlEntries) {
      htmlEntries.add(openTag);
      htmlEntries.add(closeTag);
    }
  }

  public record ScriptWithTextTags(IOpenElementTag openTag, IText text, ICloseElementTag closeTag) {

  }
}
