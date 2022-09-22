package com.ferblaca.openrewrite.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.maven.ChangeParentPom;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class RewriteRecipeMavenTest implements RewriteTest {

  @Override
  public void defaults(RecipeSpec spec) {
    spec.recipe(new ChangeParentPom("com.group.foo", "com.group.foo.new", "parent-foo", "parent-foo-new", "1.0.1", null, false));
  }

  @Test
  void recipeMavenTest() {
    rewriteRun(org.openrewrite.maven.Assertions.pomXml(
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
              <modelVersion>4.0.0</modelVersion>
               <parent>
               <groupId>com.group.foo</groupId>
               <artifactId>parent-foo</artifactId>
               <version>1.0.0</version>
             <relativePath />
             </parent>
            </project>
            """, """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
              <modelVersion>4.0.0</modelVersion>
               <parent>
               <groupId>com.group.foo.new</groupId>
               <artifactId>parent-foo-new</artifactId>
               <version>1.0.1</version>
             <relativePath />
             </parent>
            </project>
            """));
  }
}
