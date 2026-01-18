package vn.tafi.object;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConfigLoader {
	private static final String CONFIG_FILE_NAME = "App.config";
	private static final Map<String, String> properties = new HashMap<>();

	public static void loadProperties() throws IOException {
        File configFile = findConfigFile();

        if (configFile == null || !configFile.exists()) {
            throw new FileNotFoundException("Không tìm thấy file cấu hình: " + CONFIG_FILE_NAME);
        }

        System.out.println("Loading config from: " + configFile.getAbsolutePath());

        Properties props = new Properties();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            props.load(reader);
        }

        // Đưa tất cả properties vào Map
        properties.clear();
        for (String key : props.stringPropertyNames()) {
            properties.put(key, props.getProperty(key));
        }
    }

    /**
     * Tìm file App.config từ các vị trí khác nhau (theo ưu tiên):
     * 1. Thư mục tương đối (working directory) - cho debug
     * 2. Thư mục chứa JAR (app installation directory) - cho custom config sau cài đặt
     * 3. Thư mục resources trong JAR - mặc định được package với JAR
     * 4. Thư mục home của user - fallback cuối cùng
     */
    private static File findConfigFile() {
        // 1. Tìm ở thư mục hiện tại (working directory) - ưu tiên cao cho debug
        File workingDirConfig = new File(CONFIG_FILE_NAME);
        if (workingDirConfig.exists()) {
            System.out.println("Found App.config in working directory: " + workingDirConfig.getAbsolutePath());
            return workingDirConfig;
        }

        // 2. Tìm ở thư mục chứa JAR (app installation directory)
        try {
            String jarPath = ConfigLoader.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            File jarDir = new File(jarPath).getParentFile();
            File appDirConfig = new File(jarDir, CONFIG_FILE_NAME);
            if (appDirConfig.exists()) {
                System.out.println("Found App.config in app directory: " + appDirConfig.getAbsolutePath());
                return appDirConfig;
            }
        } catch (Exception e) {
            System.err.println("Error finding JAR directory: " + e.getMessage());
        }

        // 3. Tìm trong resources của JAR (default, packaged with JAR)
        try {
            InputStream resourceStream = ConfigLoader.class.getClassLoader()
                    .getResourceAsStream(CONFIG_FILE_NAME);
            if (resourceStream != null) {
                // Tạo file tạm thời từ resource stream
                File tempFile = File.createTempFile("App", ".config");
                tempFile.deleteOnExit();
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = resourceStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, length);
                    }
                }
                resourceStream.close();
                System.out.println("Found App.config in JAR resources: " + tempFile.getAbsolutePath());
                return tempFile;
            }
        } catch (Exception e) {
            System.err.println("Error loading from resources: " + e.getMessage());
        }

        // 4. Tìm ở thư mục home của user (fallback)
        String userHome = System.getProperty("user.home");
        File userHomeConfig = new File(userHome, CONFIG_FILE_NAME);
        if (userHomeConfig.exists()) {
            System.out.println("Found App.config in user home: " + userHomeConfig.getAbsolutePath());
            return userHomeConfig;
        }

        return null;
    }

	// Hàm lấy giá trị từ Map
	public static String getProperty(String key) {
		return properties.getOrDefault(key, ""); // Trả về giá trị mặc định nếu không có key
	}

	// Hàm kiểm tra nếu property tồn tại
	public static boolean hasProperty(String key) {
		return properties.containsKey(key);
	}

}
