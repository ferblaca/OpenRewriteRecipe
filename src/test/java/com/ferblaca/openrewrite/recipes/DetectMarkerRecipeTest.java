package com.ferblaca.openrewrite.recipes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openrewrite.Result;
import org.openrewrite.SourceFile;
import org.openrewrite.java.marker.JavaProject;
import org.openrewrite.maven.MavenParser;
import org.openrewrite.quark.Quark;
import org.openrewrite.quark.QuarkParser;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class DetectMarkerRecipeTest {

    private final JavaProject javaProject = new JavaProject(UUID.randomUUID(), "myproject", null);

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

    @Test
    void modified_pom_adding_maven_property_When_detect_a_file() {

        List<Quark> quarkList = new QuarkParser().parse("hi");
        final List<Xml.Document> mavenList = MavenParser.builder().build().parse(EMPTY_APP_MAVEN_POM);

        // Source List (Yml and Maven)
        final List<SourceFile> sources = new ArrayList<>();

        quarkList.stream().forEach(documents -> sources.add(documents));
        mavenList.stream().forEach(document -> {
            sources.add(document.withMarkers(document.getMarkers().addIfAbsent(this.javaProject)));
        });

        // Execute the Recipe
        final DetectMarkerRecipe detectMarkerRecipe = new DetectMarkerRecipe("file", "maven-property");
        final List<Result> results = detectMarkerRecipe.run(sources);

        final String after = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
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
                "        <maven-property>to fill!</maven-property>\n" +
                "    </properties>\n" +
                "    <dependencies/>\n" +
                "</project>";

        // Recipe Results assertions
        Assertions.assertTrue(results.size() == 1);
        Assertions.assertEquals(after, results.get(0).getAfter().printAll());
    }


}