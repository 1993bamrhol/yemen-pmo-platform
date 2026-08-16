package ye.gov.pmo.identity.service;

import ye.gov.pmo.identity.entity.Permission;
import ye.gov.pmo.identity.exception.ResourceConflictException;
import ye.gov.pmo.identity.exception.ResourceNotFoundException;
import ye.gov.pmo.identity.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public Permission findById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Permission not found with id: " + id));
    }

    public Permission findByName(String name) {
        return permissionRepository.findByName(name)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Permission not found: " + name));
    }

    public Permission save(Permission permission) {
        ensureUnique(permission.getName(), null);
        return permissionRepository.save(permission);
    }

    public Permission update(Long id, Permission permission) {
        Permission existing = findById(id);

        ensureUnique(permission.getName(), id);

        existing.setName(permission.getName());
        existing.setDescription(permission.getDescription());

        return permissionRepository.save(existing);
    }

    public void delete(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permission not found with id: " + id);
        }

        permissionRepository.deleteById(id);
    }

    private void ensureUnique(String name, Long currentId) {
        permissionRepository.findByName(name)
                .ifPresent(existing -> {
                    if (currentId == null || !existing.getId().equals(currentId)) {
                        throw new ResourceConflictException("Permission already exists: " + name);
                    }
                });
    }
}