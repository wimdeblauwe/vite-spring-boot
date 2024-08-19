package io.github.wimdeblauwe.vite.spring.boot.thymeleaf;

import java.util.LinkedHashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.model.AttributeValueQuotes;
import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IStandaloneElementTag;
import org.thymeleaf.model.ITemplateEvent;

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

  public record ScriptTags(IOpenElementTag openTag, ICloseElementTag closeTag) {

    public void addTagsTo(List<ITemplateEvent> htmlEntries) {
      htmlEntries.add(openTag);
      htmlEntries.add(closeTag);
    }
  }
}
