package com.gmail.kovalev.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Order {
    private UUID id;
    private List<Product> products;
    private OffsetDateTime createDate;
}
