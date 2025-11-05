package hongik.triple.commonmodule.dto.analysis;

public record NaverProductDto(
        String productId,
        String productName,
        String productUrl,
        Integer productPrice,
        String productImage,
        String categoryName,
        String mallName,
        String brand
) {}