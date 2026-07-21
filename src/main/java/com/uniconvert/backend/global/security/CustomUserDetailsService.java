package com.uniconvert.backend.global.security;

import lombok.RequiredArgsConstructor;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmail(username)
                .map(user -> new CustomUserDetails(user.getUserId(), user.getEmail()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
