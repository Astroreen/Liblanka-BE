package me.astroreen.liblanka.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    @NotBlank(message="Old password is required")
    private String oldPassword;

    @NotBlank(message="New password is required")
    private String newPassword;

    @NotBlank(message="Confirmation password is required")
    private String confirmationPassword;
}
