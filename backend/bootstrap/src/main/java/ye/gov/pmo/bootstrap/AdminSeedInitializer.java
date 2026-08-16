package ye.gov.pmo.bootstrap;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ye.gov.pmo.identity.entity.Permission;
import ye.gov.pmo.identity.entity.Role;
import ye.gov.pmo.identity.entity.User;
import ye.gov.pmo.identity.service.PermissionService;
import ye.gov.pmo.identity.service.RoleService;
import ye.gov.pmo.identity.service.UserService;

@Component
public class AdminSeedInitializer implements ApplicationRunner {

    private final PermissionService permissionService;
    private final RoleService roleService;
    private final UserService userService;

    public AdminSeedInitializer(PermissionService permissionService, RoleService roleService, UserService userService) {
        this.permissionService = permissionService;
        this.roleService = roleService;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> requiredPermissions = List.of(
                "content.read", "content.write", "content.manage",
                "portal.publish", "portal.manage");

        for (String permissionName : requiredPermissions) {
            try {
                permissionService.findByName(permissionName);
            } catch (RuntimeException ex) {
                Permission permission = new Permission();
                permission.setName(permissionName);
                permission.setDescription("Permission for " + permissionName);
                permissionService.save(permission);
            }
        }

        Role adminRole;
        try {
            adminRole = roleService.findByName("PMO_ADMIN");
        } catch (RuntimeException ex) {
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
            userService.findByUsername("admin");
        } catch (RuntimeException ex) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@pmo.gov.ye");
            adminUser.setPassword("Admin@123");
            User savedUser = userService.save(adminUser);
            userService.addRole(savedUser.getId(), adminRole);
        }
    }
}
