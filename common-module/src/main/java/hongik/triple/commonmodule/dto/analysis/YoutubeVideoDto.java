package hongik.triple.commonmodule.dto.analysis;

public record YoutubeVideoDto(
        String videoId,
        String videoTitle,
        String videoUrl,
        String channelName,
        String thumbnailUrl
) {}