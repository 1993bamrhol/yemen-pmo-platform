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
class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void findAllReturnsNews() throws Exception {
        mockMvc.perform(get("/api/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("اجتماع لمناقشة أولويات الخدمات الحكومية الرقمية"))
                .andExpect(jsonPath("$[0].category").value("الأخبار"));
    }

    @Test
    void findByIdReturnsSingleNewsArticle() throws Exception {
        mockMvc.perform(get("/api/news/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("اعتماد الإطار المؤسسي للبوابة الرسمية"));
    }
}
