package io.github.wimdeblauwe.vite.spring.boot.thymeleaf;

import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteDevServerConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteLinkResolver;
import io.github.wimdeblauwe.vite.spring.boot.ViteManifestReader;
import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.assertj.core.api.Assertions.assertThat;

class ViteTagProcessorTest {

  private TemplateEngine templateEngine;
  private TemplateEngine issue19TemplateEngine;

  @BeforeEach
  void setUp() throws Exception {
    // Configure Thymeleaf
    ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setPrefix("/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setTemplateMode(TemplateMode.HTML);
    templateResolver.setCharacterEncoding("UTF-8");

    JsonMapper jsonMapper = JsonMapper.builder().build();

    // Create and configure template engine with example manifest
    templateEngine = new SpringTemplateEngine();
    templateEngine.setTemplateResolver(templateResolver);

    ViteConfigurationProperties properties = new ViteConfigurationProperties(ViteConfigurationProperties.Mode.BUILD,
                                                                             new ClassPathResource("vite-manifest-example.json"), null, "static",null, null);
    ViteDevServerConfigurationProperties devServerConfigurationProperties = new ViteDevServerConfigurationProperties("localhost", 5431);

    ViteManifestReader manifestReader = new ViteManifestReader(jsonMapper, properties);
    manifestReader.init();
    ViteLinkResolver linkResolver = new ViteLinkResolver(properties, devServerConfigurationProperties, manifestReader);

    ViteDialect viteDialect = new ViteDialect(
            properties,
            devServerConfigurationProperties,
            linkResolver);
    templateEngine.addDialect(viteDialect);

    // Create and configure template engine with issue-19 manifest
    issue19TemplateEngine = new SpringTemplateEngine();
    issue19TemplateEngine.setTemplateResolver(templateResolver);

    ViteConfigurationProperties issue19Properties = new ViteConfigurationProperties(ViteConfigurationProperties.Mode.BUILD,
                                                                                    new ClassPathResource("vite-manifest-issue-19.json"), null, "src/main/javascript", null, null);

    ViteManifestReader issue19ManifestReader = new ViteManifestReader(jsonMapper, issue19Properties);
    issue19ManifestReader.init();
    ViteLinkResolver issue19LinkResolver = new ViteLinkResolver(issue19Properties, devServerConfigurationProperties, issue19ManifestReader);

    ViteDialect issue19ViteDialect = new ViteDialect(
            issue19Properties,
            devServerConfigurationProperties,
            issue19LinkResolver);
    issue19TemplateEngine.addDialect(issue19ViteDialect);
  }

  @Test
  void shouldProcessTemplate() {
    Context context = new Context();
    String result = templateEngine.process("example", context);

    assertThat(result)
            .contains("<link rel=\"stylesheet\" href=\"/assets/application-BJA3xOLB.css\">")
            .contains("<script type=\"module\" src=\"/assets/ButtonBar-8UAhfTQ4.js\"></script>")
            .contains("<script type=\"module\" src=\"/assets/client-3T5L5Tgj.js\">");
  }

  @Test
  void shouldOnlyOutputSameEntryOnce() {
    Context context = new Context();
    String result = templateEngine.process("example-many-entries", context);

    assertThat(result)
            .contains("<link rel=\"stylesheet\" href=\"/assets/application-BJA3xOLB.css\">")
            .contains("<script type=\"module\" src=\"/assets/ButtonBar-8UAhfTQ4.js\"></script>")
            .containsOnlyOnce("<script type=\"module\" src=\"/assets/client-3T5L5Tgj.js\">");
  }

  @Test
  void shouldEmitCssFromEntryCssArray() {
    // Issue 19 - CSS from manifest entry css arrays should be emitted
    // even when the CSS file does not have its own standalone manifest entry
    Context context = new Context();
    String result = issue19TemplateEngine.process("issue-19-entry-with-css", context);

    assertThat(result)
            .contains("<link rel=\"stylesheet\" href=\"/assets/app_form._C7kVfoY.css\">");
  }

  @Test
  void shouldEmitCssFromImportedChunks() {
    // Issue 19 - CSS from imported chunks should be emitted with proper / prefix
    // app-form.js imports _calendar.C4S5ojBx.min.js which has css: ["assets/calendar.fn7WE02H.css"]
    Context context = new Context();
    String result = issue19TemplateEngine.process("issue-19-entry-with-css", context);

    assertThat(result)
            .contains("<link rel=\"stylesheet\" href=\"/assets/calendar.fn7WE02H.css\">");
  }

  @Test
  void shouldEmitCssFromDirectEntryCssArray() {
    // Issue 19 - common.js has css: ["assets/common.TBP_tahU.css"]
    // This CSS should be emitted even though it has no standalone manifest entry
    Context context = new Context();
    String result = issue19TemplateEngine.process("issue-19-entry-with-imported-css", context);

    assertThat(result)
            .contains("<link rel=\"stylesheet\" href=\"/assets/common.TBP_tahU.css\">");
  }
}