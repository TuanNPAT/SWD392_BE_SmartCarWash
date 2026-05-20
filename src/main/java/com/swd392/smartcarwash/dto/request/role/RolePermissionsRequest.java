package com.swd392.smartcarwash.dto.request.role;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class RolePermissionsRequest {
    @NotEmpty(message = "Permission IDs must not be empty")
    public Set<Long> permissionIds;
    
    // reason field inherited from BaseActionRequest
    // Will be auto-populated when @UserAction(requiresReason = true)
}
