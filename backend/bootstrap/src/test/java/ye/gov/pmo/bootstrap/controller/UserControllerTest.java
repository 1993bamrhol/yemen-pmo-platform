package ye.gov.pmo.bootstrap.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ye.gov.pmo.identity.controller.UserController;
import ye.gov.pmo.identity.dto.UserResponse;
import ye.gov.pmo.identity.entity.User;
import ye.gov.pmo.identity.exception.GlobalExceptionHandler;
import ye.gov.pmo.identity.mapper.UserMapper;
import ye.gov.pmo.identity.service.RoleService;
import ye.gov.pmo.identity.service.UserService;
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
import java.util.List;

@SpringBootTest(classes = UserControllerTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({UserController.class, GlobalExceptionHandler.class})
    static class TestApplication {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private RoleService roleService;

    @Test
    void findAllReturnsUsers() throws Exception {
        User user = new User();
        user.setUsername("admin");
        user.setEmail("admin@example.com");

        UserResponse response = new UserResponse();
        response.setUsername("admin");
        response.setEmail("admin@example.com");

        given(userService.findAll()).willReturn(List.of(user));
        given(userMapper.toResponse(user)).willReturn(response);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].email").value("admin@example.com"));
    }

    @Test
    void createReturnsCreated() throws Exception {
        User savedUser = new User();
        savedUser.setUsername("admin");
        savedUser.setEmail("admin@example.com");

        given(userService.save(org.mockito.ArgumentMatchers.any(User.class))).willReturn(savedUser);
        given(userMapper.toResponse(savedUser)).willReturn(new UserResponse());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","email":"admin@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void updateReturnsOk() throws Exception {
        User updatedUser = new User();
        updatedUser.setUsername("editor");
        updatedUser.setEmail("editor@example.com");

        UserResponse response = new UserResponse();
        response.setUsername("editor");
        response.setEmail("editor@example.com");

        given(userService.update(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(User.class)))
                        .willReturn(updatedUser);
        given(userMapper.toResponse(updatedUser)).willReturn(response);

        mockMvc.perform(put("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"username":"editor","email":"editor@example.com","password":"password123"}
                                        """))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username").value("editor"))
                        .andExpect(jsonPath("$.email").value("editor@example.com"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/1"))
                        .andExpect(status().isNoContent());
    }

    @Test
    void missingUserReturnsNotFound() throws Exception {
        given(userService.findById(99L))
                        .willThrow(new ye.gov.pmo.identity.exception.ResourceNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));
    }
}
