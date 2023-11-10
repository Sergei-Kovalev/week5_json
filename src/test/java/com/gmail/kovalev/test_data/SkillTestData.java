package com.gmail.kovalev.test_data;

import com.gmail.kovalev.entity.Skill;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class SkillTestData {
    @Builder.Default
    private String nameOfSkill = "Run";

    @Builder.Default
    private String description = "Fast Running";

    public Skill buildSkill() {
        return new Skill(nameOfSkill, description);
    }
}
