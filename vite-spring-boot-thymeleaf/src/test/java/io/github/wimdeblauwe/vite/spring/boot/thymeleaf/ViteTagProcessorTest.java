package io.github.wimdeblauwe.vite.spring.boot.thymeleaf;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteDevServerConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteLinkResolver;
import io.github.wimdeblauwe.vite.spring.boot.ViteManifestReader;
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

  @BeforeEach
  void setUp() throws Exception {
    // Configure Thymeleaf
    ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setPrefix("/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setTemplateMode(TemplateMode.HTML);
    templateResolver.setCharacterEncoding("UTF-8");

    // Create and configure template engine
    templateEngine = new SpringTemplateEngine();
    templateEngine.setTemplateResolver(templateResolver);

    ViteConfigurationProperties properties = new ViteConfigurationProperties(ViteConfigurationProperties.Mode.BUILD,
                                                                             new ClassPathResource("vite-manifest-example.json"), null, "static",null, null);
    ViteDevServerConfigurationProperties devServerConfigurationProperties = new ViteDevServerConfigurationProperties("localhost", 5431);

    ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    ViteManifestReader manifestReader = new ViteManifestReader(objectMapper, properties);
    manifestReader.init();
    ViteLinkResolver linkResolver = new ViteLinkResolver(properties, devServerConfigurationProperties, manifestReader);

    ViteDialect viteDialect = new ViteDialect(
            properties,
            devServerConfigurationProperties,
            linkResolver);
    templateEngine.addDialect(viteDialect);
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
  void shouldApplyBuildModeContextPathToImportedChunks() throws Exception {
    TemplateEngine engineWithContextPath = createTemplateEngineWithContextPath("/myapp");
    String result = engineWithContextPath.process("example", new Context());

    assertThat(result)
            // entry chunk gets the context path (already worked)
            .contains("<script type=\"module\" src=\"/myapp/assets/ButtonBar-8UAhfTQ4.js\"></script>")
            // imported chunks must also get the context path (regression for missing prefix)
            .contains("<script type=\"module\" src=\"/myapp/assets/client-3T5L5Tgj.js\">")
            .doesNotContain("<script type=\"module\" src=\"/assets/client-3T5L5Tgj.js\">");
  }

  private TemplateEngine createTemplateEngineWithContextPath(String contextPath) throws Exception {
    ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setPrefix("/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setTemplateMode(TemplateMode.HTML);
    templateResolver.setCharacterEncoding("UTF-8");

    ViteConfigurationProperties properties = new ViteConfigurationProperties(
            ViteConfigurationProperties.Mode.BUILD,
            new ClassPathResource("vite-manifest-example.json"),
            null,
            "static",
            contextPath,
            null);
    ViteDevServerConfigurationProperties devServerProps = new ViteDevServerConfigurationProperties("localhost", 5431);

    ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ViteManifestReader manifestReader = new ViteManifestReader(objectMapper, properties);
    manifestReader.init();
    ViteLinkResolver linkResolver = new ViteLinkResolver(properties, devServerProps, manifestReader);

    SpringTemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(templateResolver);
    engine.addDialect(new ViteDialect(properties, devServerProps, linkResolver));
    return engine;
  }
}