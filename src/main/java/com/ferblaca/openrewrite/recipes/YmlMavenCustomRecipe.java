package com.ferblaca.openrewrite.recipes;

import com.ferblaca.openrewrite.utils.OpenRewriteUtils;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.search.FindDependency;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.yaml.MergeYaml;
import org.openrewrite.yaml.search.FindProperty;
import org.openrewrite.yaml.tree.Yaml;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class YmlMavenCustomRecipe extends Recipe {

    @Option(displayName = "GroupIdFind",
            description = "The groupId of the maven to find",
            example = "com.fasterxml.jackson.core")
    String groupIdFind;

    @Option(displayName = "ArtifactIdFind",
            description = "The artifactId of the maven to find",
            example = "jackson-databind")
    String artifactIdFind;

    @Option(displayName = "yamlKeyFind",
            description = "The key of the yaml property to find",
            example = "foo.key")
    String yamlKeyFind;

    @Option(displayName = "newYamlKey",
            description = "The new key of yaml property",
            example = "foo.bar.new")
    String newYamlKey;

    public YmlMavenCustomRecipe(String groupIdFind, String artifactIdFind, String yamlKeyFind, String newYamlKey) {
        this.groupIdFind = groupIdFind;
        this.artifactIdFind = artifactIdFind;
        this.yamlKeyFind = yamlKeyFind;
        this.newYamlKey = newYamlKey;
    }

    @Override
    public String getDisplayName() {
        return "Something with Maven and something with YAML";
    }

    @Override
    protected List<SourceFile> visit(final List<SourceFile> before, final ExecutionContext ctx) {
        final AtomicBoolean mavenDatagridEmbeddedFound = new AtomicBoolean(false);
        final AtomicBoolean yamlConfigPropertyNotFound = new AtomicBoolean(true);

        ListUtils.map(before, (integer, sourceFile) -> {
            if (OpenRewriteUtils.isMavenSource(sourceFile)) {
                final Xml.Document mavenDocument = (Xml.Document) sourceFile;
                // finding any maven dependency...
                final Set<Xml.Tag> tags =
                        FindDependency.find(mavenDocument, groupIdFind, artifactIdFind);
                if (!tags.isEmpty()) {
                    mavenDatagridEmbeddedFound.set(true);
                }
            } else if (OpenRewriteUtils.isYamlSource(sourceFile)) {
                // Find property into Yml source
                final Set<Yaml.Block> blocks = FindProperty.find((Yaml.Documents) sourceFile, yamlKeyFind, true);
                if (!blocks.isEmpty()) {
                    yamlConfigPropertyNotFound.set(false);
                }
            }
            return sourceFile;
        });

        // If conditions meets
        if (mavenDatagridEmbeddedFound.get() && yamlConfigPropertyNotFound.get()) {
            // add property to application.yml
            this.doNext(new MergeYaml("$", newYamlKey, true, "**/application.yml"));
        }
        return super.visit(before, ctx);
    }
}
