package com.gmail.kovalev.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
    public Object deserialize(String jsonString) {
        String formatted = removeAllExtraWhitespaces(jsonString);

        parseJsonObject(formatted);

        return null;
    }

    public void parseJsonObject(String jsonString) {
        System.out.println("ok, about to parse JSON object " + jsonString);
        if (jsonString.startsWith("{") && jsonString.endsWith("}"))
        {
            String pairList = jsonString.substring(1, jsonString.length() - 1).trim();
            System.out.println(pairList);

            parseJsonPairList(pairList);
        }
        else {
            System.out.println("Syntax error: not a JSON object. Must start with { and end with }");
        }
    }

    public void parseJsonPairList(String pairList) {
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
            parseJsonObject(value.toString());
            StringBuilder emptyString = new StringBuilder();
            cutNextToken(sbPairList, ",", emptyString);
        } else if (sbPairList.charAt(0) == '[') {
            cutTillMatchingParen(sbPairList, "[", "]", value);

            System.out.println("found VALUE of type ArrayList<OBJECT>" + value);

            String arrayString = value.substring(1, value.length() - 1).trim();
//            System.out.println(arrayString);
            ArrayList<String> stringsArray = splitByObjects(arrayString);

            for (String str : stringsArray) {
                parseJsonObject(str);
            }

        } else if (sbPairList.charAt(0) == '\"') {
            this.cutNextToken(sbPairList, ",", value);
            System.out.println("found VALUE of type STRING:" + value);

        } else if (sbPairList.charAt(0) == 'n') {
            sbPairList.delete(0, 4);
            System.out.println("found VALUE of type NULL: " + null);
        } else if (sbPairList.charAt(0) == 't' || sbPairList.charAt(0) == 'f') {
            if (sbPairList.charAt(0) == 't') {
                sbPairList.delete(0, 4);
                System.out.println("found VALUE of type BOOLEAN: " + true);
            } else {
                sbPairList.delete(0, 5);
                System.out.println("found VALUE of type BOOLEAN: " + false);
            }
        } else if ("0123456789".contains(sbPairList.charAt(0) + "")) {
            String valueOfNumeric = findNumeric(sbPairList);
            System.out.println("found VALUE of type NUMERIC: " + valueOfNumeric);
            sbPairList.delete(0, valueOfNumeric.length());
        }
        else {
            System.out.println("syntax error: VALUE must be STRING / OBJECT / ArrayList<OBJECT> / NULL / BOOLEAN / NUMERIC");
            return;
        }
        parseJsonPairList(sbPairList.toString());
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
        if (sourceString.trim().isEmpty()){
            return;
        }
        int sepIndex = sourceString.indexOf(separator);
        if (sepIndex == -1) { // разделитель не найден, последний элемент списка
            token.setLength(0);
            token.append(sourceString);
            sourceStringBuilder.setLength(0);
        } else {
            String key = sourceString.substring(0, sepIndex);
            String restOfString = sourceString.substring(sepIndex + separator.length());
            sourceStringBuilder.setLength(0);
            sourceStringBuilder.append(restOfString);
            token.setLength(0);
            token.append(key);
        }
    }

    public void cutTillMatchingParen(StringBuilder sbSrc, String openParen, String closeParen, StringBuilder matchPart){
        String src = sbSrc.toString();
        matchPart.setLength(0);
        int openParenCount = 0;
        String state = "not_copying";
        for (int i = 0; i < src.length(); i++){
            String cs = String.valueOf(src.charAt(i)); // cs - current symbol
            if (state.equals("not_copying")){
                if (cs.equals(openParen)) {
                    state = "copying";
                }
            }
            if (state.equals("copying")){
                matchPart.append(cs);
                if (cs.equals(openParen)){
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


//
//    public static HashMap<String, String> parseSimpleJson(String inputJsonString) {
//        final String regex = "(?:\\\"|\\')(?<key>[\\w\\d]+)(?:\\\"|\\')(?:\\:\\s*)(?:\\\"|\\')?(?<value>[\\w\\s-]*)(?:\\\"|\\')?";
//        HashMap<String, String> map = new HashMap<>();
//        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
//        final Matcher matcher = pattern.matcher(inputJsonString);
//
//        while (matcher.find()) {
//            for (int i = 1; i <= matcher.groupCount(); i++) {
//                map.put(matcher.group("key"), matcher.group("value"));
//            }
//        }
//        return map;
//    }

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
