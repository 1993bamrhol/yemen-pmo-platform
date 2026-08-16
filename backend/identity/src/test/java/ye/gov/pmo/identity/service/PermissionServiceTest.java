package ye.gov.pmo.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import ye.gov.pmo.identity.entity.Permission;
import ye.gov.pmo.identity.exception.ResourceConflictException;
import ye.gov.pmo.identity.exception.ResourceNotFoundException;
import ye.gov.pmo.identity.repository.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    void savePersistsPermissionWhenUnique() {
        Permission permission = new Permission();
        permission.setName("users.read");

        given(permissionRepository.findByName("users.read")).willReturn(Optional.empty());
        given(permissionRepository.save(any(Permission.class))).willAnswer(invocation -> invocation.getArgument(0));

        Permission saved = permissionService.save(permission);

        assertEquals("users.read", saved.getName());
        verify(permissionRepository).save(permission);
    }

    @Test
    void saveRejectsDuplicatePermissionName() {
        Permission existing = new Permission();
        Permission duplicate = new Permission();
        duplicate.setName("users.read");

        given(permissionRepository.findByName("users.read")).willReturn(Optional.of(existing));

        assertThrows(ResourceConflictException.class, () -> permissionService.save(duplicate));
    }

    @Test
    void deleteThrowsWhenMissing() {
        given(permissionRepository.existsById(1L)).willReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> permissionService.delete(1L));
    }
}
