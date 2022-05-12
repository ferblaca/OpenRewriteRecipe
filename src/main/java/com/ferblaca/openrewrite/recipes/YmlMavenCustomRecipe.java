package com.ferblaca.openrewrite.recipes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ferblaca.openrewrite.utils.OpenRewriteUtils;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.maven.tree.ResolvedDependency;
import org.openrewrite.maven.tree.Scope;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.yaml.MergeYaml;
import org.openrewrite.yaml.search.FindProperty;
import org.openrewrite.yaml.tree.Yaml;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Yml maven custom recipe.
 */
public class YmlMavenCustomRecipe extends Recipe {

    /**
     * The Group id find.
     */
    @Option(displayName = "GroupIdFind",
            description = "The groupId of the maven to find",
            example = "com.fasterxml.jackson.core")
    String groupIdFind;

    /**
     * The Artifact id find.
     */
    @Option(displayName = "ArtifactIdFind",
            description = "The artifactId of the maven to find",
            example = "jackson-databind")
    String artifactIdFind;

    /**
     * The Yaml key find.
     */
    @Option(displayName = "yamlKeyFind",
            description = "The key of the yaml property to find",
            example = "foo.key")
    String yamlKeyFind;

    /**
     * The New yaml key.
     */
    @Option(displayName = "newYamlKey",
            description = "The new key of yaml property",
            example = "foo.bar.new")
    String newYamlKey;

    /**
     * Instantiates a new Yml maven custom recipe.
     *
     * @param groupIdFind    the group id find
     * @param artifactIdFind the artifact id find
     * @param yamlKeyFind    the yaml key find
     * @param newYamlKey     the new yaml key
     */
    @JsonCreator
    public YmlMavenCustomRecipe(@NonNull @JsonProperty("groupIdFind") String groupIdFind,
                                @NonNull @JsonProperty("artifactIdFind") String artifactIdFind,
                                @NonNull @JsonProperty("yamlKeyFind") String yamlKeyFind,
                                @NonNull @JsonProperty("newYamlKey") String newYamlKey) {
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
                if (!mavenDatagridEmbeddedFound.get()) {
                    findMavenDependency(ctx, mavenDatagridEmbeddedFound, sourceFile);
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

    private void findMavenDependency(ExecutionContext ctx, AtomicBoolean mavenDatagridEmbeddedFound, SourceFile sourceFile) {
        new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitDocument(Xml.Document document, ExecutionContext ctx) {
                for (ResolvedDependency resolvedDependency : getResolutionResult()
                        .getDependencies()
                        .get(Scope.fromName(Scope.Compile.name()))) {
                    if (resolvedDependency.getGav().getGroupId().equals(groupIdFind) && resolvedDependency.getArtifactId().equals(artifactIdFind)) {
                        mavenDatagridEmbeddedFound.set(true);
                        break;
                    }
                }
                return super.visitDocument(document, ctx);
            }
        }.visit(sourceFile, ctx);
    }

}
