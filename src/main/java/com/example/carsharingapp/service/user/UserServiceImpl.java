package com.example.carsharingapp.service.user;

import com.example.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.user.UserResponseDto;
import com.example.carsharingapp.exception.RegistrationException;
import com.example.carsharingapp.mapper.UserMapper;
import com.example.carsharingapp.model.Role;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.RoleRepository;
import com.example.carsharingapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;


    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("User with this email already exists");
        }
        Optional<Role> defaultRole = roleRepository.findByRole(Role.RoleName.CUSTOMER.name());
        User user = userMapper.toModel(requestDto);
        user.setPassword(encoder.encode(requestDto.getPassword()));
        user.getRoles().add(defaultRole.orElseThrow(() ->
                new EntityNotFoundException("Can't find role: " + Role.RoleName.CUSTOMER)));
        userRepository.save(user);
        return userMapper.toDto(user);
    }
}
