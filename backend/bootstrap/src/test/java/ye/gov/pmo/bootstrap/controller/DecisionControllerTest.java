package ye.gov.pmo.bootstrap.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ye.gov.pmo.bootstrap.BootstrapApplication;

@SpringBootTest(classes = BootstrapApplication.class)
@AutoConfigureMockMvc
class DecisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void findAllReturnsDecisions() throws Exception {
        mockMvc.perform(get("/api/decisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("قرار اعتماد الهوية البصرية الرسمية"));
    }

    @Test
    void findByIdReturnsSingleDecision() throws Exception {
        mockMvc.perform(get("/api/decisions/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("تعميم تنظيم المحتوى الحكومي"));
    }
}
