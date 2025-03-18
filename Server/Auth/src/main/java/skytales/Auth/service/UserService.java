package skytales.Auth.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import skytales.Auth.model.Role;
import skytales.Auth.model.User;
import skytales.Auth.repository.UserRepository;
import skytales.Auth.web.dto.UserListItem;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    public UserService(UserRepository userRepository, JwtService jwtService, RestTemplate restTemplate, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;

    }

    public List<UserListItem> listUsers() {
        List<User> users = userRepository.findAll();
        return convertUsersToUserListRequest(users);
    }

    public boolean giveUserRole(String id) {

        try {
            UUID userId = UUID.fromString(id);

            User user = getById(userId);
            user.setRole(Role.USER);

            userRepository.save(user);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error while demoting user");
        }
    }

    public boolean giveAdminRole(String id) {
        try {
            UUID userId = UUID.fromString(id);

            User user = getById(userId);
            user.setRole(Role.ADMIN);

            userRepository.save(user);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error while promoting user");
        }
    }

    public List<UserListItem> convertUsersToUserListRequest(List<User> users) {
        return users.stream()
                .map(user -> new UserListItem( user.getId().toString() ,user.getUsername(), user.getEmail(), user.getRole().toString()))
                .collect(Collectors.toList());
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User was not found"));
    }


}
