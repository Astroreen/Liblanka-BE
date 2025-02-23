package me.astroreen.liblanka.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.astroreen.liblanka.domain.auth.UserRole;

@Getter
@Setter
@Builder
public class UserDto {
    private String name;
    private String email;
//    private String password; //we can't send password to the client
    private UserRole role;
    private byte[] profilePicture;
}
