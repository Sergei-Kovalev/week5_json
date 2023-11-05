package com.gmail.kovalev.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private UUID id;
    private String name;
    private Double price;
}
