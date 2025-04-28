package hongik.triple.apimodule;

import hongik.triple.commonmodule.AcneLogCommonRoot;
import hongik.triple.domainmodule.AcneLogDomainRoot;
import hongik.triple.inframodule.AcneLogInfraRoot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication(scanBasePackageClasses = {
        AcneLogCommonRoot.class,
        AcneLogDomainRoot.class,
        AcneLogInfraRoot.class,
        ApiModuleApplication.class
})
public class ApiModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiModuleApplication.class, args);
    }

}
