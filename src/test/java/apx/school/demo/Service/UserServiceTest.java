package apx.school.demo.Service;

import apx.school.demo.Entity.BookEntity;
import apx.school.demo.Entity.UserEntity;
import apx.school.demo.Dto.UserDto;
import apx.school.demo.Exception.BookNotExist;
import apx.school.demo.Exception.BookNotAvailability;
import apx.school.demo.Exception.BookNotInProperty;
import apx.school.demo.Exception.UserNotExist;
import apx.school.demo.Repository.MongoDBRepository;
import apx.school.demo.Repository.PostgreDBRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;



public class UserServiceTest {

    @Mock
    private PostgreDBRepository postgreDBRepository;

    @Mock
    private MongoDBRepository bookMongoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userService.setEmail("test@example.com");
    }

    @Test
    public void testGetAll() {
        // Arrange
        UserEntity userEntity = new UserEntity(1L, "John", "test@example.com", "password", new HashMap<>());
        when(postgreDBRepository.findAll()).thenReturn(List.of(userEntity));

        // Act
        var result = userService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getName());
    }

    @Test
    public void testGetMyData() {
        // Arrange
        UserEntity userEntity = new UserEntity(1L, "John", "test@example.com", "password", new HashMap<>());
        when(postgreDBRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));

        // Act
        var result = userService.getMyData();

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getName());
    }

    @Test
    public void testGetMyData_UserDoesNotExist() {
        // Arrange
        when(postgreDBRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotExist.class, () -> userService.getMyData());
    }

    @Test
    public void testGetById() {
        // Arrange
        UserEntity userEntity = new UserEntity(1L, "John", "test@example.com", "password", new HashMap<>());
        when(postgreDBRepository.findById(anyLong())).thenReturn(Optional.of(userEntity));

        // Act
        var result = userService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getName());
    }

    @Test
    public void testGetById_UserDoesNotExist() {
        // Arrange
        when(postgreDBRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotExist.class, () -> userService.getById(1L));
    }

    @Test
    public void testUpdate() {
        // Arrange
        UserEntity oldUser = new UserEntity(1L, "John", "test@example.com", "password", new HashMap<>());
        UserDto newUserDto = new UserDto(1L, "John Updated", "test@example.com", "newpassword", new HashMap<>());
        when(postgreDBRepository.findByEmail(anyString())).thenReturn(Optional.of(oldUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedpassword");
        when(postgreDBRepository.save(any(UserEntity.class))).thenReturn(new UserEntity(1L, "John Updated", "test@example.com", "encodedpassword", new HashMap<>()));

        // Act
        var result = userService.update(newUserDto);

        // Assert
        assertNotNull(result);
        assertEquals("John Updated", result.getName());
        assertEquals("encodedpassword", result.getPassword());
    }

    @Test
    public void testUpdate_UserDoesNotExist() {
        // Arrange
        UserDto userDto = new UserDto(1L, "John Updated", "test@example.com", "newpassword", new HashMap<>());
        when(postgreDBRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotExist.class, () -> userService.update(userDto));
    }

    @Test
    public void testDelete() {
        // Arrange
        when(postgreDBRepository.findById(anyLong())).thenReturn(Optional.of(new UserEntity()));

        // Act
        String result = userService.delete(1L);

        // Assert
        assertEquals("El usuario se elimino correctamente", result);
    }

    @Test
    public void testDelete_UserDoesNotExist() {
        // Arrange
        when(postgreDBRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotExist.class, () -> userService.delete(1L));
    }

    @Test
    public void testSetNewBook() {
        // Arrange
        BookEntity bookEntity = new BookEntity("book1", "Author", "Title", "Disponible");
        UserEntity userEntity = new UserEntity(1L, "John", "test@example.com", "password", new HashMap<>());
        when(bookMongoRepository.findById(anyString())).thenReturn(Optional.of(bookEntity));
        when(postgreDBRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
        doNothing().when(postgreDBRepository).updateMyBooks(anyString(), any(HashMap.class));
        doNothing().when(bookMongoRepository).updateAvailability(anyString(), anyString());

        // Act
        String result = userService.setNewBook("book1");

        // Assert
        assertEquals("El libro 'Title' se ha agregado a su colección", result);
    }

    @Test
    public void testSetNewBook_BookDoesNotExist() {
        // Arrange
        when(bookMongoRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookNotExist.class, () -> userService.setNewBook("book1"));
    }

    @Test
    public void testSetNewBook_BookNotAvailable() {
        // Arrange
        BookEntity bookEntity = new BookEntity("book1", "Author", "Title", "No disponible");
        when(bookMongoRepository.findById(anyString())).thenReturn(Optional.of(bookEntity));

        // Act & Assert
        assertThrows(BookNotAvailability.class, () -> userService.setNewBook("book1"));
    }

    @Test
    public void testRestoreBook() {
        // Arrange
        UserEntity userEntity = new UserEntity(1L, "John", "test@example.com", "password", new HashMap<>());
        userEntity.getMyBooks().put("book1", "Title");
        when(postgreDBRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
        doNothing().when(postgreDBRepository).updateMyBooks(anyString(), any(HashMap.class));
        doNothing().when(bookMongoRepository).updateAvailability(anyString(), anyString());

        // Act
        String result = userService.restoreBook("book1");

        // Assert
        assertEquals("Has devuelto el libro 'Title'", result);
    }

    @Test
    public void testRestoreBook_BookNotInProperty() {
        // Arrange
        UserEntity userEntity = new UserEntity(1L, "John", "test@example.com", "password", new HashMap<>());
        when(postgreDBRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));

        // Act & Assert
        assertThrows(BookNotInProperty.class, () -> userService.restoreBook("book1"));
    }
}
