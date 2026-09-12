package ye.gov.pmo.bootstrap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "features.unified-content-read.enabled=true",
        "features.unified-content-read.allowed-types="
})
@AutoConfigureMockMvc
class PublicContentEmptyAllowlistIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void enabledReaderWithEmptyAllowlistFailsClosed() throws Exception {
        mockMvc.perform(get("/api/v1/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        mockMvc.perform(get("/api/v1/content").param("type", "NEWS"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/content/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/content/by-slug/news/not-public"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/entities/{entityId}/content", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
