package hongik.triple.apimodule;

import hongik.triple.commonmodule.AcneLogCommonRoot;
import hongik.triple.domainmodule.AcneLogDomainRoot;
import hongik.triple.inframodule.AcneLogInfraRoot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@SpringBootApplication(scanBasePackageClasses = {
        AcneLogCommonRoot.class,
        AcneLogDomainRoot.class,
        AcneLogInfraRoot.class,
        ApiModuleApplication.class
})
@EntityScan(basePackages = "hongik.triple.domainmodule") // 도메인 모듈의 엔티티 경로
@EnableJpaRepositories(basePackages = "hongik.triple.domainmodule") // 도메인 모듈의 레포지토리 경로
public class ApiModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiModuleApplication.class, args);
        log.info("AcneLog Server Started!");
    }

}
