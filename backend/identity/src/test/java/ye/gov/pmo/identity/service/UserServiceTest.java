package ye.gov.pmo.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import ye.gov.pmo.identity.entity.User;
import ye.gov.pmo.identity.exception.ResourceConflictException;
import ye.gov.pmo.identity.exception.ResourceNotFoundException;
import ye.gov.pmo.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void saveEncodesPasswordAndPersistsUser() {
        User user = new User();
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setPassword("password123");

        given(userRepository.findByUsername("admin")).willReturn(Optional.empty());
        given(userRepository.findByEmail("admin@example.com")).willReturn(Optional.empty());
        given(passwordEncoder.encode("password123")).willReturn("encoded-password");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.save(user);

        assertEquals("encoded-password", saved.getPassword());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(user);
    }

    @Test
    void saveRejectsDuplicateUsername() {
        User existing = new User();

        User user = new User();
        user.setUsername("admin");
        user.setEmail("admin2@example.com");
        user.setPassword("password123");

        given(userRepository.findByUsername("admin")).willReturn(Optional.of(existing));

        assertThrows(ResourceConflictException.class, () -> userService.save(user));
    }

    @Test
    void updateEncodesPasswordAndPersistsChanges() {
        User existing = new User();
        existing.setUsername("admin");
        existing.setEmail("admin@example.com");

        User incoming = new User();
        incoming.setUsername("editor");
        incoming.setEmail("editor@example.com");
        incoming.setPassword("password123");

        given(userRepository.findById(1L)).willReturn(Optional.of(existing));
        given(userRepository.findByUsername("editor")).willReturn(Optional.empty());
        given(userRepository.findByEmail("editor@example.com")).willReturn(Optional.empty());
        given(passwordEncoder.encode("password123")).willReturn("encoded-password");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.update(1L, incoming);

        assertEquals("editor", updated.getUsername());
        assertEquals("editor@example.com", updated.getEmail());
        assertEquals("encoded-password", updated.getPassword());
        verify(userRepository).save(existing);
    }

    @Test
    void deleteThrowsWhenUserMissing() {
        given(userRepository.existsById(eq(7L))).willReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(7L));
    }
}
