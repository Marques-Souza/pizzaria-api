package br.com.pizzaria.service;

import br.com.pizzaria.dtos.UserRequestDto;
import br.com.pizzaria.dtos.UserResponseDto;
import br.com.pizzaria.entity.User;
import br.com.pizzaria.exception.UserAlreadyExistsException;
import br.com.pizzaria.mapper.UserMapper;
import br.com.pizzaria.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    void createRejectsDuplicateEmail() {
        UserRequestDto request = new UserRequestDto("Maria", "maria@example.com", "senha123");
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.create(request));
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void createEncodesPasswordBeforeSaving() {
        UserRequestDto request = new UserRequestDto("Maria", "maria@example.com", "senha123");
        User user = new User();
        UserResponseDto response = new UserResponseDto(
                UUID.randomUUID(), "Maria", "maria@example.com", user.getRole());
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(request.password())).thenReturn("bcrypt-hash");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        assertSame(response, userService.create(request));

        assertEquals("bcrypt-hash", user.getPassword());
        verify(userRepository).save(user);
    }
}
