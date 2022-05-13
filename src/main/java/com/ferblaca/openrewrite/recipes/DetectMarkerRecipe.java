package com.ferblaca.openrewrite.recipes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ferblaca.openrewrite.utils.OpenRewriteUtils;
import org.openrewrite.*;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.ChangePropertyValue;
import org.openrewrite.quark.Quark;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DetectMarkerRecipe extends Recipe {

    @Option(displayName = "markerFileName", description = "the name of marker file",
            example = "file.marker")
    String markerFileName;

    @Option(displayName = "mavenPropertyName", description = "the name of maven property",
            example = "maven.property")
    String mavenPropertyName;

    public DetectMarkerRecipe(@NonNull @JsonProperty("markerFileName") String markerFileName,
                              @NonNull @JsonProperty("mavenPropertyName") String mavenPropertyName) {
        this.markerFileName = markerFileName;
        this.mavenPropertyName = mavenPropertyName;
    }

    @Override
    public String getDisplayName() {
        return "Detect Marker on project and add maven property";
    }

    @Override
    protected List<SourceFile> visit(final List<SourceFile> before, final ExecutionContext ctx) {
        final AtomicBoolean amigaWsApiFirstClientMarkerFound = new AtomicBoolean(false);

        ListUtils.map(before, (integer, sourceFile) -> {
            new TreeVisitor<Tree, ExecutionContext>() {
                @Override
                public @Nullable Tree visitSourceFile(SourceFile sourceFile, ExecutionContext executionContext) {
                    if (OpenRewriteUtils.isQuarkSource(sourceFile)) {
                        Quark quark = (Quark) sourceFile;
                        System.out.println("############ Quark found: " + quark.getSourcePath());
                    }

                    if (sourceFile.getSourcePath().endsWith(markerFileName)) {
                        amigaWsApiFirstClientMarkerFound.set(true);
                    }
                    return sourceFile;
                }

//                @Nullable
//                @Override
//                public Tree visit(@Nullable final Tree tree, final ExecutionContext executionContext) {
//                    if (tree instanceof SourceFile) {
//                        final SourceFile sourceFile = (SourceFile) tree;
//                        System.out.println("############ sourceFile: " + sourceFile.getSourcePath());
//                        if (sourceFile.getSourcePath().endsWith(markerFileName)) {
//                            amigaWsApiFirstClientMarkerFound.set(true);
//                        }
//                    }
//                    return tree;
//                }

            }.visit(before, ctx);
            return sourceFile;
        });

        // If marker is found
        if (amigaWsApiFirstClientMarkerFound.get()) {
            // add maven property
            this.doNext(new ChangePropertyValue(mavenPropertyName, "to fill!", true));
        }

        return super.visit(before, ctx);
    }
}
