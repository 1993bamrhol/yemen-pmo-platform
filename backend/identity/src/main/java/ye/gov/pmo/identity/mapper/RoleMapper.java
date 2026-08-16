package ye.gov.pmo.identity.mapper;

import ye.gov.pmo.identity.dto.RoleRequest;
import ye.gov.pmo.identity.dto.RoleResponse;
import ye.gov.pmo.identity.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public Role toEntity(RoleRequest request) {

        Role role = new Role();

        role.setName(request.getName());
        role.setDescription(request.getDescription());

        return role;
    }

    public RoleResponse toResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        response.setDescription(role.getDescription());
        response.setPermissions(
                role.getPermissions()
                        .stream()
                        .map(permission -> permission.getName())
                        .collect(java.util.stream.Collectors.toSet())
        );
        return response;
    }
}