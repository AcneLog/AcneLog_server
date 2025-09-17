package hongik.triple.inframodule.s3;

import org.springframework.stereotype.Component;

@Component
public class S3Client {

    /**
     * S3 Object에 대한 presigned URL을 생성
     *
     * @param fileName 파일 이름
     * @param isUpload true면 업로드를 위한 presigned URL, false면 다운로드를 위한 presigned URL
     * @return the presigned URL as a String
     */
    public String getPresignedUrl(String fileName, Boolean isUpload) {
        return null;
    }
}
