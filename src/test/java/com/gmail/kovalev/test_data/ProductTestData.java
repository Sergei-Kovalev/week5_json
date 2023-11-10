package com.gmail.kovalev.test_data;

import com.gmail.kovalev.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder(setterPrefix = "with")
public class ProductTestData {

    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Builder.Default
    private String name = "Milk";

    @Builder.Default
    private Double price = 2.85;

    public Product buildProduct() {
        return new Product(id, name, price);
    }
}
