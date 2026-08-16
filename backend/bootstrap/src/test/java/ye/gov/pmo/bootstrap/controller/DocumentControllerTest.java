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
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void findAllReturnsDocuments() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("وثيقة التحليل المؤسسي"));
    }

    @Test
    void findByIdReturnsSingleDocument() throws Exception {
        mockMvc.perform(get("/api/documents/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("خطة MVP"));
    }
}
