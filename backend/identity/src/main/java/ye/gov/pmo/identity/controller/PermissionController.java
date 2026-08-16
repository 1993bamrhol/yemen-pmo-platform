package ye.gov.pmo.identity.controller;

import ye.gov.pmo.identity.dto.PermissionRequest;
import ye.gov.pmo.identity.dto.PermissionResponse;
import ye.gov.pmo.identity.entity.Permission;
import ye.gov.pmo.identity.mapper.PermissionMapper;
import ye.gov.pmo.identity.service.PermissionService;
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
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    public PermissionController(PermissionService permissionService, PermissionMapper permissionMapper) {
        this.permissionService = permissionService;
        this.permissionMapper = permissionMapper;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('permissions.read')")
    public ResponseEntity<List<PermissionResponse>> findAll() {
        List<PermissionResponse> permissions = permissionService.findAll()
                .stream()
                .map(permissionMapper::toResponse)
                .toList();

        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permissions.read')")
    public ResponseEntity<PermissionResponse> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(permissionMapper.toResponse(permissionService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('permissions.write')")
    public ResponseEntity<PermissionResponse> create(@Valid @RequestBody PermissionRequest request) {
        Permission permission = permissionMapper.toEntity(request);
        Permission savedPermission = permissionService.save(permission);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(permissionMapper.toResponse(savedPermission));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permissions.write')")
    public ResponseEntity<PermissionResponse> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody PermissionRequest request) {

        Permission permission = permissionMapper.toEntity(request);
        Permission updatedPermission = permissionService.update(id, permission);

        return ResponseEntity.ok(permissionMapper.toResponse(updatedPermission));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permissions.write')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
