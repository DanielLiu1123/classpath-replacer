package com.example;

/**
 * @author Freeman
 * @since 2023/2/12
 */
public class JsonUtil {

    private static final boolean jacksonPresent = isPresent("com.fasterxml.jackson.databind.ObjectMapper");
    private static final boolean gsonPresent = isPresent("com.google.gson.Gson");

    private static final JSON json;

    static {
        if (jacksonPresent) {
            json = new Jackson();
        } else if (gsonPresent) {
            json = new Gson();
        } else {
            throw new RuntimeException("No JSON implementation found");
        }
    }

    public static String toJson(Object object) {
        return json.toJson(object);
    }

    public static <T> T toBean(String jsonString, Class<T> clazz) {
        return json.toBean(jsonString, clazz);
    }

    private static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static JSON instance() {
        return json;
    }
}
