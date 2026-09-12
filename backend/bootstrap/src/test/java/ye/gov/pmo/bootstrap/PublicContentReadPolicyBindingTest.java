package ye.gov.pmo.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ye.gov.pmo.content.config.PublicContentReadPolicy;
import ye.gov.pmo.content.domain.ContentType;

class PublicContentReadPolicyBindingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withBean(PublicContentReadPolicy.class);

    @Test
    void offAndEmptyIsClosed() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(PublicContentReadPolicy.class).publicTypes()).isEmpty();
        });
    }

    @Test
    void enabledNewsAllowsNewsOnly() {
        contextRunner
                .withPropertyValues(
                        "features.unified-content-read.enabled=true",
                        "features.unified-content-read.allowed-types=NEWS")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(PublicContentReadPolicy.class).publicTypes())
                            .containsExactly(ContentType.NEWS);
                });
    }

    @Test
    void enabledAndEmptyIsClosed() {
        contextRunner
                .withPropertyValues(
                        "features.unified-content-read.enabled=true",
                        "features.unified-content-read.allowed-types=")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(PublicContentReadPolicy.class).publicTypes()).isEmpty();
                });
    }

    @Test
    void unknownContentTypeFailsStartupBinding() {
        contextRunner
                .withPropertyValues(
                        "features.unified-content-read.enabled=true",
                        "features.unified-content-read.allowed-types=NEWS,UNKNOWN")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("PublicContentReadPolicy");
                });
    }
}
