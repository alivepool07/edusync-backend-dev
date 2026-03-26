package com.project.edusync.common.security;

import com.project.edusync.common.exception.ResourceNotFoundException;
import com.project.edusync.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameWithAuthorities(username).orElseThrow(() ->
                new ResourceNotFoundException("UserDetails not found user : " + username)
        );
    }
}
