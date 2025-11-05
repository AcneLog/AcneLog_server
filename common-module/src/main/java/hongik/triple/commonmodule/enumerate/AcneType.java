package hongik.triple.commonmodule.enumerate;

import lombok.Getter;

@Getter
public enum AcneType {

    NORMAL("정상", "피부 상태가 정상입니다.", "기본적인 세안과 보습을 유지하세요.", "건강한 식습관과 충분한 수면을 취하세요."),
    COMEDONES("좁쌀", "모공이 막혀 형성된 작은 돌기입니다.", "과도한 피지 제거를 피하고, 순한 클렌저를 사용하세요.", "규칙적인 각질 제거와 보습을 유지하세요."),
    PUSTULES("화농성", "피지선에 염증이 생겨 고름이 찬 여드름입니다.", "손으로 짜지 말고, 항염 작용이 있는 제품을 사용하세요.", "자극적인 음식과 스트레스를 피하세요."),
    PAPULES("염증성", "붉고 단단한 여드름으로, 염증이 동반됩니다.", "항염 작용이 있는 스킨케어 제품을 사용하세요.", "피부를 청결하게 유지하고, 자극을 피하세요."),
    FOLLICULITIS("모낭염", "모낭에 염증이 생긴 상태입니다.", "항생제 연고를 사용하고, 청결을 유지하세요.", "피부 자극을 최소화하고, 통풍이 잘 되는 옷을 입으세요.");

    private final String koreanName;
    private final String description;
    private final String careMethod;
    private final String guide;

    AcneType(String koreanName, String description, String careMethod, String guide) {
        this.koreanName = koreanName;
        this.description = description;
        this.careMethod = careMethod;
        this.guide = guide;
    }
}
