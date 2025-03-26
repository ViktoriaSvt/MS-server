package skytales.Auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import skytales.Auth.model.Role;
import skytales.Auth.model.User;
import skytales.Auth.repository.UserRepository;
import skytales.Auth.web.dto.UserListItem;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setRole(Role.USER);
    }

    @Test
    void testListUsers() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        List<UserListItem> users = userService.listUsers();
        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        assertEquals("testUser", users.get(0).username());
    }

    @Test
    void testGiveUserRole() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        boolean result = userService.giveUserRole(user.getId().toString());
        assertTrue(result);
        assertEquals(Role.USER, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGiveAdminRole() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        boolean result = userService.giveAdminRole(user.getId().toString());
        assertTrue(result);
        assertEquals(Role.ADMIN, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGetById_UserExists() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User foundUser = userService.getById(user.getId());
        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
    }

    @Test
    void testGetById_UserNotFound() {
        UUID randomId = UUID.randomUUID();
        when(userRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.getById(randomId));
    }

    @Test
    void testGiveUserRole_Exception() {

        when(userRepository.findById(any(UUID.class))).thenThrow(new RuntimeException("Error while demoting user"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.giveUserRole(user.getId().toString()));

        assertEquals("Error while demoting user", exception.getMessage());
    }

}
