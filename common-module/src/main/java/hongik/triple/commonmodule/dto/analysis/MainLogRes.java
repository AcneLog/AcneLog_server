package hongik.triple.commonmodule.dto.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MainLogRes(
        @JsonProperty("COMEDONES") int comedones,
        @JsonProperty("PUSTULES") int pustules,
        @JsonProperty("PAPULES") int papules,
        @JsonProperty("FOLLICULITIS") int follicultis,
        List<AnalysisRes> analysisRes
) {
    public static MainLogRes from(int comedones, int pustules, int papules, int follicultis, List<AnalysisRes> analysisRes) {
        return new MainLogRes(
                comedones,
                pustules,
                papules,
                follicultis,
                analysisRes
        );
    }
}
