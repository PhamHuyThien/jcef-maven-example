package home.thienph.utils;

import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtils {

    /**
     * Hàm chính: Tìm kiếm tất cả các class trong package (Hỗ trợ cả IDE và File JAR)
     */
    @SneakyThrows
    public static Set<Class<?>> findClasses(String basePackage) {
        Set<Class<?>> classes = new HashSet<>();
        String path = basePackage.replace('.', '/');

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) classLoader = ClassUtils.class.getClassLoader();
        if (classLoader == null) classLoader = ClassLoader.getSystemClassLoader();

        // Lấy tất cả tài nguyên khớp với đường dẫn package
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();

            if ("file".equals(protocol)) {
                // TRƯỜNG HỢP 1: Chạy trong IDE (Thư mục thông thường)
                String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
                scanDirectory(new File(filePath), basePackage, classes);

            } else if ("jar".equals(protocol)) {
                // TRƯỜNG HỢP 2: Chạy từ file JAR đóng gói
                scanJar(resource, path, classes);
            }
        }
        return classes;
    }

    /**
     * Quét đệ quy trong thư mục (Dành cho IDE)
     */
    private static void scanDirectory(File directory, String currentPackage, Set<Class<?>> classes) throws Exception {
        if (!directory.exists() || directory.listFiles() == null) return;

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                String subPackageName = currentPackage + "." + file.getName();
                scanDirectory(file, subPackageName, classes);
            } else if (file.getName().endsWith(".class")) {
                String className = currentPackage + "." + file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        }
    }

    /**
     * Quét các phần tử bên trong file JAR (Dành cho file đóng gói)
     */
    private static void scanJar(URL resource, String packagePath, Set<Class<?>> classes) throws Exception {
        // Kết nối và mở file JAR
        JarURLConnection jarURLConnection = (JarURLConnection) resource.openConnection();
        try (JarFile jar = jarURLConnection.getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                // Kiểm tra xem phần tử này có nằm trong package mục tiêu và phải là file .class không
                if (name.startsWith(packagePath) && name.endsWith(".class")) {
                    // Chuyển đổi đường dẫn file nén "home/thienph/Main.class" thành "home.thienph.Main"
                    String className = name.replace('/', '.').substring(0, name.length() - 6);
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        // Bỏ qua nếu có class lỗi cấu trúc hệ thống
                    }
                }
            }
        }
    }
}