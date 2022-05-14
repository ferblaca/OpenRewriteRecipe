package com.ferblaca.openrewrite.recipes;

import com.ferblaca.openrewrite.utils.OpenRewriteUtils;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.maven.AddDependencyVisitor;
import org.openrewrite.maven.search.FindDependency;
import org.openrewrite.maven.tree.Scope;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.tree.Yaml;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AddJasyptDependencyMaven extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate Jasypt starter";
    }

    @Override
    protected List<SourceFile> visit(final List<SourceFile> before, final ExecutionContext ctx) {
        final AtomicBoolean yamlEncValueFound = new AtomicBoolean(false);

        ListUtils.map(before, (integer, sourceFile) -> {
            if (OpenRewriteUtils.isYamlSource(sourceFile)) {
                new YamlIsoVisitor<ExecutionContext>() {
                    @Override
                    public Yaml.Mapping.Entry visitMappingEntry(final Yaml.Mapping.Entry entry, final ExecutionContext context) {
                        final Yaml.Mapping.Entry e = super.visitMappingEntry(entry, context);
                        // find a value on a property that starts with 'ENC('
                        if (e.getValue() instanceof Yaml.Scalar && ((Yaml.Scalar) e.getValue()).getValue().startsWith("ENC(")) {
                            yamlEncValueFound.set(true);
                        }
                        return e;
                    }
                }.visit(sourceFile, ctx);
            }
            return sourceFile;
        });

        // If Jasypt value is found on yml file...
        if (yamlEncValueFound.get()) {
            return ListUtils.map(before, sourceFile -> {
                if (OpenRewriteUtils.isMavenSource(sourceFile) && FindDependency.find((Xml.Document) sourceFile, "org.jasypt", "jasypt").isEmpty()) {
                    // Add recipe to add the jasypt stater
                    final AddDependencyVisitor addDependencyVisitor =
                            new AddDependencyVisitor("org.jasypt", "jasypt", "1.9.3", null,
                                    "compile", false, null, null, null, null);
                    final Xml visit = addDependencyVisitor.visit(sourceFile, ctx);
                    return (SourceFile) visit;
                }
                return sourceFile;
            });
        }
        
        return before;
    }
}