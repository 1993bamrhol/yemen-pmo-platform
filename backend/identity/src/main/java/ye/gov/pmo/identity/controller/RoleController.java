package ye.gov.pmo.identity.controller;

import ye.gov.pmo.identity.dto.RoleRequest;
import ye.gov.pmo.identity.dto.RoleResponse;
import ye.gov.pmo.identity.entity.Role;
import ye.gov.pmo.identity.entity.Permission;
import ye.gov.pmo.identity.mapper.RoleMapper;
import ye.gov.pmo.identity.service.PermissionService;
import ye.gov.pmo.identity.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;
    private final PermissionService permissionService;

    public RoleController(
            RoleService roleService,
            RoleMapper roleMapper,
            PermissionService permissionService) {
        this.roleService = roleService;
        this.roleMapper = roleMapper;
        this.permissionService = permissionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('roles.read')")
    public ResponseEntity<List<RoleResponse>> findAll() {
        List<RoleResponse> roles = roleService.findAll()
                .stream()
                .map(roleMapper::toResponse)
                .toList();

        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('roles.read')")
    public ResponseEntity<RoleResponse> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(roleMapper.toResponse(roleService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('roles.write')")
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        Role role = roleMapper.toEntity(request);
        Role savedRole = roleService.save(role);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roleMapper.toResponse(savedRole));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('roles.write')")
    public ResponseEntity<RoleResponse> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody RoleRequest request) {

        Role role = roleMapper.toEntity(request);
        Role updatedRole = roleService.update(id, role);

        return ResponseEntity.ok(roleMapper.toResponse(updatedRole));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('roles.write')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('roles.manage')")
    public ResponseEntity<RoleResponse> addPermission(
            @PathVariable("roleId") Long roleId,
            @PathVariable("permissionId") Long permissionId) {

        Permission permission = permissionService.findById(permissionId);
        Role updatedRole = roleService.addPermission(roleId, permission);

        return ResponseEntity.ok(roleMapper.toResponse(updatedRole));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('roles.manage')")
    public ResponseEntity<RoleResponse> removePermission(
            @PathVariable("roleId") Long roleId,
            @PathVariable("permissionId") Long permissionId) {

        Permission permission = permissionService.findById(permissionId);
        Role updatedRole = roleService.removePermission(roleId, permission);

        return ResponseEntity.ok(roleMapper.toResponse(updatedRole));
    }
}
