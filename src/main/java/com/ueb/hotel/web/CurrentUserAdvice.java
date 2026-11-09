package com.ueb.hotel.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** I vendos në çdo faqe emrin e përdoruesit dhe nëse është ADMIN (për navigimin). */
@ControllerAdvice
public class CurrentUserAdvice {

    @ModelAttribute("currentUser")
    public String currentUser() {
        Authentication auth = authentication();
        return auth == null ? null : auth.getName();
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        Authentication auth = authentication();
        return auth != null && auth.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private static Authentication authentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }
}
