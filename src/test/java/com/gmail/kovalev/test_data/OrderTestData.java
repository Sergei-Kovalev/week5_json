package com.gmail.kovalev.test_data;

import com.gmail.kovalev.entity.Order;
import com.gmail.kovalev.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Data
@Builder(setterPrefix = "with")
public class OrderTestData {

    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Builder.Default
    private List<Product> products = List.of(
            ProductTestData.builder().build().buildProduct(),
            ProductTestData.builder().withName("Kefir").build().buildProduct()
    );

    @Builder.Default
    private OffsetDateTime createDate = OffsetDateTime.of(
            LocalDateTime.of(2023, Month.OCTOBER, 10, 12, 55, 59, 123456789),
            ZoneOffset.of("+03:00"));

    public Order buildOrder() {
        return new Order(id, products, createDate);
    }
}
