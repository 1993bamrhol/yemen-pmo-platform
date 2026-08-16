package ye.gov.pmo.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import ye.gov.pmo.identity.entity.Role;
import ye.gov.pmo.identity.exception.ResourceConflictException;
import ye.gov.pmo.identity.exception.ResourceNotFoundException;
import ye.gov.pmo.identity.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void savePersistsRoleWhenUnique() {
        Role role = new Role();
        role.setName("ADMIN");

        given(roleRepository.findByName("ADMIN")).willReturn(Optional.empty());
        given(roleRepository.save(any(Role.class))).willAnswer(invocation -> invocation.getArgument(0));

        Role saved = roleService.save(role);

        assertEquals("ADMIN", saved.getName());
        verify(roleRepository).save(role);
    }

    @Test
    void saveRejectsDuplicateRoleName() {
        Role existing = new Role();
        Role duplicate = new Role();
        duplicate.setName("ADMIN");

        given(roleRepository.findByName("ADMIN")).willReturn(Optional.of(existing));

        assertThrows(ResourceConflictException.class, () -> roleService.save(duplicate));
    }

    @Test
    void updateThrowsWhenMissing() {
        given(roleRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roleService.update(1L, new Role()));
    }
}
