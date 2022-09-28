package com.relationalai.utils;

import java.io.IOException;
import java.util.Properties;

public class SDKProperties {
    static Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(SDKProperties.class.getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getSDKVersion() {
        return properties.getProperty("sdk.version");
    }
}
