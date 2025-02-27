package me.n1ar4.jar.analyzer.engine;

import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Decompile Engine
 */
public class DecompileEngine {
    public static final String INFO = "<html>" +
            "<b>FernFlower</b> - A great plugin from <b>JetBrains intellij-community</b>" +
            "</html>";
    private static final Logger logger = LogManager.getLogger();
    private static final String JAVA_DIR = "jar-analyzer-decompile";
    private static final String JAVA_FILE = ".java";
    private static final String FERN_PREFIX = "//\n" +
            "// Jar Analyzer V2 by 4ra1n\n" +
            "// (powered by FernFlower decompiler)\n" +
            "//\n";

    /**
     * Decompile Any Class
     *
     * @param classFilePath Class File Path
     * @return Java Source Code
     */
    public static String decompile(Path classFilePath) {
        try {
            boolean fern = MainForm.getInstance().getFernRadio().isSelected();
            if (fern) {
                Path dirPath = Paths.get(Const.tempDir);
                Path deDirPath = dirPath.resolve(Paths.get(JAVA_DIR));
                if (!Files.exists(deDirPath)) {
                    Files.createDirectory(deDirPath);
                }
                String javaDir = deDirPath.toAbsolutePath().toString();
                String fileName = classFilePath.getFileName().toString();
                String[] split = fileName.split("\\.");
                if (split.length < 2) {
                    throw new RuntimeException("decompile error");
                }
                String newFileName = split[0] + JAVA_FILE;
                Path newFilePath = deDirPath.resolve(Paths.get(newFileName));
                // TRY DELETE CACHE
                try {
                    Files.delete(newFilePath);
                } catch (Exception ignored) {
                }

                // RESOLVE $ CLASS
                List<String> extraClassList = new ArrayList<>();
                Path classDirPath = classFilePath.getParent();
                String classNamePrefix = classFilePath.getFileName().toString();
                classNamePrefix = classNamePrefix.split("\\.")[0];

                String finalClassNamePrefix = classNamePrefix;
                Files.walkFileTree(classDirPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        String fileName = file.getFileName().toString();
                        if (fileName.startsWith(finalClassNamePrefix + "$")) {
                            extraClassList.add(file.toAbsolutePath().toString());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });

                List<String> cmd = new ArrayList<>();
                cmd.add(classFilePath.toAbsolutePath().toString());
                cmd.addAll(extraClassList);
                cmd.add(javaDir);

                LogUtil.log("decompile class: " + classFilePath.getFileName().toString());

                // FERN FLOWER API
                ConsoleDecompiler.main(cmd.toArray(new String[0]));
                byte[] code = Files.readAllBytes(newFilePath);
                String codeStr = new String(code);
                codeStr = FERN_PREFIX + codeStr;
                // TRY DELETE CACHE
                try {
                    Files.delete(newFilePath);
                } catch (Exception ignored) {
                }
                return codeStr;
            } else {
                LogUtil.log("unknown error");
                return null;
            }
        } catch (Exception ex) {
            logger.error("decompile error: {}", ex.toString());
        }
        return null;
    }
}
