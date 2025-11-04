package hongik.triple.commonmodule.enumerate;

import lombok.Getter;

@Getter
public enum AcneType {

    NORMAL("정상"),
    COMEDONES("좁쌀"),
    PUSTULES("화농성"),
    PAPULES("염증성"),
    FOLLICULITIS("모낭염");

    private final String description;

    AcneType(String description) {
        this.description = description;
    }
}
