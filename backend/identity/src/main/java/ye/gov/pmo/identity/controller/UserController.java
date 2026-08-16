package ye.gov.pmo.identity.controller;

import ye.gov.pmo.identity.dto.UserRequest;
import ye.gov.pmo.identity.dto.UserResponse;
import ye.gov.pmo.identity.entity.User;
import ye.gov.pmo.identity.entity.Role;
import ye.gov.pmo.identity.service.RoleService;
import ye.gov.pmo.identity.mapper.UserMapper;
import ye.gov.pmo.identity.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final RoleService roleService;

    public UserController(
            UserService userService,
            UserMapper userMapper,
            RoleService roleService) {

        this.userService = userService;
        this.userMapper = userMapper;
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('users.read')")
    public ResponseEntity<List<UserResponse>> findAll() {

        List<UserResponse> users = userService.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('users.read')")
    public ResponseEntity<UserResponse> findById(
            @PathVariable("id") Long id) {

        User user = userService.findById(id);

        return ResponseEntity.ok(
                userMapper.toResponse(user)
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('users.write')")
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody UserRequest request) {

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        User savedUser = userService.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toResponse(savedUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('users.write')")
    public ResponseEntity<UserResponse> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserRequest request) {

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        User updatedUser = userService.update(id, user);

        return ResponseEntity.ok(
                userMapper.toResponse(updatedUser)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('users.write')")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id) {

        userService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('users.manage')")
    public ResponseEntity<UserResponse> addRole(
            @PathVariable("userId") Long userId,
            @PathVariable("roleId") Long roleId) {

        Role role = roleService.findById(roleId);
        User updatedUser = userService.addRole(userId, role);

        return ResponseEntity.ok(userMapper.toResponse(updatedUser));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('users.manage')")
    public ResponseEntity<UserResponse> removeRole(
            @PathVariable("userId") Long userId,
            @PathVariable("roleId") Long roleId) {

        Role role = roleService.findById(roleId);
        User updatedUser = userService.removeRole(userId, role);

        return ResponseEntity.ok(userMapper.toResponse(updatedUser));
    }
}