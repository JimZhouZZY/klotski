package io.github.jimzhouzzy.klotski.util;

import java.nio.file.Paths;

public class ConfigPathHelper {

    public String getConfigFilePath(String appName, String fileName) {
        String userHome = System.getProperty("user.home");
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            // Windows: 使用 %APPDATA% 目录
            return Paths.get(System.getenv("APPDATA"), appName, fileName).toString();
        } else if (osName.contains("mac")) {
            // macOS: 使用 ~/Library/Application Support/ 目录
            System.out.println(Paths.get(userHome, "Library", "Application Support", appName, fileName).toString());
            return Paths.get(userHome, "Library", "Application Support", appName, fileName).toString();
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            // Linux/Unix: 使用 ~/.config/ 目录
            return Paths.get(userHome, ".config", appName, fileName).toString();
        } else {
            // 默认: 使用用户主目录
            return Paths.get(userHome, appName, fileName).toString();
        }
    }
}
