package ye.gov.pmo.bootstrap.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ye.gov.pmo.bootstrap.BootstrapApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = BootstrapApplication.class)
@AutoConfigureMockMvc
class PortalHomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homeReturnsPortalContentWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/portal/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hero.title").value("بوابة رئاسة مجلس الوزراء اليمني: المصدر الرسمي للمعلومة الحكومية"))
                .andExpect(jsonPath("$.latestNews[0].title").value("اجتماع لمناقشة أولويات الخدمات الحكومية الرقمية"))
                .andExpect(jsonPath("$.governancePrinciples[0]").value("الشفافية"));
    }
}
