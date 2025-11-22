package hongik.triple.commonmodule.dto.analysis;

import java.util.List;

public record AnalysisLogRes(
        Long analysisId,
        String imageUrl,
        String userName,
        String userSkinType,
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
