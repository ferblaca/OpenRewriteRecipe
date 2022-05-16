package com.ferblaca.openrewrite.recipes;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openrewrite.Result;
import org.openrewrite.SourceFile;
import org.openrewrite.maven.MavenParser;
import org.openrewrite.quark.Quark;
import org.openrewrite.quark.QuarkParser;
import org.openrewrite.xml.tree.Xml;

class AddCommentIntoMavenDependencyTagTest {

  private static String APP_MAVEN_POM_BEFORE =
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
          "    <dependencies>\n" +
          "        <dependency>\n" +
          "            <groupId>org.jasypt</groupId>\n" +
          "            <artifactId>jasypt</artifactId>\n" +
          "            <version>1.9.3</version>\n" +
          "        </dependency>\n" +
          "    </dependencies>\n" +
          "</project>";

  @Test
  void modified_pom_adding_custom_comment_to_dependecy() {

    List<Quark> quarkList = new QuarkParser().parse("hi");
    final List<Xml.Document> mavenList = MavenParser.builder().build().parse(APP_MAVEN_POM_BEFORE);

    // Execute the Recipe
    final AddCommentIntoMavenDependencyTag recipe = new AddCommentIntoMavenDependencyTag();
    final List<Result> results = recipe.run(mavenList);

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
        "    </properties>\n" +
        "    <dependencies>\n" +
        "        <dependency>\n" +
        "            <!--custom comment into dependency!!!-->\n" +
        "            <groupId>org.jasypt</groupId>\n" +
        "            <artifactId>jasypt</artifactId>\n" +
        "            <version>1.9.3</version>\n" +
        "        </dependency>\n" +
        "    </dependencies>\n" +
        "</project>";

    // Recipe Results assertions
    Assertions.assertTrue(results.size() == 1);
    Assertions.assertEquals(after, results.get(0).getAfter().printAll());
  }


}