package me.astroreen.liblanka.domain.auth.service;

import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.common.excpetion.ResourceNotFoundException;
import me.astroreen.liblanka.domain.auth.UserRole;
import me.astroreen.liblanka.domain.auth.dto.AuthenticationRequest;
import me.astroreen.liblanka.domain.auth.dto.AuthenticationResponse;
import me.astroreen.liblanka.domain.auth.dto.RegisterRequest;
import me.astroreen.liblanka.domain.auth.entity.User;
import me.astroreen.liblanka.domain.auth.exception.EmailAlreadyInUseException;
import me.astroreen.liblanka.domain.auth.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public void register(RegisterRequest request) {

        var existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new EmailAlreadyInUseException();
        }

        var user =
                User.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role(UserRole.USER)
                        .build();

        userRepository.save(user);
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user =
                userRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() -> new ResourceNotFoundException(UserService.USER_NOT_FOUND));

        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated.");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User user)) {
            throw new IllegalStateException("Authenticated principal is not a User.");
        }

        return user;
    }
}
