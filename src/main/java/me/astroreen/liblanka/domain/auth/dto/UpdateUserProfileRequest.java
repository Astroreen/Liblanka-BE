package me.astroreen.liblanka.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import me.astroreen.liblanka.domain.auth.UserRole;

@Getter
@Setter

public class UpdateUserProfileRequest {

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email is not formatted")
    private String email;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z-'\\s]+$", message = "Name can only contain letters, spaces, hyphens, and apostrophes")
    private String name;

    private UserRole role;
}
