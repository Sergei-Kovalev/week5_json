package com.gmail.kovalev.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        return String.format("{%s}", serialized);
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
            case "UUID", "String", "OffsetDateTime", "LocalDate" ->
                    String.format("\"%s\"", fieldObject);               // нужно расширить принимаемыми типами данных
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
    public String beautifyOneLineString(String jsonStringOneLine) {
        int howManySpaces = 4;
        char[] chars = jsonStringOneLine.toCharArray();
        String newLine = System.lineSeparator();

        StringBuilder formattedStr = new StringBuilder();
        boolean beginQuotes = false;

        for (int i = 0, indent = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '\"') {
                formattedStr.append(c);
                beginQuotes = !beginQuotes;
                continue;
            }

            if (!beginQuotes) {
                switch (c) {
                    case '{', '[' -> {
                        formattedStr.append(c).append(newLine).append(String.format("%" + (indent += howManySpaces) + "s", ""));
                        continue;
                    }
                    case '}', ']' -> {
                        formattedStr.append(newLine).append((indent -= howManySpaces) > 0 ? String.format("%" + indent + "s", "") : "").append(c);
                        continue;
                    }
                    case ':' -> {
                        formattedStr.append(c).append(" ");
                        continue;
                    }
                    case ',' -> {
                        formattedStr.append(c).append(newLine).append(indent > 0 ? String.format("%" + indent + "s", "") : "");
                        continue;
                    }
                    default -> {
                        if (Character.isWhitespace(c)) continue;
                    }
                }
            }

            formattedStr.append(c);
        }
        return formattedStr.toString();
    }

    @Override
    public <T> T deserialize(String jsonString, Class<T> clazz) {
        String formatted = removeAllExtraWhitespaces(jsonString);
        Map<String, Object> map = new HashMap<>();
        System.out.println("LL parsing begin:");
        parseJsonObject(formatted, map, "ROOT");

        T t;
        try {
            t = fromMapToObject(map, clazz, "ROOT");
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchFieldException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    private <T> T fromMapToObject(Map<String, Object> map, Class<T> clazz, String mapLevel)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        Field[] declaredFields = clazz.getDeclaredFields();

        T instance = clazz.getDeclaredConstructor().newInstance();

        Map<String, Object> root = (Map<String, Object>) map.get(mapLevel);

        for (Field field : declaredFields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            if (fieldType == String.class) {
                String value = (String) root.get(fieldName);
                field.set(instance, value);
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                boolean value = (boolean) root.get(fieldName);
                field.set(instance, value);
            } else if (fieldType == double.class || fieldType == Double.class) {
                double value = Double.parseDouble((String) root.get(fieldName));
                field.set(instance, value);
            } else if (fieldType == int.class || fieldType == Integer.class) {
                int value = Integer.parseInt((String) root.get(fieldName));
                field.set(instance, value);
            } else if (fieldType == float.class || fieldType == Float.class) {
                float value = Float.parseFloat((String) root.get(fieldName));
                field.set(instance, value);
            } else if (fieldType == UUID.class) {
                UUID value = UUID.fromString((String) root.get(fieldName));
                field.set(instance, value);
            } else if (fieldType == LocalDate.class) {
                LocalDate value = LocalDate.parse((String) root.get(fieldName));
                field.set(instance, value);
            } else if (fieldType == OffsetDateTime.class) {
                OffsetDateTime value = OffsetDateTime.parse((String) root.get(fieldName));
                field.set(instance, value);
            } else if (fieldType == List.class) {
                List<T> fieldList = fillListWithInnerObjects(root, fieldName, instance);
                field.set(instance, fieldList);
            } else if (fieldType.getName().startsWith("[")) {
                T[] array = fillArrayWithInnerObjects(root, fieldName, instance);
                field.set(instance, array);
            } else if (fieldType.getName().contains("com.gmail.kovalev.entity")) {
                T innerInstance = fillInnerObjectFields(instance, fieldName, root);
                field.set(instance, innerInstance);
            }
        }
        return instance;
    }

    private <T> T fillInnerObjectFields(T instance, String fieldName, Map<String, Object> root) throws NoSuchFieldException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Field declaredField = instance.getClass().getDeclaredField(fieldName);
        String name = declaredField.getType().getName();
        Class<?> paramClass = Class.forName(name);

        Map<String, Object> innerMap = (Map<String, Object>) root.get(fieldName);
        return (T) fromMapToObject(innerMap, paramClass, "Inner object");
    }

    private <T> T[] fillArrayWithInnerObjects(Map<String, Object> root, String fieldName, T instance)
            throws NoSuchFieldException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        List<Object> innerList = (List<Object>) root.get(fieldName);
        Field declaredField = instance.getClass().getDeclaredField(fieldName);

        int arraySize = innerList.size();

        String name = declaredField.getType().getName();
        String subName = name.substring(2, name.length() - 1);
        Class<?> paramClass = Class.forName(subName);

        T[] array = (T[]) Array.newInstance(paramClass, arraySize);

        for (int i = 0; i < arraySize; i++) {
            Map<String, Object> innerMap = (Map<String, Object>) innerList.get(i);

            T innerInstance = (T) fromMapToObject(innerMap, paramClass, "Object in List");
            array[i] = innerInstance;
        }
        return array;
    }

    private <T> List<T> fillListWithInnerObjects(Map<String, Object> root, String fieldName, T instance)
            throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        List<Object> innerList = (List<Object>) root.get(fieldName);
        Field declaredField = instance.getClass().getDeclaredField(fieldName);

        Type genericType = declaredField.getGenericType();
        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

        Class<?> paramClass = null;
        for (Type type : actualTypeArguments) {
            paramClass = (Class<?>) type;
        }

        List<T> fieldList = new ArrayList<>();

        for (Object o : innerList) {
            Map<String, Object> innerMap = (Map<String, Object>) o;

            T innerInstance = (T) fromMapToObject(innerMap, paramClass, "Object in List");
            fieldList.add(innerInstance);
        }
        return fieldList;
    }

    public void parseJsonObject(String jsonString, Map<String, Object> map, String key) {
        Map<String, Object> mapObj = new HashMap<>();

        System.out.println("ok, about to parse JSON object " + jsonString);
        if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
            String pairList = jsonString.substring(1, jsonString.length() - 1).trim();
            parseJsonPairList(pairList, mapObj);

            map.put(key, mapObj);

        } else {
            System.out.println("Syntax error: not a JSON object. Must start with { and end with }");
        }
    }

    public void parseJsonPairList(String pairList, Map<String, Object> map) {

        pairList = pairList.trim();
        if (pairList.isEmpty()) {
            //System.out.println("pairList is empty");
            return;
        }
        if (pairList.startsWith(",")) {
            pairList = pairList.substring(1).trim();
        }

        System.out.println("ok, about to parse pair list " + pairList);
        if (pairList.charAt(0) != '\"') {
            System.out.println("syntax error: key must be of type STRING, input:  + pairList");
            return;
        }
        StringBuilder key = new StringBuilder();
        StringBuilder sbPairList = new StringBuilder(pairList);
        cutNextToken(sbPairList, ":", key);
        System.out.println("found KEY: " + key);
        // checking type of value - may be STRING / OBJECT / ArrayList<OBJECT> / NULL / BOOLEAN / NUMERIC
        StringBuilder value = new StringBuilder();
        if (sbPairList.charAt(0) == '{') {
            cutTillMatchingParen(sbPairList, "{", "}", value);
            System.out.println("found VALUE of type OBJECT:" + value);

            Map<String, Object> mapForInnerObject = new HashMap<>();
            parseJsonObject(value.toString(), mapForInnerObject, "Inner object");
            map.put(key.toString(), mapForInnerObject);

        } else if (sbPairList.charAt(0) == '[') {
            cutTillMatchingParen(sbPairList, "[", "]", value);

            System.out.println("found VALUE of type ArrayList<OBJECT>" + value);

            String arrayString = value.substring(1, value.length() - 1).trim();
            List<String> stringsArray = splitByObjects(arrayString);

            List<Object> list = new ArrayList<>();
            for (String str : stringsArray) {
                Map<String, Object> mapForInnerObject = new HashMap<>();
                parseJsonObject(str, mapForInnerObject, "Object in List");
                list.add(mapForInnerObject);
            }

            map.put(key.toString(), list);

        } else if (sbPairList.charAt(0) == '\"') {
            cutNextToken(sbPairList, ",", value);
            System.out.println("found VALUE of type STRING: " + value);

            map.put(key.toString(), value.toString());

        } else if (sbPairList.charAt(0) == 'n') {
            sbPairList.delete(0, 4);
            System.out.println("found VALUE of type NULL: " + null);

            map.put(key.toString(), null);

        } else if (sbPairList.charAt(0) == 't' || sbPairList.charAt(0) == 'f') {
            if (sbPairList.charAt(0) == 't') {
                sbPairList.delete(0, 4);
                System.out.println("found VALUE of type BOOLEAN: " + true);

                map.put(key.toString(), true);

            } else {
                sbPairList.delete(0, 5);
                System.out.println("found VALUE of type BOOLEAN: " + false);

                map.put(key.toString(), false);

            }
        } else if ("0123456789".contains(sbPairList.charAt(0) + "")) {
            String valueOfNumeric = findNumeric(sbPairList);
            System.out.println("found VALUE of type NUMERIC: " + valueOfNumeric);
            sbPairList.delete(0, valueOfNumeric.length());

            map.put(key.toString(), valueOfNumeric);

        } else {
            System.out.println("syntax error: VALUE must be STRING / OBJECT / ArrayList<OBJECT> / NULL / BOOLEAN / NUMERIC");
            return;
        }
        parseJsonPairList(sbPairList.toString(), map);
    }

    private String findNumeric(StringBuilder sbPairList) {
        String string = sbPairList.toString();
        StringBuilder value = new StringBuilder();
        while (!string.isEmpty() && "0123456789.".contains(string.charAt(0) + "")) {
            value.append(string.charAt(0));
            string = string.substring(1);
        }
        return value.toString();
    }

    private ArrayList<String> splitByObjects(String arrayString) {
        StringBuilder value = new StringBuilder();
        StringBuilder sourceString = new StringBuilder(arrayString);
        ArrayList<String> arrayList = new ArrayList<>();
        while (!sourceString.isEmpty()) {
            cutTillMatchingParen(sourceString, "{", "}", value);
            arrayList.add(value.toString());
            if (!sourceString.isEmpty()) {
                sourceString.deleteCharAt(0); // удаляем запятую, если объектов несколько
            }
        }
        return arrayList;
    }

    public void cutNextToken(StringBuilder sourceStringBuilder, String separator, StringBuilder token) {
        String sourceString = sourceStringBuilder.toString();
        if (sourceString.trim().isEmpty()) {
            return;
        }
        int sepIndex = sourceString.indexOf(separator);
        if (sepIndex == -1) { // разделитель не найден, последний элемент списка
            token.setLength(0);
            token.append(sourceString, 1, sourceString.length() - 1);
            sourceStringBuilder.setLength(0);
        } else {
            String key = sourceString.substring(1, sepIndex - 1);
            String restOfString = sourceString.substring(sepIndex + separator.length());
            sourceStringBuilder.setLength(0);
            sourceStringBuilder.append(restOfString);
            token.setLength(0);
            token.append(key);
        }
    }

    public void cutTillMatchingParen(StringBuilder sbSrc, String openParen, String closeParen, StringBuilder matchPart) {
        String src = sbSrc.toString();
        matchPart.setLength(0);
        int openParenCount = 0;
        String state = "not copying";
        for (int i = 0; i < src.length(); i++) {
            String cs = String.valueOf(src.charAt(i)); // cs - current symbol
            if (state.equals("not copying")) {
                if (cs.equals(openParen)) {
                    state = "copying";
                }
            }
            if (state.equals("copying")) {
                matchPart.append(cs);
                if (cs.equals(openParen)) {
                    openParenCount = openParenCount + 1;
                }
                if (cs.equals(closeParen)) {
                    openParenCount = openParenCount - 1;
                }
                if (openParenCount == 0) {
                    break;
                }
            }
        }
        sbSrc.setLength(0);
        sbSrc.append(src.substring(matchPart.length()));
    }

    private String removeAllExtraWhitespaces(String jsonString) {
        char[] chars = jsonString.toCharArray();
        StringBuilder formattedStr = new StringBuilder();
        boolean beginQuotes = false;

        for (char c : chars) {
            if (c == '\"') {
                formattedStr.append(c);
                beginQuotes = !beginQuotes;
                continue;
            }
            if (!beginQuotes) {
                if (c == '\n' || Character.isWhitespace(c)) {
                    continue;
                }
            }
            formattedStr.append(c);
        }
        return formattedStr.toString();
    }
}
