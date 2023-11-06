package com.gmail.kovalev.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private UUID id;
    private Skill[] skills;
    private String firstName;
    private String lastName;
    private LocalDate dateBirth;
    private List<Order> orders;
    private boolean isWeird;
}
