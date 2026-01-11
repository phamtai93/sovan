package vn.tafi.object;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConfigLoader {
	private static final String CONFIG_FILE_PATH = "updateFile.config"; // Đường dẫn file config
	private static final Map<String, String> properties = new HashMap<>();

	public static void loadProperties() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists()) {
            throw new FileNotFoundException("Không tìm thấy file cấu hình: " + CONFIG_FILE_PATH);
        }

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

	// Hàm lấy giá trị từ Map
	public static String getProperty(String key) {
		return properties.getOrDefault(key, ""); // Trả về giá trị mặc định nếu không có key
	}

	// Hàm kiểm tra nếu property tồn tại
	public static boolean hasProperty(String key) {
		return properties.containsKey(key);
	}

}
