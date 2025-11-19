package hongik.triple.commonmodule.dto.analysis;

import java.util.List;

public record MainLogRes(
        int comedones,
        int pustules,
        int papules,
        int follicultis,
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
