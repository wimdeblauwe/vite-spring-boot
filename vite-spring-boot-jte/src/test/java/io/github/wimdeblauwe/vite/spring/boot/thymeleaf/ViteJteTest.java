package io.github.wimdeblauwe.vite.spring.boot.thymeleaf;

import static io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties.Mode.BUILD;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import io.github.wimdeblauwe.vite.spring.boot.ViteConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteDevServerConfigurationProperties;
import io.github.wimdeblauwe.vite.spring.boot.ViteLinkResolver;
import io.github.wimdeblauwe.vite.spring.boot.ViteManifestReader;
import io.github.wimdeblauwe.vite.spring.boot.jte.ViteJte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ViteJteTest {

    private TemplateEngine templateEngine;

    @BeforeEach
    void setUp() throws Exception {
        // Configure JTE
        CodeResolver codeResolver = new DirectoryCodeResolver(Path.of("src/test/jte"));

        // Create and configure the template engine
        templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);

        ViteConfigurationProperties properties = new ViteConfigurationProperties(BUILD,
            new ClassPathResource("vite-manifest-example.json"), null, null, null);
        ViteDevServerConfigurationProperties devServerConfigurationProperties = new ViteDevServerConfigurationProperties("localhost", 5431);

        ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ViteManifestReader manifestReader = new ViteManifestReader(objectMapper, properties);
        manifestReader.init();
        ViteLinkResolver linkResolver = new ViteLinkResolver(properties, devServerConfigurationProperties, manifestReader);

        ViteJte.init(linkResolver, properties, devServerConfigurationProperties);
    }

    @Test
    void shouldProcessTemplate() {
        TemplateOutput output = new StringOutput();
        templateEngine.render("example.jte", Map.of(), output);
        String result = output.toString();

        assertThat(result)
            .contains("<link rel=\"stylesheet\" href=\"/assets/application-BJA3xOLB.css\"/>")
            .contains("<script type=\"module\" src=\"/assets/ButtonBar-8UAhfTQ4.js\"></script>")
            .contains("<script type=\"module\" src=\"/assets/client-3T5L5Tgj.js\">");
    }

    @Test
    void shouldOnlyOutputSameEntryOnce() {
        TemplateOutput output = new StringOutput();
        templateEngine.render("example-many-entries.jte", Map.of(), output);
        String result = output.toString();

        assertThat(result)
            .contains("<link rel=\"stylesheet\" href=\"/assets/application-BJA3xOLB.css\"/>")
            .contains("<script type=\"module\" src=\"/assets/ButtonBar-8UAhfTQ4.js\"></script>")
            .containsOnlyOnce("<script type=\"module\" src=\"/assets/client-3T5L5Tgj.js\">");
    }
}