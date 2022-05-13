package com.ferblaca.openrewrite.recipes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openrewrite.Result;
import org.openrewrite.SourceFile;
import org.openrewrite.java.marker.JavaProject;
import org.openrewrite.maven.MavenParser;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.yaml.YamlParser;
import org.openrewrite.yaml.tree.Yaml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

class AddJasyptDependencyMavenTest {

    private static String EMPTY_APP_MAVEN_POM =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                    "    <modelVersion>4.0.0</modelVersion>\n" +
                    "    <parent>\n" +
                    "        <groupId>org.springframework.boot</groupId>\n" +
                    "        <artifactId>spring-boot-starter-parent</artifactId>\n" +
                    "        <version>2.6.5</version>\n" +
                    "        <relativePath/>\n" +
                    "    </parent>\n" +
                    "    <groupId>com.example.openrewrite.test</groupId>\n" +
                    "    <artifactId>demoOpenrewrite</artifactId>\n" +
                    "    <version>0.0.1-SNAPSHOT</version>\n" +
                    "    <name>demoOpenRewrite</name>\n" +
                    "    <properties>\n" +
                    "        <java.version>11</java.version>\n" +
                    "    </properties>\n" +
                    "    <dependencies/>\n" +
                    "</project>";

    static final String YML_JASYPT_WITH_ENC_VALUE_FILE = "app:\n"
            + "  common:\n"
            + "    jasypt:\n"
            + "      enabled: true\n"
            + "      pass: pass\n"
            + "      test:\n"
            + "        password: ENC(CQiimDOpYhCBXZFC6DfCDNhRlewoKRY4)";

    private final JavaProject javaProject = new JavaProject(UUID.randomUUID(), "myproject", null);

    @Test
    void modified_pom_adding_jackson_dependency_When_almost_once_value_yml_property_starts_with_ENC() {

        final List<Yaml.Documents> ymlList = new YamlParserCustom("application-test.yml").parse(YML_JASYPT_WITH_ENC_VALUE_FILE);
        final List<Xml.Document> mavenList = MavenParser.builder().build().parse(EMPTY_APP_MAVEN_POM);

        // Source List (Yml and Maven)
        final List<SourceFile> sources = new ArrayList<>();

        ymlList.stream().forEach(documents -> sources.add(documents));
        mavenList.stream().forEach(document -> {
            sources.add(document.withMarkers(document.getMarkers().addIfAbsent(this.javaProject)));
        });

        // Execute the Recipe
        final AddJasyptDependencyMaven migrateJasyptStarter = new AddJasyptDependencyMaven();
        final List<Result> results = migrateJasyptStarter.run(sources);

        final String after = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <parent>\n" +
                "        <groupId>org.springframework.boot</groupId>\n" +
                "        <artifactId>spring-boot-starter-parent</artifactId>\n" +
                "        <version>2.6.5</version>\n" +
                "        <relativePath/> <!-- lookup parent from repository -->\n" +
                "    </parent>\n" +
                "    <groupId>com.example.openrewrite.test</groupId>\n" +
                "    <artifactId>demoOpenrewrite</artifactId>\n" +
                "    <version>0.0.1-SNAPSHOT</version>\n" +
                "    <name>demoOpenRewrite</name>\n" +
                "    <properties>\n" +
                "        <java.version>11</java.version>\n" +
                "    </properties>\n" +
                "    <dependency>\n" +
                "        <groupId>org.jasypt</groupId>\n" +
                "        <artifactId>jasypt</artifactId>\n" +
                "        <version>1.9.3</version>\n" +
                "    </dependency>\n" +
                "</project>";

        // Recipe Results assertions
        Assertions.assertTrue(results.size() == 1);
        Assertions.assertEquals(after, results.get(0).getAfter().printAll());
    }

    static class YamlParserCustom extends YamlParser {

        private final String yamlFileName;

        public YamlParserCustom(final String yamlFileName) {
            this.yamlFileName = yamlFileName;
        }

        @Override
        public Path sourcePathFromSourceText(final Path prefix, final String sourceCode) {
            if (Objects.isNull(this.yamlFileName)) {
                return super.sourcePathFromSourceText(prefix, sourceCode);
            } else {
                return prefix.resolve(this.yamlFileName);
            }
        }
    }

}