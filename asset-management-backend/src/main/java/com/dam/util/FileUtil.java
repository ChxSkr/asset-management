package com.dam.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

public class FileUtil {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt",
            "jpg", "jpeg", "png", "gif", "svg",
            "mp4", "mp3", "wav",
            "zip"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public static boolean isAllowedExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return false;
        }
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    public static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public static boolean isSizeValid(long size) {
        return size > 0 && size <= MAX_FILE_SIZE;
    }

    public static String generateStorageFilename(String originalFilename) {
        return UUID.randomUUID().toString().replace("-", "") + getExtension(originalFilename);
    }

    public static Path saveFile(byte[] content, String basePath, String storageFilename) throws IOException {
        Path dir = Paths.get(basePath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Path filePath = dir.resolve(storageFilename);
        Files.write(filePath, content);
        return filePath;
    }

    public static void deleteFile(String basePath, String storageFilename) {
        try {
            Path filePath = Paths.get(basePath, storageFilename);
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }

    public static byte[] readFile(String basePath, String storageFilename) throws IOException {
        Path filePath = Paths.get(basePath, storageFilename);
        return Files.readAllBytes(filePath);
    }
}