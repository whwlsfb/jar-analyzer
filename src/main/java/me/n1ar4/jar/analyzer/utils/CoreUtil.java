package me.n1ar4.jar.analyzer.utils;

import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoreUtil {
    private static final Logger logger = LogManager.getLogger();

    public static List<ClassFileEntity> getAllClassesFromJars(List<String> jarPathList) {
        logger.info("collect all class");
        Set<ClassFileEntity> classFileSet = new HashSet<>();
        Path temp = Paths.get("jar-analyzer-temp");
        try {
            Files.delete(temp);
        } catch (Exception ignored) {
        }
        try {
            Files.createDirectory(temp);
        } catch (IOException ignored) {
        }
        for (String jarPath : jarPathList) {
            classFileSet.addAll(JarUtil.resolveNormalJarFile(jarPath));
        }
        return new ArrayList<>(classFileSet);
    }
}
