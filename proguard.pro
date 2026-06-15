# ====================================================================
# 1. CẤU HÌNH TỪ ĐIỂN ĐỔI TÊN THÀNH iiiililililili
# ====================================================================
-obfuscationdictionary i1lO0.txt
-classobfuscationdictionary i1lO0.txt
-packageobfuscationdictionary i1lO0.txt

# ====================================================================
# 2. SỬA LỖI INCOMPLETE CLASS HIERARCHY (QUAN TRỌNG)
# ====================================================================
-dontskipnonpubliclibraryclasses         # Đọc cả các class non-public của thư viện để hiểu rõ cấu trúc
-dontskipnonpubliclibraryclassmembers  # Đọc các phương thức non-public của thư viện
-dontwarn ** # Bỏ qua mọi cảnh báo thiếu class
-dontnote ** # Bỏ qua mọi thông báo chú ý hệ thống

# TẮT XÓA CODE VÀ TỐI ƯU HÓA (ĐỂ TRÁNH LỖI MẤT CODE JCEF)
-dontshrink
-dontoptimize

# BẮT BUỘC PHẢI GIỮ LẠI STACKMAPTABLE CHO JAVA 7 TRỞ LÊN (SỬA LỖI VERIFYERROR)
-keepattributes Signature,InnerClasses,EnclosingMethod,Deprecated,Annotation,SourceFile,LineNumberTable,StackMapTable

# XÓA HOẶC COMMENT DÒNG -dontpreverify ĐỂ PROGUARD TỰ ĐỘNG TÍNH TOÁN LẠI STACKMAPTABLE
# (Không dùng -dontpreverify khi chạy với Java 17)

# ====================================================================
# 3. GIỮ NGUYÊN VẸN 100% TẤT CẢ CÁC THƯ VIỆN NGOÀI
# ====================================================================
-keep class !home.thienph.** { *; }

# ====================================================================
# 4. GIỮ LẠI LỚP MAIN ĐỂ FILE EXE/JAR CHẠY ĐƯỢC
# ====================================================================
-keep public class home.thienph.Main {
    public static void main(java.lang.String[]);
}

# ====================================================================
# 1. BẢO VỆ CẤU TRÚC THƯ MỤC VÀ SERVICES CHO LOGBACK (QUAN TRỌNG NHẤT)
# ====================================================================
# Bắt buộc phải có để Logback nạp được META-INF/services/ qua ServiceLoader
-keepdirectories

# ====================================================================
# 1. BẢO VỆ CẤU TRÚC THƯ MỤC VÀ SERVICES CHO LOGBACK
# ====================================================================
# Giữ lại toàn bộ cấu trúc thư mục (rất quan trọng để Logback tìm thấy META-INF)
-keepdirectories META-INF/**
-keepdirectories ch.qos.logback.**
-keepdirectories org.slf4j.**

# Đảm bảo nội dung các file service không bị thay đổi nếu có mapping class
-adaptresourcefilenames **
-adaptresourcefilecontents META-INF/services/**,logback.xml

# ====================================================================
# 2. GIỮ NGUYÊN VẸN LOGBACK VÀ SLF4J
# ====================================================================
-keep class ch.qos.logback.** { *; }
-keep class org.slf4j.** { *; }

# ====================================================================
# 3. BẢO VỆ CÁC PHƯƠNG THỨC ENUM VÀ THUỘC TÍNH REFLECTION
# ====================================================================
# Logback cần các hàm này để parse chuỗi cấu hình trong logback.xml thành Enum
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Giữ lại các meta-data cần thiết cho Reflection
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions