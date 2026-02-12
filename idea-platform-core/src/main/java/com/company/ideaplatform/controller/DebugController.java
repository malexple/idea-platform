package com.company.ideaplatform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DebugController {

    private final PasswordEncoder passwordEncoder;

    @GetMapping("/debug/hash")
    public String generateHash(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }

    @GetMapping("/debug/verify")
    public String verifyPassword(
            @RequestParam String password,
            @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(password, hash);
        return "Password: " + password + "\nHash: " + hash + "\nMatches: " + matches;
    }
}
