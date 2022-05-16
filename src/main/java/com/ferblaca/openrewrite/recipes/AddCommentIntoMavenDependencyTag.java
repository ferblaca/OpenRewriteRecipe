package com.ferblaca.openrewrite.recipes;

import static org.openrewrite.Tree.randomId;

import java.util.Collections;
import java.util.Optional;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.marker.Markers;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.xml.ChangeTagContentVisitor;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.xml.tree.Xml.Tag;

public class AddCommentIntoMavenDependencyTag extends Recipe {

  @Override
  public String getDisplayName() {
    return "Add comment into maven source";
  }

  @Override
  protected TreeVisitor<?, ExecutionContext> getVisitor() {
    return new MavenIsoVisitor<ExecutionContext>() {
      @Override
      public Xml.Tag visitTag(final Xml.Tag tag, final ExecutionContext ctx) {
        if (this.isDependencyTag("org.jasypt", "jasypt")) {
          final Optional<Tag> scopeTag = tag.getChild("scope");
          final String scope = scopeTag.isPresent() && scopeTag.get().getValue().isPresent() ? scopeTag.get().getValue().get() : null;

          // xml comment
          final Xml.Comment customComment = new Xml.Comment(randomId(),
              tag.getPrefix(),
              Markers.EMPTY,
              "custom comment into dependency!!!");

          // Change Content of dependency Tag
          this.doAfterVisit(new ChangeTagContentVisitor<>(tag, Collections.singletonList(customComment)));
        }
        return super.visitTag(tag, ctx);
      }
    };
  }

}
