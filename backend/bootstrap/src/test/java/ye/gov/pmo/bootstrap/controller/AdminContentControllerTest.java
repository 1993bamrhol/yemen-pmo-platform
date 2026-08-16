package ye.gov.pmo.bootstrap.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = AdminContentControllerTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminContentControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "ye.gov.pmo.bootstrap.entity")
    @EnableJpaRepositories(basePackages = "ye.gov.pmo.bootstrap.repository")
    @Import(AdminContentController.class)
    static class TestApplication {
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void contentEndpointReturnsList() throws Exception {
        mockMvc.perform(get("/api/admin/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("اجتماع لمناقشة أولويات الخدمات الحكومية الرقمية"))
                .andExpect(jsonPath("$[0].status").value("منشور"));
    }

    @Test
    @Order(2)
    void summaryEndpointReturnsCounts() throws Exception {
        mockMvc.perform(get("/api/admin/content/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.published").value(2));
    }

    @Test
    @Order(3)
    void createAndUpdateAndDeleteContent() throws Exception {
        mockMvc.perform(post("/api/admin/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "news",
                                  "title": "موضوع جديد",
                                  "status": "draft",
                                  "author": "مستخدم",
                                  "category": "الأخبار"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("موضوع جديد"));

        mockMvc.perform(put("/api/admin/content/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "news",
                                  "title": "تحديث الموضوع",
                                  "status": "published",
                                  "author": "مستخدم",
                                  "category": "الأخبار"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("تحديث الموضوع"));

        mockMvc.perform(delete("/api/admin/content/1"))
                .andExpect(status().isNoContent());
    }
}
