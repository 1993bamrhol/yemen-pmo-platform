package ye.gov.pmo.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "ye.gov.pmo")
@EntityScan(basePackages = {"ye.gov.pmo.identity.entity", "ye.gov.pmo.bootstrap.entity"})
@EnableJpaRepositories(basePackages = {"ye.gov.pmo.identity.repository", "ye.gov.pmo.bootstrap.repository"})
public class BootstrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootstrapApplication.class, args);
    }

}