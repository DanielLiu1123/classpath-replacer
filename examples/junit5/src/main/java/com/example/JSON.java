package com.example;

/**
 * @author Freeman
 * @since 2023/2/12
 */
public interface JSON {

    String toJson(Object object);

    <T> T toBean(String json, Class<T> clazz);
}
