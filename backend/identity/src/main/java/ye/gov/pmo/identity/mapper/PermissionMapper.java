package ye.gov.pmo.identity.mapper;

import ye.gov.pmo.identity.dto.PermissionRequest;
import ye.gov.pmo.identity.dto.PermissionResponse;
import ye.gov.pmo.identity.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public Permission toEntity(PermissionRequest request) {

        Permission permission = new Permission();

        permission.setName(request.getName());
        permission.setDescription(request.getDescription());

        return permission;
    }

    public PermissionResponse toResponse(Permission permission) {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setName(permission.getName());
        response.setDescription(permission.getDescription());
        return response;
    }
}