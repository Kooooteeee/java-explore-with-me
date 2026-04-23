package ru.practicum.ewm.main.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(NewUserDto newUserDto) {
        if (!isUniqueEmail(newUserDto.getEmail())) {
            throw new ConflictException("Пользователь с такой почтой уже существует!");
        }
        User user = userRepository.save(UserMapper.toUser(newUserDto));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        List<User> users =  new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            Pageable pageable = PageRequest.of(from / size, size);
            users = userRepository.findAll(pageable).getContent();
        } else {
            users = userRepository.findAllByIdIn(ids);
        }
        return users.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteUser(Long userId) {
        findByIdOrThrow(userId);
        userRepository.deleteById(userId);
    }

    private boolean isUniqueEmail(String email) {
        return !userRepository.existsByEmail(email);
    }

    private User findByIdOrThrow(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("Такого пользователя не существует!");
        } else {
            return user.get();
        }
    }
}
