package home.thienph.jcef.utils;

import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtils {

    /**
     * Quét Class bằng chuỗi Package - Tương thích IDE, File JAR và File .EXE (jlauncher, launch4j...)
     */
    @SneakyThrows
    public static Set<Class<?>> findClasses(String basePackage) {
        Set<Class<?>> classes = new HashSet<>();
        String packagePath = basePackage.replace('.', '/') + "/"; // "home/thienph/"

        // Sử dụng LinkedHashSet để giữ thứ tự ưu tiên và tự động loại bỏ các đường dẫn trùng lặp
        Set<File> rootsToScan = new LinkedHashSet<>();

        // HƯỚNG 1: Định vị dựa theo vị trí của chính lớp ClassUtils này
        try {
            URL codeSourceUrl = ClassUtils.class.getProtectionDomain().getCodeSource().getLocation();
            if (codeSourceUrl != null) {
                String codeSourcePath = Paths.get(codeSourceUrl.toURI()).toAbsolutePath().toString();
                rootsToScan.add(new File(codeSourcePath));
            }
        } catch (Exception ignored) {
            // Bỏ qua nếu môi trường native chặn lấy CodeSource
        }

        // HƯỚNG 2: Vét cạn toàn bộ các đường dẫn trong Classpath hệ thống (Cực kỳ quan trọng đối với các bộ launcher EXE)
        String classpath = System.getProperty("java.class.path");
        if (classpath != null && !classpath.isEmpty()) {
            String[] pathElements = classpath.split(File.pathSeparator);
            for (String element : pathElements) {
                rootsToScan.add(new File(element).getAbsoluteFile());
            }
        }

        // Bắt đầu quét qua tất cả các vị trí tài nguyên tìm được
        for (File root : rootsToScan) {
            if (!root.exists()) continue;

            if (root.isDirectory()) {
                // TRƯỜNG HỢP 1: Chạy trong IDE (Có thư mục target/classes vật lý)
                File packageDir = new File(root, packagePath);
                scanDirectory(packageDir, basePackage, classes);
            } else if (root.isFile()) {
                // TRƯỜNG HỢP 2: Chạy từ file JAR hoặc file .EXE đã đóng gói
                // Loại bỏ điều kiện check đuôi ".jar", cố gắng mở bằng JarFile để đọc cấu trúc nén bên trong
                try (JarFile jar = new JarFile(root)) {
                    Enumeration<JarEntry> entries = jar.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();

                        // Tìm các file .class nằm trong luồng package mục tiêu
                        if (name.startsWith(packagePath) && name.endsWith(".class")) {
                            String className = name.replace('/', '.').substring(0, name.length() - 6);
                            try {
                                classes.add(Class.forName(className));
                            } catch (Throwable ignored) {
                                // Bỏ qua nếu class lỗi liên kết hệ thống hoặc chưa được obfuscate đồng bộ
                            }
                        }
                    }
                } catch (IOException e) {
                    // Nếu không phải file cấu trúc nén ZIP/JAR hợp lệ (ví dụ file exe thuần hệ thống), bỏ qua an toàn
                }
            }
        }
        return classes;
    }

    /**
     * Quét đệ quy trong thư mục (Dành cho môi trường IDE)
     */
    private static void scanDirectory(File directory, String currentPackage, Set<Class<?>> classes) throws Exception {
        if (!directory.exists() || directory.listFiles() == null) return;

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                scanDirectory(file, currentPackage + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = currentPackage + "." + file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        }
    }

    /**
     * Hàm Helper giữ nguyên từ code gốc của bạn để gán giá trị mặc định cho Primitive types khi dùng Reflection
     */
    public static Object getDefaultValueForPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == char.class) return '\u0000';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        return null;
    }
}