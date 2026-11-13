package br.com.pizzaria.service;

import br.com.pizzaria.dtos.SessionRequestDto;
import br.com.pizzaria.dtos.SessionResponseDto;
import br.com.pizzaria.entity.User;
import br.com.pizzaria.exception.InvalidCredentialsException;
import br.com.pizzaria.mapper.UserMapper;
import br.com.pizzaria.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtEncoder jwtEncoder;
    @InjectMocks
    private SessionService sessionService;

    @Test
    void loginRejectsUnknownEmail() {
        SessionRequestDto request = new SessionRequestDto("nobody@example.com", "senha123");
        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> sessionService.login(request));
        verify(jwtEncoder, never()).encode(any());
    }

    @Test
    void loginRejectsIncorrectPassword() {
        SessionRequestDto request = new SessionRequestDto("maria@example.com", "senhaerrada");
        User user = new User();
        user.setPassword("stored-hash");
        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> sessionService.login(request));
        verify(jwtEncoder, never()).encode(any());
    }

    @Test
    void loginReturnsEncodedJwtAndPublicUser() {
        UUID userId = UUID.randomUUID();
        SessionRequestDto request = new SessionRequestDto("maria@example.com", "senha123");
        User user = new User();
        user.setId(userId);
        user.setPassword("stored-hash");
        Jwt encodedJwt = Jwt.withTokenValue("signed-token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .claim("role", user.getRole().name())
                .build();
        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(encodedJwt);

        SessionResponseDto response = sessionService.login(request);

        assertEquals("signed-token", response.accessToken());
        verify(userMapper).toResponse(user);
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }
}
