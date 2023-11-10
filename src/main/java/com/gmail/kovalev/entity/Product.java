package com.gmail.kovalev.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {
    private UUID id;
    private String name;
    private Double price;
}
