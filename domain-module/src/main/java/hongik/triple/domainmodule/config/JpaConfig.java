package hongik.triple.domainmodule.config;

import hongik.triple.domainmodule.AcneLogDomainRoot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@EnableJpaRepositories(basePackageClasses = {AcneLogDomainRoot.class})
@EntityScan(basePackageClasses = {AcneLogDomainRoot.class})
@Configuration
public class JpaConfig {

    @PersistenceContext
    private EntityManager em;
}