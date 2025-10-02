package hongik.triple.commonmodule.enumerate;

import lombok.Getter;

@Getter
public enum SkinType {

    OILY("지성"),
    DRY("건성"),
    COMBINATION("수부지");

    private final String description;

    SkinType(String description) {
        this.description = description;
    }
}