package hongik.triple.commonmodule.dto.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import hongik.triple.commonmodule.enumerate.SkinType;

import java.util.List;

public record AnalysisData(
        @JsonProperty("prediction_index")
        Integer predictionIndex,
        @JsonProperty("prediction_label")
        String predictionLabel,
        @JsonProperty("prediction_confidence")
        Double predictionConfidence,
        List<Double> scores
) {

    public SkinType labelToSkinType() {
        return switch (this.predictionLabel) {
            case "Comedones" -> SkinType.COMEDONES;
            case "Pustules" -> SkinType.PUSTULES;
            case "Papules" -> SkinType.PAPULES;
            case "Folliculitis" -> SkinType.FOLLICULITIS;
            default -> throw new IllegalArgumentException("Unknown label: " + this.predictionLabel);
        };
    }
}
