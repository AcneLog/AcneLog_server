package hongik.triple.commonmodule.dto.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import hongik.triple.commonmodule.enumerate.AcneType;

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

    public AcneType labelToSkinType() {
        return switch (this.predictionLabel) {
            case "Comedones" -> AcneType.COMEDONES;
            case "Pustules" -> AcneType.PUSTULES;
            case "Papules" -> AcneType.PAPULES;
            case "Folliculitis" -> AcneType.FOLLICULITIS;
            default -> throw new IllegalArgumentException("Unknown label: " + this.predictionLabel);
        };
    }
}
