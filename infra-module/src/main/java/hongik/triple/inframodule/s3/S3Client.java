package hongik.triple.inframodule.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import hongik.triple.commonmodule.exception.ApplicationException;
import hongik.triple.commonmodule.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Component
public class S3Client {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.baseUrl}")
    private String baseUrl;

    private final AmazonS3 amazonS3;

    /**
     * 이미지 업로드
     */
    public String uploadImage(MultipartFile file, String dirName) {
        validateFile(file);
        validateImageExtension(file);

        String key = generateFileKey(file.getOriginalFilename(), dirName);
        ObjectMetadata metadata = createMetadata(file);
        uploadToS3(file, key, metadata);

        return key;
    }

    /**
     * 이미지 조회
     */
    public String getImage(String key) {
        validateKey(key);
        validateObjectExists(key);

        return baseUrl + "/" + key;
    }

    /**
     * 이미지 삭제
     */
    public void deleteImage(String key) {
        validateKey(key);
        validateObjectExists(key);

        deleteObjectFromS3(key);
    }

    /*
    file 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApplicationException(ErrorCode.EMPTY_FILE_EXCEPTION);
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new ApplicationException(ErrorCode.INVALID_FILENAME_EXCEPTION);
        }
    }

    /*
    file 확장자 검사
     */
    private void validateImageExtension(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();

        // 허용 확장자 목록
        List<String> allowed = List.of("jpg", "jpeg", "png", "gif", "webp");

        if (!allowed.contains(extension)) {
            throw new ApplicationException(ErrorCode.NOT_ALLOWED_FILE_EXTENSION);
        }
    }

    /*
    key 생성
     */
    private String generateFileKey(String originalName, String dirName) {
        String date = LocalDate.now().toString();
        String uuid = UUID.randomUUID().toString();

        return String.format("%s/%s_%s_%s", dirName, date, uuid, originalName);
    }

    /*
    Metadata 생성
     */
    private ObjectMetadata createMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        return metadata;
    }

    /*
    S3 업로드
     */
    private void uploadToS3(MultipartFile file, String key, ObjectMetadata metadata) {
        try (InputStream input = file.getInputStream()) {
            PutObjectRequest request = new PutObjectRequest(bucket, key, input, metadata);
            amazonS3.putObject(request);

        } catch (IOException e) {
            throw new ApplicationException(ErrorCode.FILE_IO_EXCEPTION);

        } catch (AmazonServiceException e) {
            log.error("AWS Service 에러: {}", e.getErrorMessage());
            throw new ApplicationException(ErrorCode.FAILED_UPLOAD_FILE);

        } catch (SdkClientException e) {
            log.error("AWS Client 에러: {}", e.getMessage());
            throw new ApplicationException(ErrorCode.FAILED_UPLOAD_FILE);
        }
    }

    /*
    key 유효성 검사
     */
    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new ApplicationException(ErrorCode.EMPTY_S3_KEY_EXCEPTION);
        }
    }

    /*
    S3 객체 유효성 검사
     */
    private void validateObjectExists(String key) {
        if (!amazonS3.doesObjectExist(bucket, key)) {
            throw new ApplicationException(ErrorCode.NOT_FOUND_S3_EXCEPTION);
        }
    }

    /*
    S3 이미지 객체 삭제
     */
    private void deleteObjectFromS3(String key) {
        try {
            amazonS3.deleteObject(bucket, key);
        } catch (AmazonServiceException e) {
            log.error("이미지 삭제 중 AWS Service 에러 발생: {}", e.getErrorMessage());
            throw new ApplicationException(ErrorCode.FAILED_DELETE_FILE);
        } catch (SdkClientException e) {
            log.error("이미지 삭제 중 AWS Client 에러 발생: {}", e.getMessage());
            throw new ApplicationException(ErrorCode.FAILED_DELETE_FILE);
        }
    }

}
