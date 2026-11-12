package br.com.pizzaria.service;

import br.com.pizzaria.dtos.UserRequestDto;
import br.com.pizzaria.dtos.UserResponseDto;
import br.com.pizzaria.entity.User;
import br.com.pizzaria.exception.UserAlreadyExistsException;
import br.com.pizzaria.mapper.UserMapper;
import br.com.pizzaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto create(UserRequestDto dto){
       if (userRepository.existsByEmailIgnoreCase(dto.email())){
           throw new UserAlreadyExistsException("User already registered.");
       }
        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }



}
