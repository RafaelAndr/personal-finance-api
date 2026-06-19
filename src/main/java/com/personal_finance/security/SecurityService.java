package com.personal_finance.security;

import com.personal_finance.entity.Users;
import com.personal_finance.repository.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityService {

    private final UsersRepository usersRepository;

    public Users getUserLoggedIn(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        return usersRepository.findByUsername(username).orElseThrow(
                () -> new EntityNotFoundException(String.format("Username '%s' not found", username)));
    }
}