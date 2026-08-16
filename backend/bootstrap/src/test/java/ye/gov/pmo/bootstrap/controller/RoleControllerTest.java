package ye.gov.pmo.bootstrap.controller;

import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ye.gov.pmo.identity.controller.RoleController;
import ye.gov.pmo.identity.dto.RoleResponse;
import ye.gov.pmo.identity.entity.Role;
import ye.gov.pmo.identity.exception.GlobalExceptionHandler;
import ye.gov.pmo.identity.mapper.RoleMapper;
import ye.gov.pmo.identity.service.PermissionService;
import ye.gov.pmo.identity.service.RoleService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = RoleControllerTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({RoleController.class, GlobalExceptionHandler.class})
    static class TestApplication {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @MockBean
    private RoleMapper roleMapper;

    @MockBean
    private PermissionService permissionService;

    @Test
    void findAllReturnsRoles() throws Exception {
        Role role = new Role();
        role.setName("ADMIN");

        RoleResponse response = new RoleResponse();
        response.setName("ADMIN");

        given(roleService.findAll()).willReturn(List.of(role));
        given(roleMapper.toResponse(role)).willReturn(response);

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }

    @Test
    void createReturnsCreated() throws Exception {
        Role role = new Role();
        role.setName("ADMIN");

        RoleResponse response = new RoleResponse();
        response.setName("ADMIN");

        given(roleMapper.toEntity(org.mockito.ArgumentMatchers.any())).willReturn(role);
        given(roleService.save(role)).willReturn(role);
        given(roleMapper.toResponse(role)).willReturn(response);

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"ADMIN","description":"System admin"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void updateReturnsOk() throws Exception {
        Role role = new Role();
        role.setName("ADMIN");

        RoleResponse response = new RoleResponse();
        response.setName("ADMIN");

        given(roleMapper.toEntity(org.mockito.ArgumentMatchers.any())).willReturn(role);
        given(roleService.update(1L, role)).willReturn(role);
        given(roleMapper.toResponse(role)).willReturn(response);

        mockMvc.perform(put("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"ADMIN","description":"Updated"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        doNothing().when(roleService).delete(1L);

        mockMvc.perform(delete("/api/roles/1"))
                .andExpect(status().isNoContent());
    }

}
