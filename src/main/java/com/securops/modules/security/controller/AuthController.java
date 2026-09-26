package com.securops.modules.security.controller;

import com.securops.modules.security.dto.AuthDtos.*;
import com.securops.modules.security.entity.RoleType;
import com.securops.modules.security.entity.User;
import com.securops.modules.security.jwt.JwtTokenProvider;
import com.securops.modules.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.name()))
                .collect(Collectors.toList());

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        String token = jwtTokenProvider.generateToken(auth);

        Set<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(LoginResponseDto.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roles(roleNames)
                .expiresInMs(86400000)
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("El nombre de usuario ya está en uso");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("El correo electrónico ya está registrado");
        }

        Set<RoleType> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String r : request.getRoles()) {
                try {
                    roles.add(RoleType.valueOf(r.startsWith("ROLE_") ? r : "ROLE_" + r));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        if (roles.isEmpty()) {
            roles.add(RoleType.ROLE_GUARD);
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .roles(roles)
                .enabled(true)
                .build();

        userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente");
    }
}
