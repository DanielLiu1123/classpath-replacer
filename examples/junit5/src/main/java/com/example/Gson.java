package com.example;

/**
 * @author Freeman
 */
public class Gson implements JSON {

    private static final com.google.gson.Gson gson = new com.google.gson.Gson();

    @Override
    public String toJson(Object object) {
        return gson.toJson(object);
    }

    @Override
    public <T> T toBean(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}
