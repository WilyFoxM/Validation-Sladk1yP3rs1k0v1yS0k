package ru.wilyfox.client.protocol;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class DwHandshakeFingerprint {
    private static final HexFormat HEX = HexFormat.of();

    private DwHandshakeFingerprint() {
    }

    static String generate() {
        List<String> parts = new ArrayList<>();
        addSystemProperties(parts);
        addMemoryInfo(parts);
        addFileStores(parts);
        addEnvironment(parts);

        parts.removeIf(part -> part == null || part.isBlank());
        parts.sort(String::compareTo);

        return sha256Hex(String.join("|", parts));
    }

    private static void addSystemProperties(List<String> parts) {
        addProperty(parts, "cpu.arch", "os.arch", false);
        addProperty(parts, "os.arch", "os.arch", true);
        addProperty(parts, "os.name", "os.name", false);
        addProperty(parts, "os.version", "os.version", false);
        addProperty(parts, "user.home", "user.home", false);
        addProperty(parts, "user.name", "user.name", false);

        int processors = Runtime.getRuntime().availableProcessors();
        parts.add("cpu.count=" + processors);
        parts.add("cpu.cores=" + processors);
    }

    private static void addMemoryInfo(List<String> parts) {
        try {
            OperatingSystemMXBean operatingSystemMxBean = ManagementFactory.getOperatingSystemMXBean();
            Method method = operatingSystemMxBean.getClass().getMethod("getTotalMemorySize");
            method.setAccessible(true);
            Object value = method.invoke(operatingSystemMxBean);
            if (value instanceof Long totalMemory) {
                parts.add("mem.total=" + totalMemory);
            }
        } catch (Exception ignored) {
        }
    }

    private static void addFileStores(List<String> parts) {
        List<FileStore> fileStores = new ArrayList<>();
        for (FileStore fileStore : FileSystems.getDefault().getFileStores()) {
            fileStores.add(fileStore);
        }

        fileStores.sort(Comparator.comparing(fileStore -> safeFileStoreName(fileStore).toLowerCase(Locale.ROOT)));

        for (FileStore fileStore : fileStores) {
            try {
                parts.add("fs:" + safeFileStoreName(fileStore) + "=" + fileStore.getTotalSpace());
            } catch (Exception ignored) {
            }
        }
    }

    private static void addEnvironment(List<String> parts) {
        Map<String, String> environment = System.getenv();
        addEnvironment(parts, "computername", environment.get("COMPUTERNAME"));
        addEnvironment(parts, "cpu.id", environment.get("PROCESSOR_IDENTIFIER"));
    }

    private static void addProperty(List<String> parts, String label, String propertyKey, boolean uppercase) {
        String value = System.getProperty(propertyKey, "");
        if (value.isBlank()) {
            return;
        }

        if (uppercase) {
            value = value.toUpperCase(Locale.ROOT);
        }

        parts.add(label + "=" + value);
    }

    private static void addEnvironment(List<String> parts, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        parts.add(label + "=" + value);
    }

    private static String safeFileStoreName(FileStore fileStore) {
        try {
            String name = fileStore.name();
            return name == null ? "" : name;
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HEX.formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
