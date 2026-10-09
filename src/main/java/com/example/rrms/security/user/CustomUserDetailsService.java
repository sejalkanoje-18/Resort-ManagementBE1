package com.example.rrms.security.user;

import com.example.rrms.domain.TenantStatus;
import com.example.rrms.domain.modal.User;
import com.example.rrms.repository.TenantRepository;
import com.example.rrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository users;
    private final TenantRepository tenants;

    /** username == user id (string), as stored in the JWT "sub" claim. */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return loadUserById(Long.valueOf(userId));
    }

    @Transactional(readOnly = true)
    public UserPrincipal loadUserById(Long id) {
        User u = users.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean tenantActive = true;
        if (u.getTenantId() != null) {
            tenantActive = tenants.findById(u.getTenantId())
                    .map(t -> t.getStatus() == TenantStatus.ACTIVE)
                    .orElse(false);
        }
        return UserPrincipal.from(u, tenantActive);
    }
}