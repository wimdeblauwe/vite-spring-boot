package io.github.wimdeblauwe.vite.spring.boot;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import tools.jackson.databind.json.JsonMapper;

@JsonTest
class ViteManifestReaderTest {

  @Autowired
  private JsonMapper jsonMapper;

  @Test
  void givenDevMode_doNothing() throws IOException {
    ViteManifestReader reader = new ViteManifestReader(jsonMapper, new ViteConfigurationProperties(ViteConfigurationProperties.Mode.DEV,
                                                                                                   new ClassPathResource("does-not-exist"), null, "static", null, null));
    assertThatNoException()
            .isThrownBy(reader::init);
  }

  @Test
  void givenBuildModeAndCouldNotFindManifest_doNothing() throws IOException {
    ViteManifestReader reader = new ViteManifestReader(jsonMapper, new ViteConfigurationProperties(ViteConfigurationProperties.Mode.BUILD,
                                                                                                   new ClassPathResource("does-not-exist"), null, "static", null, null));
    assertThatNoException()
            .isThrownBy(reader::init);
  }

  @Nested
  class HappyFlowTests {

    private ViteManifestReader reader;

    @BeforeEach
    void setUp() throws IOException {
      reader = new ViteManifestReader(jsonMapper, new ViteConfigurationProperties(ViteConfigurationProperties.Mode.BUILD,
                                                                                  new ClassPathResource("io/github/wimdeblauwe/vite/spring/boot/vite-manifest-example.json"),
                                                                                  null, "static", null, null));

      reader.init();
    }

    @Test
    void getBundledPath() {
      assertThat(reader.getBundledPath("static/css/application.css"))
              .isEqualTo("assets/application-BJA3xOLB.css");
    }

    @Test
    void getManifestEntry() {
      assertThat(reader.getManifestEntry("static/css/application.css"))
              .satisfies(entry -> {
                assertThat(entry.isEntry()).isTrue();
                assertThat(entry.file()).isEqualTo("assets/application-BJA3xOLB.css");
                assertThat(entry.src()).isEqualTo("static/css/application.css");
                assertThat(entry.imports()).isEmpty();
                assertThat(entry.css()).isEmpty();
              });
    }

    @Test
    void getManifestEntryWithImports() {
      assertThat(reader.getManifestEntry("static/react/ListExample.tsx"))
              .satisfies(entry -> {
                assertThat(entry.isEntry()).isTrue();
                assertThat(entry.file()).isEqualTo("assets/ListExample-BE9sf6Vz.js");
                assertThat(entry.src()).isEqualTo("static/react/ListExample.tsx");
                assertThat(entry.imports()).containsExactly("_client-3T5L5Tgj.js");
                assertThat(entry.css()).isEmpty();
              });
    }
  }
}