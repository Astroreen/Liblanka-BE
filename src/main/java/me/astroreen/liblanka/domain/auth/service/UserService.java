package me.astroreen.liblanka.domain.auth.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import me.astroreen.liblanka.common.excpetion.ResourceNotFoundException;
import me.astroreen.liblanka.domain.auth.UserRole;
import me.astroreen.liblanka.domain.auth.dto.*;
import me.astroreen.liblanka.domain.auth.entity.User;
import me.astroreen.liblanka.domain.auth.exception.EmailAlreadyInUseException;
import me.astroreen.liblanka.domain.auth.exception.IncorrectPasswordException;
import me.astroreen.liblanka.domain.auth.exception.PasswordConfirmationException;
import me.astroreen.liblanka.domain.auth.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springdoc.core.service.RequestBodyService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    public static final String USER_NOT_FOUND = "User not found";
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RequestBodyService requestBodyBuilder;

    public UserDto getUserDetails() {
        var user =
                userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                        .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        return UserDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .profilePicture(user.getImage())
                .build();
    }

    public UpdateUserProfileResponse updateUserDetails(@NotNull UpdateUserProfileRequest updateUserProfileRequest) {
        var user = authenticationService.getAuthenticatedUser();

        if (!user.getEmail().equals(updateUserProfileRequest.getEmail())) {
            var existingUser = userRepository.findByEmail(updateUserProfileRequest.getEmail());
            if (existingUser.isPresent()) {
                log.error("Email already in use: {}", updateUserProfileRequest.getEmail());
                throw new EmailAlreadyInUseException();
            }
            user.setEmail(updateUserProfileRequest.getEmail());
        }

        user.setName(updateUserProfileRequest.getName());
        user.setRole(updateUserProfileRequest.getRole());

        userRepository.save(user);

        var newJwtToken = jwtService.generateToken(user);

        var userDto = UserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return UpdateUserProfileResponse.builder().userDto(userDto).token(newJwtToken).build();
    }

    public User uploadProfilePicture(byte[] image) {
        User user = authenticationService.getAuthenticatedUser();

        user.setImage(image);
        userRepository.save(user);

        return user;
    }

    public void deleteProfilePicture() {
        User user = authenticationService.getAuthenticatedUser();

        user.setImage(null);

        userRepository.save(user);
    }

    public ChangePasswordResponse changePassword(ChangePasswordRequest changePasswordRequest) {
        final User user = authenticationService.getAuthenticatedUser();

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())){
            throw new IncorrectPasswordException();
        }

        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmationPassword())){
            throw new PasswordConfirmationException();
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));

        userRepository.save(user);

        final String newJwtToken = jwtService.generateToken(user);

        return ChangePasswordResponse.builder().token(newJwtToken).build();
    }
}