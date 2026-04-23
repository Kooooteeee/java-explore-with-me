package ru.practicum.ewm.main.user;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserDto newUserDto);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    void deleteUser(Long userId);
}
