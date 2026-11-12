package br.com.pizzaria.service;

import br.com.pizzaria.mapper.UserMapper;
import br.com.pizzaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

}
