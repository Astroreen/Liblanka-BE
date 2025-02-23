package me.astroreen.liblanka.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UpdateUserProfileResponse {
    private UserDto userDto;
    private String token;
}
