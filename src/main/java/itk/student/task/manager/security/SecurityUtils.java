package itk.student.task.manager.security;

import itk.student.task.manager.enums.GlobalRole;
import itk.student.task.manager.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ForbiddenException("Authentication required");
        }
        return principal;
    }

    public static UUID currentUserId() {
        return currentUser().getId();
    }

    public static boolean isAdmin() {
        return currentUser().getGlobalRole() == GlobalRole.ADMIN;
    }
}
