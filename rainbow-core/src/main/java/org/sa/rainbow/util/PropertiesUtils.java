package org.sa.rainbow.util;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;

public class PropertiesUtils {

    public static String getEnvOrProperty(String key) {
        return getEnvOrProperty(key, null);
    }

    public static String getEnvOrProperty(String key, String defaultValue) {
        String value = getenv(key);
        if (value == null) {
            value = getProperty(key);
        }
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }
}
