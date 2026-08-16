package ye.gov.pmo.bootstrap.controller;

import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ye.gov.pmo.identity.controller.PermissionController;
import ye.gov.pmo.identity.dto.PermissionResponse;
import ye.gov.pmo.identity.entity.Permission;
import ye.gov.pmo.identity.exception.GlobalExceptionHandler;
import ye.gov.pmo.identity.mapper.PermissionMapper;
import ye.gov.pmo.identity.service.PermissionService;
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

@SpringBootTest(classes = PermissionControllerTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class PermissionControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({PermissionController.class, GlobalExceptionHandler.class})
    static class TestApplication {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private PermissionMapper permissionMapper;

    @Test
    void findAllReturnsPermissions() throws Exception {
        Permission permission = new Permission();
        permission.setName("users.read");

        PermissionResponse response = new PermissionResponse();
        response.setName("users.read");

        given(permissionService.findAll()).willReturn(List.of(permission));
        given(permissionMapper.toResponse(permission)).willReturn(response);

        mockMvc.perform(get("/api/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("users.read"));
    }

    @Test
    void createReturnsCreated() throws Exception {
        Permission permission = new Permission();
        permission.setName("users.read");

        PermissionResponse response = new PermissionResponse();
        response.setName("users.read");

        given(permissionMapper.toEntity(org.mockito.ArgumentMatchers.any())).willReturn(permission);
        given(permissionService.save(permission)).willReturn(permission);
        given(permissionMapper.toResponse(permission)).willReturn(response);

        mockMvc.perform(post("/api/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"users.read","description":"Read users"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void updateReturnsOk() throws Exception {
        Permission permission = new Permission();
        permission.setName("users.read");

        PermissionResponse response = new PermissionResponse();
        response.setName("users.read");

        given(permissionMapper.toEntity(org.mockito.ArgumentMatchers.any())).willReturn(permission);
        given(permissionService.update(1L, permission)).willReturn(permission);
        given(permissionMapper.toResponse(permission)).willReturn(response);

        mockMvc.perform(put("/api/permissions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"users.read","description":"Updated"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        doNothing().when(permissionService).delete(1L);

        mockMvc.perform(delete("/api/permissions/1"))
                .andExpect(status().isNoContent());
    }
}
