package br.unipar.frameworks.controller;

import br.unipar.frameworks.JwtService;
import br.unipar.frameworks.dto.LoginRequest;
import br.unipar.frameworks.dto.RegisterRequest;
import br.unipar.frameworks.dto.UserResponseDTO;
import br.unipar.frameworks.model.User;
import br.unipar.frameworks.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          JwtService jwtService,
                          BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        return ResponseEntity.ok(UserResponseDTO.from(userRepository.save(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return userRepository.findByEmail(request.email())
                .filter(user -> passwordEncoder.matches(request.password(), user.getPassword()))
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(Map.of(
                        "message", "Login realizado para laboratório",
                        "token", jwtService.gerarToken(user.getId(), user.getRole()),
                        "user", UserResponseDTO.from(user)
                )))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of(
                        "error", "Email ou senha inválidos"
                )));
    }
}
