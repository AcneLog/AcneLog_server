package hongik.triple.commonmodule.dto.analysis;

import java.util.List;

public record AnalysisRes(
        Long analysisId,
        String imageUrl,
        String createdAt,
        Boolean isPublic,
        String acneType,
        String description,
        String careMethod,
        String guide,
        List<YoutubeVideoDto> videoList,
        List<NaverProductDto> productList
) {
}
