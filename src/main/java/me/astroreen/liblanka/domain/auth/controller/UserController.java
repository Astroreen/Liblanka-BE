package me.astroreen.liblanka.domain.auth.controller;

import lombok.AllArgsConstructor;
import me.astroreen.liblanka.domain.auth.dto.UserDto;
import me.astroreen.liblanka.domain.auth.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/details")
    public UserDto getUserDetails() {
        return userService.getUserDetails();
    }
}
