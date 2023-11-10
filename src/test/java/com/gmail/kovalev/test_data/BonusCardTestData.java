package com.gmail.kovalev.test_data;

import com.gmail.kovalev.entity.BonusCard;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class BonusCardTestData {

    @Builder.Default
    private String number = "Bonus123456789NoMatter";

    public BonusCard buildBonusCard() {
        return new BonusCard(number);
    }
}
