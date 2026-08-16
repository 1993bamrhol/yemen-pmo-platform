package ye.gov.pmo.identity.service;

import ye.gov.pmo.identity.entity.User;
import ye.gov.pmo.identity.entity.Role;
import ye.gov.pmo.identity.exception.ResourceConflictException;
import ye.gov.pmo.identity.exception.ResourceNotFoundException;
import ye.gov.pmo.identity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder) {

    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
}
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found: " + username));
    }

    public User save(User user) {
        ensureUnique(user.getUsername(), user.getEmail(), null);
        user.setPassword(
            passwordEncoder.encode(user.getPassword())
        );

        return userRepository.save(user);
    }

    public User update(Long id, User user) {
        User existing = findById(id);

        ensureUnique(user.getUsername(), user.getEmail(), id);

        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        existing.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(existing);
    }

    public User addRole(Long userId, Role role) {
        User user = findById(userId);
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    public User removeRole(Long userId, Role role) {
        User user = findById(userId);
        user.getRoles().remove(role);
        return userRepository.save(user);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }

    private void ensureUnique(String username, String email, Long currentId) {
        userRepository.findByUsername(username)
                .ifPresent(existing -> ensureDifferentEntity(existing.getId(), currentId,
                        "Username already exists: " + username));

        userRepository.findByEmail(email)
                .ifPresent(existing -> ensureDifferentEntity(existing.getId(), currentId,
                        "Email already exists: " + email));
    }

    private void ensureDifferentEntity(Long existingId, Long currentId, String message) {
        if (currentId == null || !existingId.equals(currentId)) {
            throw new ResourceConflictException(message);
        }
    }
}