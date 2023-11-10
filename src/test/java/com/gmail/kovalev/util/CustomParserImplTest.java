package com.gmail.kovalev.util;

import com.gmail.kovalev.test_data.BonusCardTestData;
import com.gmail.kovalev.test_data.CustomerTestData;
import com.gmail.kovalev.test_data.OrderTestData;
import com.gmail.kovalev.test_data.ProductTestData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CustomParserImplTest {

    private CustomParser customParser;
    private Gson gson;
    private Gson prettyGson;
    private Gson gsonWithAdapter;
    private Gson prettyGsonWithAdapter;

    @BeforeEach
    void setUp() {
        customParser = new CustomParserImpl();
        gson = new Gson();
        prettyGson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        gsonWithAdapter = new GsonBuilder()
                .registerTypeAdapter(OffsetDateTime.class, (JsonSerializer<OffsetDateTime>) (value, type, context) ->
                        new JsonPrimitive(value.format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSSXXXXX")))
                )
                .registerTypeAdapter(OffsetDateTime.class, (JsonDeserializer<OffsetDateTime>) (value, type, context) ->
                        OffsetDateTime.parse(value.getAsString(), DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSSXXXXX"))

                )
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (value, type, context) ->
                        new JsonPrimitive(value.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (value, type, context) ->
                        LocalDate.parse(value.getAsString()))
                .create();
        prettyGsonWithAdapter = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(OffsetDateTime.class, (JsonSerializer<OffsetDateTime>) (value, type, context) ->
                        new JsonPrimitive(value.format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSSXXXXX")))
                )
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (value, type, context) ->
                        new JsonPrimitive(value.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .create();
    }

    @Nested
    class TestMainMethodsForSimplePOJO {

        @ParameterizedTest
        @MethodSource("com.gmail.kovalev.util.CustomParserImplTest#getArgsForSimplePOJOTest")
        void serializeSimplePOJO (Object object) {
            // given
            String expected = gson.toJson(object);

            // when
            String actual;
            try {
                actual = customParser.serialize(object);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            // then
            if (object != null) {
                assertThat(actual)
                        .isEqualTo(expected);
            } else {
                assertThat(actual)
                        .isEqualTo("Yours Object is null. No sense to parse it!");
            }
        }

        @ParameterizedTest
        @MethodSource("com.gmail.kovalev.util.CustomParserImplTest#getArgsForSimplePOJOTest")
        void beautifySimplePOJO (Object object) {
            // given
            String expected = prettyGson.toJson(object);

            // when
            String actual;
            try {
                String rawJson = customParser.serialize(object);
                actual = customParser.beautifyOneLineString(rawJson, 2);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            // then
            // шут пойми какие в их библиотеке разделители для переноса строк
            if (object != null) {
                assertThat(actual.replaceAll("\r", "").replaceAll("\n", ""))
                        .isEqualTo(expected.replaceAll("\r", "").replaceAll("\n", ""));
            } else {
                assertThat(actual)
                        .isEqualTo("Yours JSON string is null. No sense to parse it!");
            }
        }

        @ParameterizedTest
        @MethodSource("com.gmail.kovalev.util.CustomParserImplTest#getArgsForSimplePOJOTest")
        void deserializeToSimplePOJO (Object object) {
            // given
            String json = null;
            Object expected = null;
            if (object != null) {
                json = gson.toJson(object);
                expected = gson.fromJson(json, object.getClass());
            }

            // when
            Object actual;
            if (object != null) {
                try {
                    String rawJson = customParser.serialize(object);
                    actual = customParser.deserialize(rawJson, object.getClass());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                actual = customParser.deserialize(null, Object.class);
            }

            // then
            if (json != null && !json.isEmpty()) {
                assertThat(actual)
                        .hasSameClassAs(expected);

                assertThat(actual.getClass().getDeclaredFields())
                        .usingRecursiveComparison().ignoringCollectionOrder()
                        .isEqualTo(expected.getClass().getDeclaredFields());

                // В принципе проверяет, что заполнено теми же значениями.
                assertThat(actual.toString())
                        .isEqualTo(expected.toString());
            } else {
                assertThat(actual)
                        .isNull();
            }
        }
    }

    @Nested
    class TestMainMethodsForPOJOWithFieldList {

        @ParameterizedTest
        @MethodSource("com.gmail.kovalev.util.CustomParserImplTest#getArgsForPOJOWithFieldListTest")
        void serializePOJOWithFieldList (Object object) {
            // given
            String expected = gsonWithAdapter.toJson(object);

            // when
            String actual;
            try {
                actual = customParser.serialize(object);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            // then
            if (object != null) {
                assertThat(actual)
                        .isEqualTo(expected);
            } else {
                assertThat(actual)
                        .isEqualTo("Yours Object is null. No sense to parse it!");
            }
        }

        @ParameterizedTest
        @MethodSource("com.gmail.kovalev.util.CustomParserImplTest#getArgsForPOJOWithFieldListTest")
        void beautifyPOJOWithFieldList (Object object) {
            // given
            String expected = prettyGsonWithAdapter.toJson(object);

            // when
            String actual;
            try {
                String rawJson = customParser.serialize(object);
                actual = customParser.beautifyOneLineString(rawJson, 2);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            // then
            // шут пойми какие в их библиотеке разделители для переноса строк
            if (object != null) {
                assertThat(actual.replaceAll("\r", "").replaceAll("\n", ""))
                        .isEqualTo(expected.replaceAll("\r", "").replaceAll("\n", ""));
            } else {
                assertThat(actual)
                        .isEqualTo("Yours JSON string is null. No sense to parse it!");
            }
        }

        @ParameterizedTest
        @MethodSource("com.gmail.kovalev.util.CustomParserImplTest#getArgsForPOJOWithFieldListTest")
        void deserializePOJOWithFieldList (Object object) {
            // given
            String json = null;
            Object expected = null;
            if (object != null) {
                json = gsonWithAdapter.toJson(object);
                expected = gsonWithAdapter.fromJson(json, object.getClass());
            }

            // when
            Object actual;
            if (object != null) {
                try {
                    String rawJson = customParser.serialize(object);
                    actual = customParser.deserialize(rawJson, object.getClass());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                actual = customParser.deserialize(null, Object.class);
            }

            // then
            if (json != null && !json.isEmpty()) {
                assertThat(actual)
                        .hasSameClassAs(expected);

                assertThat(actual.getClass().getDeclaredFields())
                        .usingRecursiveComparison().ignoringCollectionOrder()
                        .isEqualTo(expected.getClass().getDeclaredFields());

                // В принципе проверяет, что заполнено теми же значениями.
                assertThat(actual.toString())
                        .isEqualTo(expected.toString());
            } else {
                assertThat(actual)
                        .isNull();
            }
        }
    }

    @Nested
    class TestMainMethodsForPOJOWithManyFieldTypes {

        @ParameterizedTest
        @MethodSource("com.gmail.kovalev.util.CustomParserImplTest#getArgsForPOJOPOJOWithManyFieldTypesTest")
        void serializePOJOWithManyFieldTypes (Object object) {
            // given
            String expected = gsonWithAdapter.toJson(object);

            // when
            String actual;
            try {
                actual = customParser.serialize(object);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            // then
            if (object != null) {
                assertThat(actual)
                        .isEqualTo(expected);
            } else {
                assertThat(actual)
                        .isEqualTo("Yours Object is null. No sense to parse it!");
            }
        }

        @ParameterizedTest
        @MethodSource("com.gmail.kovalev.util.CustomParserImplTest#getArgsForPOJOPOJOWithManyFieldTypesTest")
        void beautifyPOJOWithManyFieldTypes (Object object) {
            // given
            String expected = prettyGsonWithAdapter.toJson(object);

            // when
            String actual;
            try {
                String rawJson = customParser.serialize(object);
                actual = customParser.beautifyOneLineString(rawJson, 2);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            // then
            // шут пойми какие в их библиотеке разделители для переноса строк
            if (object != null) {
                assertThat(actual.replaceAll("\r", "").replaceAll("\n", ""))
                        .isEqualTo(expected.replaceAll("\r", "").replaceAll("\n", ""));
            } else {
                assertThat(actual)
                        .isEqualTo("Yours JSON string is null. No sense to parse it!");
            }
        }

        @ParameterizedTest
        @MethodSource("com.gmail.kovalev.util.CustomParserImplTest#getArgsForPOJOPOJOWithManyFieldTypesTest")
        void deserializePOJOWithManyFieldTypes (Object object) {
            // given
            String json = null;
            Object expected = null;
            if (object != null) {
                json = gsonWithAdapter.toJson(object);
                expected = gsonWithAdapter.fromJson(json, object.getClass());
            }

            // when
            Object actual;
            if (object != null) {
                try {
                    String rawJson = customParser.serialize(object);
                    actual = customParser.deserialize(rawJson, object.getClass());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                actual = customParser.deserialize(null, Object.class);
            }

            // then
            if (json != null && !json.isEmpty()) {
                assertThat(actual)
                        .hasSameClassAs(expected);

                assertThat(actual.getClass().getDeclaredFields())
                        .usingRecursiveComparison().ignoringCollectionOrder()
                        .isEqualTo(expected.getClass().getDeclaredFields());

                // В принципе проверяет, что заполнено теми же значениями.
                assertThat(actual.toString())
                        .isEqualTo(expected.toString());
            } else {
                assertThat(actual)
                        .isNull();
            }
        }
    }

    public static Stream<Arguments> getArgsForPOJOPOJOWithManyFieldTypesTest() {
        return Stream.of(
                Arguments.of(
                        CustomerTestData.builder().build().buildCustomer()
                ),
                Arguments.of(
                        CustomerTestData.builder()
                                .withBonusCard(
                                        BonusCardTestData.builder()
                                                .withNumber("897987979798")
                                                .build().buildBonusCard()
                                )
                                .build().buildCustomer()
                ),
//                Arguments.of(
//                        CustomerTestData.builder()                // GSON как то странно обрабатывает null поля
//                                .withBonusCard(null)              // он их просто не печатает
//                                .build().buildCustomer()          // Десериализация проходит успешно
//                ),
                Arguments.of((Object) null)
        );
    }

    public static Stream<Arguments> getArgsForPOJOWithFieldListTest() {
        return Stream.of(
                Arguments.of(
                        OrderTestData.builder().build().buildOrder()
                ),
                Arguments.of(
                        OrderTestData.builder()
                                .withProducts(
                                        List.of(
                                                ProductTestData.builder().build().buildProduct(),
                                                ProductTestData.builder()
                                                        .withName("Rice")
                                                        .withPrice(6.22)
                                                        .build().buildProduct(),
                                                ProductTestData.builder()
                                                        .withName("Cotton shoes")
                                                        .withPrice(33.2)
                                                        .build().buildProduct()
                                        )
                                )
                                .build().buildOrder()
                ),
                Arguments.of((Object) null)
        );
    }

    public static Stream<Arguments> getArgsForSimplePOJOTest() {
        return Stream.of(
                Arguments.of(
                        BonusCardTestData.builder().build().buildBonusCard()
                ),
                Arguments.of(
                        BonusCardTestData.builder()
                                .withNumber("NewNumberBLABLABLA")
                                .build().buildBonusCard()
                ),
                Arguments.of(
                        (Object) null
                ),
                Arguments.of(
                        ProductTestData.builder().build().buildProduct()
                ),
                Arguments.of(
                        ProductTestData.builder()
                                .withId(UUID.randomUUID())
                                .withName("Kefir")
                                .withPrice(6.66)
                                .build().buildProduct()
                )
        );
    }
}