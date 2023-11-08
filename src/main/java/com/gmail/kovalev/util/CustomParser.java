package com.gmail.kovalev.util;

public interface CustomParser {
    String serialize(Object o) throws IllegalAccessException;

    <T> T deserialize(String jsonString, Class<T> clazz);

    String beautifyOneLineString(String jsonString);
}
