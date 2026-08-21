package ye.gov.pmo.bootstrap;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ye.gov.pmo.identity.entity.Permission;
import ye.gov.pmo.identity.entity.Role;
import ye.gov.pmo.identity.entity.User;
import ye.gov.pmo.identity.exception.ResourceNotFoundException;
import ye.gov.pmo.identity.service.PermissionService;
import ye.gov.pmo.identity.service.RoleService;
import ye.gov.pmo.identity.service.UserService;

@Component
public class AdminSeedInitializer implements ApplicationRunner {

    private final PermissionService permissionService;
    private final RoleService roleService;
    private final UserService userService;
    private final String adminUsername;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeedInitializer(
            PermissionService permissionService,
            RoleService roleService,
            UserService userService,
            @Value("${bootstrap.admin.username:admin}") String adminUsername,
            @Value("${bootstrap.admin.email:admin@pmo.gov.ye}") String adminEmail,
            @Value("${bootstrap.admin.password:}") String adminPassword) {

        this.permissionService = permissionService;
        this.roleService = roleService;
        this.userService = userService;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> requiredPermissions = List.of(
                "content.read", "content.write", "content.manage",
                "portal.publish", "portal.manage");

        for (String permissionName : requiredPermissions) {
            try {
                permissionService.findByName(permissionName);
            } catch (ResourceNotFoundException ex) {
                Permission permission = new Permission();
                permission.setName(permissionName);
                permission.setDescription("Permission for " + permissionName);
                permissionService.save(permission);
            }
        }

        Role adminRole;
        try {
            adminRole = roleService.findByName("PMO_ADMIN");
        } catch (ResourceNotFoundException ex) {
            Role role = new Role();
            role.setName("PMO_ADMIN");
            role.setDescription("System administrator");
            adminRole = roleService.save(role);
        }

        for (String permissionName : requiredPermissions) {
            Permission permission = permissionService.findByName(permissionName);
            boolean alreadyAssigned = adminRole.getPermissions().stream()
                    .anyMatch(item -> item.getName().equals(permissionName));
            if (!alreadyAssigned) {
                adminRole = roleService.addPermission(adminRole.getId(), permission);
            }
        }

        try {
            userService.findByUsername(adminUsername);
        } catch (ResourceNotFoundException ex) {
            if (adminPassword == null || adminPassword.isBlank()) {
                throw new IllegalStateException(
                        "ADMIN_PASSWORD must be configured when the initial administrator does not exist");
            }

            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(adminPassword);
            User savedUser = userService.save(adminUser);
            userService.addRole(savedUser.getId(), adminRole);
        }
    }
}
