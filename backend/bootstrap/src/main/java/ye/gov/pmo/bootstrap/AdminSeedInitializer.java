package ye.gov.pmo.bootstrap;

import java.util.List;
import java.util.Set;
import java.util.UUID;
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
import ye.gov.pmo.identity.service.RoleAssignmentService;

@Component
public class AdminSeedInitializer implements ApplicationRunner {

    private static final UUID PMO_ENTITY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final PermissionService permissionService;
    private final RoleService roleService;
    private final UserService userService;
    private final RoleAssignmentService roleAssignmentService;
    private final String adminUsername;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeedInitializer(
            PermissionService permissionService,
            RoleService roleService,
            UserService userService,
            RoleAssignmentService roleAssignmentService,
            @Value("${bootstrap.admin.username:admin}") String adminUsername,
            @Value("${bootstrap.admin.email:admin@pmo.gov.ye}") String adminEmail,
            @Value("${bootstrap.admin.password:}") String adminPassword) {

        this.permissionService = permissionService;
        this.roleService = roleService;
        this.userService = userService;
        this.roleAssignmentService = roleAssignmentService;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> requiredPermissions = List.of(
                "content.read", "content.write", "content.review", "content.approve",
                "content.publish", "content.archive", "content.manage",
                "portal.publish", "portal.manage",
                "entities.read", "entities.write", "entities.manage",
                "assignments.read", "assignments.write", "assignments.manage",
                "users.read", "users.write", "users.manage",
                "roles.read", "roles.write", "roles.manage",
                "permissions.read", "permissions.write",
                "audit.read");

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

        Role pmoAdminRole = ensureRole("PMO_ADMIN", "PMO entity administrator");
        pmoAdminRole = assignPermissions(pmoAdminRole, Set.of(
                "content.read", "content.write", "content.review", "content.approve",
                "content.publish", "content.archive", "content.manage",
                "portal.publish", "portal.manage", "entities.read", "entities.write",
                "assignments.read", "assignments.write"));
        Role platformAdminRole = ensureRole("PLATFORM_SUPER_ADMIN", "National platform super administrator");
        platformAdminRole = assignPermissions(platformAdminRole, Set.copyOf(requiredPermissions));
        ensureStandardRole("ENTITY_ADMIN", "Entity administrator", Set.of(
                "entities.read", "entities.write", "assignments.read", "assignments.write",
                "content.read", "content.write"));
        ensureStandardRole("EDITOR", "Content editor", Set.of("content.read", "content.write"));
        ensureStandardRole("REVIEWER", "Content reviewer", Set.of("content.read", "content.review"));
        ensureStandardRole("PUBLISHER", "Content publisher", Set.of(
                "content.read", "content.approve", "content.publish", "content.archive", "portal.publish"));
        ensureStandardRole("SERVICE_MANAGER", "Government service manager", Set.of("entities.read"));

        User adminUser;
        try {
            adminUser = userService.findByUsername(adminUsername);
        } catch (ResourceNotFoundException ex) {
            if (adminPassword == null || adminPassword.isBlank()) {
                throw new IllegalStateException(
                        "ADMIN_PASSWORD must be configured when the initial administrator does not exist");
            }

            adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(adminPassword);
            adminUser = userService.save(adminUser);
        }

        final User resolvedAdminUser = adminUser;
        boolean hasLegacyRole = resolvedAdminUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("PMO_ADMIN"));
        if (!hasLegacyRole) {
            adminUser = userService.addRole(resolvedAdminUser.getId(), pmoAdminRole);
        }

        roleAssignmentService.grantPlatform(adminUser.getId(), platformAdminRole.getId(), adminUser.getId());
        roleAssignmentService.grantEntityForBootstrap(
                adminUser.getId(), pmoAdminRole.getId(), PMO_ENTITY_ID, adminUser.getId());
    }

    private Role ensureRole(String name, String description) {
        try {
            return roleService.findByName(name);
        } catch (ResourceNotFoundException ex) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            return roleService.save(role);
        }
    }

    private void ensureStandardRole(String name, String description, Set<String> permissions) {
        assignPermissions(ensureRole(name, description), permissions);
    }

    private Role assignPermissions(Role role, Set<String> permissionNames) {
        Role current = role;
        for (String permissionName : permissionNames) {
            Permission permission = permissionService.findByName(permissionName);
            boolean alreadyAssigned = current.getPermissions().stream()
                    .anyMatch(item -> item.getName().equals(permissionName));
            if (!alreadyAssigned) {
                current = roleService.addPermission(current.getId(), permission);
            }
        }
        return current;
    }
}
