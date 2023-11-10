package com.gmail.kovalev.test_data;

import com.gmail.kovalev.entity.BonusCard;
import com.gmail.kovalev.entity.Customer;
import com.gmail.kovalev.entity.Order;
import com.gmail.kovalev.entity.Skill;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.UUID;

@Data
@Builder(setterPrefix = "with")
public class CustomerTestData {

    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Builder.Default
    private Skill[] skills = {
            SkillTestData.builder().build().buildSkill(),
            SkillTestData.builder()
                    .withNameOfSkill("Swim")
                    .withDescription("Deep swimming")
                    .build().buildSkill()
    };

    @Builder.Default
    private String firstName = "Sergey";

    @Builder.Default
    private String lastName = "Kovalev";

    @Builder.Default
    private LocalDate dateBirth = LocalDate.of(1982, Month.DECEMBER, 21);

    @Builder.Default
    private List<Order> orders = List.of(
            OrderTestData.builder().build().buildOrder(),
            OrderTestData.builder()
                    .withProducts(List.of(
                            ProductTestData.builder().build().buildProduct(),
                            ProductTestData.builder()
                                    .withName("Meat")
                                    .build().buildProduct(),
                            ProductTestData.builder()
                                    .withName("Bread")
                                    .withPrice(5.5)
                                    .build().buildProduct()
                    ))
                    .build().buildOrder()
    );

    @Builder.Default
    private boolean isWeird = true;

    @Builder.Default
    private BonusCard bonusCard = BonusCardTestData.builder()
            .build().buildBonusCard();

    public Customer buildCustomer() {
        return new Customer(id, skills, firstName, lastName, dateBirth, orders, isWeird, bonusCard);
    }
}
