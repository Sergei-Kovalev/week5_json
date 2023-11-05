package com.gmail.kovalev.util;

public interface CustomParser {
    String serialize(Object o) throws IllegalAccessException;

    Object deserialize(String jsonString);
}
