package io.github.wimdeblauwe.vite.spring.boot;


import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

import tools.jackson.databind.json.JsonMapper;

@JsonTest
class ViteLinkResolverTest {

  @Autowired
  private JsonMapper jsonMapper;

  @Nested
  class ResolveResourceTests {
    @Test
    void testResolveResourceInDevMode() throws IOException {
      ViteLinkResolver resolver = createLinkResolver(ViteConfigurationProperties.Mode.DEV);
      Optional<String> resource = resolver.resolveResource("css/application.css");
      assertThat(resource).hasValueSatisfying( it -> assertThat(it).isEqualTo("//localhost:5173/static/css/application.css"));
    }

    @Test
    void testResolveResourceInBuildMode() throws IOException {
      ViteLinkResolver resolver = createLinkResolver(ViteConfigurationProperties.Mode.BUILD);
      Optional<String> resource = resolver.resolveResource("css/application.css");
      assertThat(resource).hasValueSatisfying( it -> assertThat(it).isEqualTo("/assets/application-BJA3xOLB.css"));
    }

    @Test
    void testResolveResourceInBuildModeWithSlashPrefix() throws IOException {
      ViteLinkResolver resolver = createLinkResolver(ViteConfigurationProperties.Mode.BUILD);
      Optional<String> resource = resolver.resolveResource("/css/application.css");
      assertThat(resource).hasValueSatisfying( it -> assertThat(it).isEqualTo("/assets/application-BJA3xOLB.css"));
    }
  }

  @Nested
  class ResolveResourceWithCustomContextPathTests {
    @Test
    void testResolveResourceInDevMode() throws IOException {
      ViteLinkResolver resolver = createLinkResolverWithCustomContextPaths(ViteConfigurationProperties.Mode.DEV);
      Optional<String> resource = resolver.resolveResource("css/application.css");
      assertThat(resource).hasValueSatisfying( it -> assertThat(it).isEqualTo("//localhost:5173/dev-context/static/css/application.css"));
    }

    @Test
    void testResolveResourceInBuildMode() throws IOException {
      ViteLinkResolver resolver = createLinkResolverWithCustomContextPaths(ViteConfigurationProperties.Mode.BUILD);
      Optional<String> resource = resolver.resolveResource("css/application.css");
      assertThat(resource).hasValueSatisfying( it -> assertThat(it).isEqualTo("/build-context/assets/application-BJA3xOLB.css"));
    }
  }

  @Nested
  class GetManifestEntryTests {
    @Test
    void testDirectMatch() throws IOException {
      ViteLinkResolver resolver = createLinkResolver(ViteConfigurationProperties.Mode.BUILD);
      ViteManifestReader.ManifestEntry entry = resolver.getManifestEntry("css/application.css");
      assertThat(entry.file()).isEqualTo("assets/application-BJA3xOLB.css");
    }

    @Test
    void testIndirectMatch() throws IOException {
      ViteLinkResolver resolver = createLinkResolver(ViteConfigurationProperties.Mode.BUILD);
      ViteManifestReader.ManifestEntry entry = resolver.getManifestEntry("_client-3T5L5Tgj.js");
      assertThat(entry.file()).isEqualTo("assets/client-3T5L5Tgj.js");
    }
  }

  private ViteLinkResolver createLinkResolver(ViteConfigurationProperties.Mode mode) throws IOException {
    ViteConfigurationProperties properties = new ViteConfigurationProperties(mode, new ClassPathResource("io/github/wimdeblauwe/vite/spring/boot/vite-manifest-example.json"), null, "static", null, null);
    ViteManifestReader manifestReader = new ViteManifestReader(jsonMapper, properties);
    manifestReader.init();
    return new ViteLinkResolver(properties,
            new ViteDevServerConfigurationProperties("localhost", 5173),
            manifestReader);
  }

  private ViteLinkResolver createLinkResolverWithCustomContextPaths(ViteConfigurationProperties.Mode mode) throws IOException {
    ViteConfigurationProperties properties = new ViteConfigurationProperties(mode,
                                                                             new ClassPathResource("io/github/wimdeblauwe/vite/spring/boot/vite-manifest-example.json"),
                                                                             null,
                                                                             "static",
                                                                             "/build-context",
                                                                             "/dev-context");
    ViteManifestReader manifestReader = new ViteManifestReader(jsonMapper, properties);
    manifestReader.init();
    return new ViteLinkResolver(properties,
            new ViteDevServerConfigurationProperties("localhost", 5173),
            manifestReader);
  }
}