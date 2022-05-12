package com.ferblaca.openrewrite.utils;

import org.openrewrite.SourceFile;
import org.openrewrite.maven.tree.MavenResolutionResult;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.yaml.tree.Yaml;

public class OpenRewriteUtils {

    public OpenRewriteUtils() {}

    public static boolean isMavenSource(final SourceFile s) {
        return s instanceof Xml.Document && s.getMarkers().findFirst(MavenResolutionResult.class).isPresent();
    }

    public static MavenResolutionResult getMavenModel(final SourceFile s) {
        return s.getMarkers().findFirst(MavenResolutionResult.class)
                .orElseThrow(() -> new IllegalStateException("Source file does not have a maven model."));
    }

    public static boolean isYamlSource(final SourceFile s) {
        return s instanceof Yaml.Documents;
    }

}
