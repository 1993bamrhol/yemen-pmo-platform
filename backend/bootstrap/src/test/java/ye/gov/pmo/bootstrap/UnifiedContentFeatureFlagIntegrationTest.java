package ye.gov.pmo.bootstrap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "features.unified-content-read.enabled=false")
@AutoConfigureMockMvc
class UnifiedContentFeatureFlagIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void publicContentApiIsNotRegisteredWhenFeatureIsDisabled() throws Exception {
        mockMvc.perform(get("/api/v1/content"))
                .andExpect(status().isNotFound());
    }
}
