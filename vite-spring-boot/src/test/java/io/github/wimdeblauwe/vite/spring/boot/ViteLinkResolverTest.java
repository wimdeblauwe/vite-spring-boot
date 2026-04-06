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

  @Nested
  class Issue19Tests {
    @Test
    void testGetManifestEntryWithCssArray() throws IOException {
      ViteLinkResolver resolver = createLinkResolverForIssue19();
      ViteManifestReader.ManifestEntry entry = resolver.getManifestEntry("bundles/app-form.js");
      assertThat(entry).isNotNull();
      assertThat(entry.css()).containsExactly("assets/app_form._C7kVfoY.css");
    }

    @Test
    void testResolveBuiltAssetPath() throws IOException {
      ViteLinkResolver resolver = createLinkResolverForIssue19();
      // CSS paths from manifest css arrays are already built paths
      // They should be resolvable without going through the manifest again
      String resolved = resolver.resolveBuiltAssetPath("assets/app_form._C7kVfoY.css");
      assertThat(resolved).isEqualTo("/assets/app_form._C7kVfoY.css");
    }

    @Test
    void testResolveBuiltAssetPathWithContextPath() throws IOException {
      ViteConfigurationProperties properties = new ViteConfigurationProperties(
              ViteConfigurationProperties.Mode.BUILD,
              new ClassPathResource("io/github/wimdeblauwe/vite/spring/boot/vite-manifest-issue-19.json"),
              null, "src/main/javascript", "/build-context", null);
      ViteManifestReader manifestReader = new ViteManifestReader(jsonMapper, properties);
      manifestReader.init();
      ViteLinkResolver resolver = new ViteLinkResolver(properties,
              new ViteDevServerConfigurationProperties("localhost", 5173),
              manifestReader);

      String resolved = resolver.resolveBuiltAssetPath("assets/app_form._C7kVfoY.css");
      assertThat(resolved).isEqualTo("/build-context/assets/app_form._C7kVfoY.css");
    }
  }

  private ViteLinkResolver createLinkResolver(ViteConfigurationProperties.Mode mode) throws IOException {
    return createLinkResolver(mode, new ClassPathResource("io/github/wimdeblauwe/vite/spring/boot/vite-manifest-example.json"));
  }

  private ViteLinkResolver createLinkResolver(ViteConfigurationProperties.Mode mode,
                                              ClassPathResource manifestResource) throws IOException {
    return createLinkResolver(mode, manifestResource, "static");
  }

  private ViteLinkResolver createLinkResolver(ViteConfigurationProperties.Mode mode,
                                              ClassPathResource manifestResource,
                                              String prefix) throws IOException {
    ViteConfigurationProperties properties = new ViteConfigurationProperties(mode, manifestResource, null, prefix, null, null);
    ViteManifestReader manifestReader = new ViteManifestReader(jsonMapper, properties);
    manifestReader.init();
    return new ViteLinkResolver(properties,
            new ViteDevServerConfigurationProperties("localhost", 5173),
            manifestReader);
  }

  private ViteLinkResolver createLinkResolverForIssue19() throws IOException {
    return createLinkResolver(ViteConfigurationProperties.Mode.BUILD,
            new ClassPathResource("io/github/wimdeblauwe/vite/spring/boot/vite-manifest-issue-19.json"),
            "src/main/javascript");
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