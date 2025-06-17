package me.astroreen.liblanka.domain.auth.service;

import lombok.RequiredArgsConstructor;
import me.astroreen.liblanka.domain.auth.UserRole;
import me.astroreen.liblanka.domain.auth.dto.AuthenticationRequest;
import me.astroreen.liblanka.domain.auth.dto.AuthenticationResponse;
import me.astroreen.liblanka.domain.auth.dto.RegisterRequest;
import me.astroreen.liblanka.domain.auth.entity.User;
import me.astroreen.liblanka.domain.auth.exception.EmailAlreadyInUseException;
import me.astroreen.liblanka.domain.auth.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Register new user into the database.
     * @param request dto of the request that contains username, email and password
     * @throws EmailAlreadyInUseException thrown only when that email already exists
     */
    public void register(@NotNull RegisterRequest request) throws EmailAlreadyInUseException{

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new EmailAlreadyInUseException();
        }

        User user =
                User.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role(UserRole.USER)
                        .build();

        userRepository.save(user);
    }

    public AuthenticationResponse login(@NotNull AuthenticationRequest request) throws NoSuchElementException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user =
                userRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() -> new NoSuchElementException(UserService.USER_NOT_FOUND));

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
