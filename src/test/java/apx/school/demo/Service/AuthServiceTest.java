package apx.school.demo.Service;

import apx.school.demo.Dto.auth.AuthDto;
import apx.school.demo.Dto.auth.LoginDto;
import apx.school.demo.Dto.auth.RegisterDto;
import apx.school.demo.Entity.UserEntity;
import apx.school.demo.Exception.UserAlreadyExist;
import apx.school.demo.Exception.UserNotExist;
import apx.school.demo.Repository.PostgreDBRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private PostgreDBRepository postgreDBRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void login_UserExist(){
        // Configuración de mocks
        String email = "test@example.com";
        String password = "password";
        LoginDto loginDto = new LoginDto(email, password);

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);

        when(postgreDBRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(jwtService.getToken(any(UserDetails.class))).thenReturn("jwt-token");

        // Ejecución del método login
        AuthDto authDto = authService.login(loginDto);

        // Verificación
        assertNotNull(authDto);
        assertEquals("jwt-token", authDto.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).setEmail(email);
    }

    @Test
    public void login_UserNotExist(){
        // Configuration de mocks
        String email = "nonexistent@example.com";
        LoginDto loginDto = new LoginDto(email, "password");

        when(postgreDBRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Ejection y verification
        assertThrows(UserNotExist.class, () -> authService.login(loginDto));
    }

    @Test
    public void register_UserExist() {
        // Configuration de mocks
        String email = "test@example.com";
        RegisterDto registerDto = new RegisterDto("Test", email, "password");

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);

        when(postgreDBRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // Ejection y verification
        assertThrows(UserAlreadyExist.class, () -> authService.register(registerDto));
    }

    @Test
    public void register_UserNotExist() {
        // Configuración de mocks
        String email = "newuser@example.com";
        RegisterDto registerDto = new RegisterDto("New User", email, "password");

        when(postgreDBRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(jwtService.getToken(any(UserDetails.class))).thenReturn("jwt-token");

        // Ejecución del método register
        AuthDto authDto = authService.register(registerDto);

        // Verificación
        assertNotNull(authDto);
        assertEquals("jwt-token", authDto.getToken());
        verify(postgreDBRepository).save(any(UserEntity.class));
        verify(userService).setEmail(email);
    }
}
