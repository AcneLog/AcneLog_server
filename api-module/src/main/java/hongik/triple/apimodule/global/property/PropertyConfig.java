package hongik.triple.apimodule.global.property;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(
        ignoreResourceNotFound = true,
        value = {
                "classpath:application.yml",
                "classpath:application-common.yml",
                "classpath:application-domain.yml",
                "classpath:application-infra.yml"
        },
        factory = YamlPropertySourceFactory.class
)
public class PropertyConfig {
}


