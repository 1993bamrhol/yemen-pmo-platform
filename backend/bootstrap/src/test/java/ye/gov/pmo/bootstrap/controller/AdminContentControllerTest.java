package ye.gov.pmo.bootstrap.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = AdminContentControllerTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminContentControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(AdminContentController.class)
    static class TestApplication {
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contentEndpointReturnsList() throws Exception {
        mockMvc.perform(get("/api/admin/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("اجتماع لمناقشة أولويات الخدمات الحكومية الرقمية"))
                .andExpect(jsonPath("$[0].status").value("منشور"));
    }

    @Test
    void summaryEndpointReturnsCounts() throws Exception {
        mockMvc.perform(get("/api/admin/content/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.published").value(2));
    }
}
