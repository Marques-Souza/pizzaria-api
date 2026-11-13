package br.com.pizzaria.service;


import br.com.pizzaria.dtos.SessionRequestDto;
import br.com.pizzaria.dtos.SessionResponseDto;
import br.com.pizzaria.exception.InvalidCredentialsException;
import br.com.pizzaria.mapper.UserMapper;
import br.com.pizzaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final long TOKEN_LIFETIME_SECONDS = 3600;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtEncoder jwtEncoder;

    @Transactional(readOnly = true)
    public SessionResponseDto login(SessionRequestDto dto){
        var user = userRepository.findByEmailIgnoreCase(dto.email())
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid email or password."
                ));
        if (!passwordEncoder.matches(dto.password(), user.getPassword())){
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("pizzaria-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(TOKEN_LIFETIME_SECONDS))
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        return new SessionResponseDto(token, userMapper.toResponse(user));

    }


}
