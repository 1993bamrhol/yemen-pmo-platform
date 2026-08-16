package ye.gov.pmo.identity.service;

import ye.gov.pmo.identity.entity.Role;
import ye.gov.pmo.identity.entity.Permission;
import ye.gov.pmo.identity.exception.ResourceConflictException;
import ye.gov.pmo.identity.exception.ResourceNotFoundException;
import ye.gov.pmo.identity.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role not found with id: " + id));
    }

    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role not found: " + name));
    }

    public Role save(Role role) {
        ensureUnique(role.getName(), null);
        return roleRepository.save(role);
    }

    public Role update(Long id, Role role) {
        Role existing = findById(id);

        ensureUnique(role.getName(), id);

        existing.setName(role.getName());
        existing.setDescription(role.getDescription());

        return roleRepository.save(existing);
    }

    public Role addPermission(Long roleId, Permission permission) {
        Role role = findById(roleId);
        role.getPermissions().add(permission);
        return roleRepository.save(role);
    }

    public Role removePermission(Long roleId, Permission permission) {
        Role role = findById(roleId);
        role.getPermissions().remove(permission);
        return roleRepository.save(role);
    }

    public void delete(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role not found with id: " + id);
        }

        roleRepository.deleteById(id);
    }

    private void ensureUnique(String name, Long currentId) {
        roleRepository.findByName(name)
                .ifPresent(existing -> {
                    if (currentId == null || !existing.getId().equals(currentId)) {
                        throw new ResourceConflictException("Role already exists: " + name);
                    }
                });
    }
}