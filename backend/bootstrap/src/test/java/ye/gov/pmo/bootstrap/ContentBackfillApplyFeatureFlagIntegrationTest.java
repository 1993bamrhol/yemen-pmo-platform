package ye.gov.pmo.bootstrap;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "features.unified-content-backfill-apply.enabled=false")
@AutoConfigureMockMvc
class ContentBackfillApplyFeatureFlagIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void applyEndpointIsAbsentWhenFeatureFlagIsDisabled() throws Exception {
        mockMvc.perform(post("/api/v1/admin/content-backfill/apply")
                        .with(user("admin"))
                        .contentType("application/json")
                        .content("""
                                {"manifestSchemaVersion":1,"confirmation":"APPLY_UNIFIED_CONTENT_V1"}
                                """))
                .andExpect(status().isNotFound());
    }
}
