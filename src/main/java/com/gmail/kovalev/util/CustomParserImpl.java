package com.gmail.kovalev.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomParserImpl implements CustomParser {
    @Override
    public String serialize(Object object) throws IllegalAccessException {
        List<String> fields = new ArrayList<>();

        Field[] declaredFields = object.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            String nameAndValueToString = parseField(field, object);

            fields.add(nameAndValueToString);
        }

        String serialized = String.join(",", fields);

        return String.format("{%s}",serialized);
    }

    private String parseField(Field field, Object object) throws IllegalAccessException {
        field.setAccessible(true);
        String nameOfField = field.getName();
        String value;
        String typeName = field.getType().getSimpleName();

        Object fieldObject = field.get(object);

        if (typeName.endsWith("[]")) {
            value = arrayToString(fieldObject);
        } else {
            value = valueByFieldType(typeName, fieldObject);
        }
        return String.format("\"%s\": %s", nameOfField, value);
    }

    private String valueByFieldType(String typeName, Object fieldObject) throws IllegalAccessException {
        if (fieldObject == null) {
            return "null";
        }

        return switch (typeName) {
            case "Double", "double", "Integer", "int", "Boolean", "boolean", "long", "Long" -> fieldObject.toString();
            case "UUID", "String", "OffsetDateTime", "LocalDate" -> String.format("\"%s\"", fieldObject);               // нужно расширить принимаемыми типами данных
            case "List" -> listToString(fieldObject);
            default -> serialize((fieldObject));
        };
    }

    private String listToString(Object fieldObject) throws IllegalAccessException {
        List<String> strings = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object> objects = (List<Object>) fieldObject;
        for (Object o : objects) {
            String string = serialize(o);
            strings.add(string);
        }
        return "[" + String.join(",", strings) + "]";
    }

    private String arrayToString(Object fieldObject) throws IllegalAccessException {
        List<String> strings = new ArrayList<>();

        for (Object o : (Object[]) fieldObject) {
            String simpleName = o.getClass().getSimpleName();
            String string = valueByFieldType(simpleName, o);
            strings.add(string);
        }
        return "[" + String.join(",", strings) + "]";
    }

    @Override
    public Object deserialize(String jsonString) {
        return null;
    }
}
