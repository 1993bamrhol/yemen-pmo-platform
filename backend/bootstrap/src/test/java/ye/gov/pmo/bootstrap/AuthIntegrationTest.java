package ye.gov.pmo.bootstrap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import ye.gov.pmo.identity.dto.AuthResponse;
import ye.gov.pmo.identity.entity.Permission;
import ye.gov.pmo.identity.entity.Role;
import ye.gov.pmo.identity.entity.User;
import ye.gov.pmo.identity.service.PermissionService;
import ye.gov.pmo.identity.service.RoleService;
import ye.gov.pmo.identity.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Test
    void loginReturnsJwtAndProtectsEndpoints() throws Exception {
        Permission permission = permissionService.findByName("users.read");

        Role role = new Role();
        role.setName("ADMIN");
        role.setDescription("Administrator");
        role = roleService.save(role);
        role = roleService.addPermission(role.getId(), permission);

        User allowedUser = new User();
        allowedUser.setUsername("auth-admin");
        allowedUser.setEmail("auth-admin@example.com");
        allowedUser.setPassword("password123");
        allowedUser = userService.save(allowedUser);
        allowedUser = userService.addRole(allowedUser.getId(), role);

        User deniedUser = new User();
        deniedUser.setUsername("auth-viewer");
        deniedUser.setEmail("auth-viewer@example.com");
        deniedUser.setPassword("password123");
        userService.save(deniedUser);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        String deniedBody = objectMapper.writeValueAsString(new LoginPayload("auth-viewer", "password123"));
        String deniedResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deniedBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse deniedAuth = objectMapper.readValue(deniedResponse, AuthResponse.class);
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + deniedAuth.getToken()))
                .andExpect(status().isForbidden());

        String body = objectMapper.writeValueAsString(new LoginPayload("auth-admin", "password123"));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + authResponse.getToken()))
                .andExpect(status().isOk());
    }

    private static class LoginPayload {
        private String username;
        private String password;

        private LoginPayload(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
