package io.mosip.testrig.apirig.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ErrorCodes {

    private static final Logger logger = LoggerFactory.getLogger(ErrorCodes.class);
    private static final Properties props = new Properties();

    static {
        loadProperties();
    }

    private ErrorCodes() {}

    private static void loadProperties() {
        try {

            File externalFile = new File("config/error-codes.properties");
            if (externalFile.exists()) {
                try (InputStream in = new FileInputStream(externalFile)) {
                    props.load(in);
                    logger.info("Loaded error-codes.properties from external config folder");
                    return;
                }
            }


            try (InputStream in = ErrorCodes.class.getResourceAsStream("/error-codes.properties")) {
                if (in != null) {
                    props.load(in);
                    logger.info("Loaded error-codes.properties from classpath");
                } else {
                    logger.warn("error-codes.properties not found in external config or classpath");
                }
            }

        } catch (Exception e) {
            logger.error("Failed to load error-codes.properties", e);
        }
    }


    public static String code(String key) {
        return key;
    }


    public static String message(String key, Object... args) {
        String template = props.getProperty(key, key);
        try {
            return MessageFormat.format(template, args);
        } catch (Exception e) {
            logger.warn("Failed to format message for key {}", key, e);
            return template;
        }
    }
}
