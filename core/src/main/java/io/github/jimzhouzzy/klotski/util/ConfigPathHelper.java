/*
 * Copyright (C) 2025 Zhiyu Zhou (jimzhouzzy@gmail.com)
 * This file is part of github.com/jimzhouzzy/Klotski.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * ConfigPathHelper.java
 * 
 * This class provides a method to get the configuration file path based on the operating system.
 * It checks the OS type and returns the appropriate path for Windows, macOS, Linux/Unix, or a default path.
 * 
 * It helps in managing configuration files for the Klotski game, 
 * ensuring that the files are stored in a user-friendly location.
 * 
 * @author JimZhouZZY
 * @version 1.17
 * @since 2025-5-25
 * @see {@link SettingsScreen}
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: implement blocked pieces
 * 2025-05-27: modify font
 * 2025-05-27: Show error dialog when load-save failed
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-05-21: Thz (#9)
 * 2025-05-21: bug fix: audio settings (not completed)
 * 2025-04-30: optimize local storage
 */

package io.github.jimzhouzzy.klotski.util;

import java.nio.file.Paths;

public class ConfigPathHelper {

    public static String getConfigFilePath(String appName, String fileName) {
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
