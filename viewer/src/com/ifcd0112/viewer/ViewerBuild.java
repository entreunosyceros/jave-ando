package com.ifcd0112.viewer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Recompila el visor sin bash/find (útil en Windows: {@code java -cp out com.ifcd0112.viewer.ViewerBuild}).
 */
public final class ViewerBuild {

    public static void main(String[] args) throws Exception {
        Path viewer = Paths.get("").toAbsolutePath().normalize();
        if (!Files.isDirectory(viewer.resolve("src"))) {
            Path fromCode = PlatformSupport.findProjectRoot().resolve("viewer");
            if (Files.isDirectory(fromCode.resolve("src"))) {
                viewer = fromCode;
            }
        }

        PlatformSupport.writeSourcesFile(viewer);

        List<String> cmd = new ArrayList<>();
        cmd.add(PlatformSupport.resolveJdkTool("javac"));
        cmd.add("-encoding");
        cmd.add("UTF-8");
        cmd.add("-d");
        cmd.add("out");
        cmd.addAll(PlatformSupport.listViewerSources(viewer));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(viewer.toFile());
        pb.inheritIO();
        int code = pb.start().waitFor();
        System.exit(code);
    }
}
