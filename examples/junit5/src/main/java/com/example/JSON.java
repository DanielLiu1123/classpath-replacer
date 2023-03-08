package com.example;

/**
 * @author Freeman
 */
public interface JSON {

    String toJson(Object object);

    <T> T toBean(String json, Class<T> clazz);
}
