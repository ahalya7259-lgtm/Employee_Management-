package com.employeehub.auth.service;

import com.employeehub.auth.dto.AuthResponse;
import com.employeehub.auth.dto.LoginRequest;
import com.employeehub.auth.entity.User;
import com.employeehub.auth.repository.UserRepository;
import com.employeehub.employee.entity.Employee;
import com.employeehub.employee.repository.EmployeeRepository;
import com.employeehub.exception.UnauthorizedException;
import com.employeehub.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        User user = userRepository.findByEmailWithRole(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().getName().name());
        claims.put("userId", user.getId());

        String token = jwtService.generateToken(claims, userDetails);

        Long employeeId = null;
        String fullName = null;
        Employee employee = employeeRepository.findByUserId(user.getId()).orElse(null);
        if (employee != null) {
            employeeId = employee.getId();
            fullName = employee.getFullName();
        }

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .role(user.getRole().getName().name())
                .employeeId(employeeId)
                .fullName(fullName)
                .build();
    }
}
