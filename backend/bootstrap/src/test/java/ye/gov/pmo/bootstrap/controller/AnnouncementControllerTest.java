package ye.gov.pmo.bootstrap.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ye.gov.pmo.news.controller.AnnouncementController;
import ye.gov.pmo.news.dto.AnnouncementResponse;
import ye.gov.pmo.news.service.AnnouncementService;

@SpringBootTest(classes = AnnouncementControllerTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class AnnouncementControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(AnnouncementController.class)
    static class TestApplication {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnnouncementService announcementService;

    @Test
    void findAllReturnsAnnouncements() throws Exception {
        given(announcementService.findAll()).willReturn(List.of(
                new AnnouncementResponse(1L, "إعلان رسمي", "إعلان", "18 أغسطس 2026", "نص الإعلان")));

        mockMvc.perform(get("/api/announcements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("إعلان رسمي"))
                .andExpect(jsonPath("$[0].category").value("إعلان"));
    }
}
