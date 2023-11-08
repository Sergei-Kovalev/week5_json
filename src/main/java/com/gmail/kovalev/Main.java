package com.gmail.kovalev;

import com.gmail.kovalev.entity.Customer;
import com.gmail.kovalev.entity.Order;
import com.gmail.kovalev.entity.Product;
import com.gmail.kovalev.entity.Skill;
import com.gmail.kovalev.util.CustomParser;
import com.gmail.kovalev.util.CustomParserImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) throws IllegalAccessException {
        CustomParser parser = new CustomParserImpl();
        Product product1 = new Product(UUID.randomUUID(), "Milk", 5.11);
        Product product2 = new Product(UUID.randomUUID(), "Bread", 2.22);
        Order order1 = new Order(UUID.randomUUID(), List.of(product1, product2), OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.of("+03:00")));
        List<Order> orders = new ArrayList<>();
        orders.add(order1);

        Customer customer = new Customer(UUID.randomUUID(),
                new Skill[]{
                        new Skill("Run", "Fast running"),
                        new Skill(null, "Deep sleeping")
                },
                "Sergey", "Kovalev", LocalDate.of(1982, Month.DECEMBER, 21), orders, true);


        String serializedOneLine = parser.serialize(customer);
        System.out.printf("""
                
                ONE-LINE JSON BELLOW
                -----------------------------------------------
                %s
                """, serializedOneLine);
        System.out.println("--------------------------------------");

        String beautifulJSON = parser.beautifyOneLineString(serializedOneLine);
        System.out.printf("""
                
                FORMATTED JSON BELLOW
                -----------------------------------------------
                %s
                """, beautifulJSON);
        System.out.println("--------------------------------------");

        Customer deserialized = parser.deserialize(beautifulJSON, Customer.class);
        System.out.printf("""
                
                DESERIALIZED POJO BELLOW (toString())
                -----------------------------------------------
                %s
                """, deserialized);
        System.out.println("--------------------------------------");
    }
}
