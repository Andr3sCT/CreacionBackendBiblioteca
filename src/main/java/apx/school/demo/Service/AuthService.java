package apx.school.demo.Service;

import apx.school.demo.Dto.auth.AuthDto;
import apx.school.demo.Dto.auth.LoginDto;
import apx.school.demo.Dto.auth.RegisterDto;
import apx.school.demo.Entity.UserEntity;
import apx.school.demo.Exception.UserAlreadyExist;
import apx.school.demo.Exception.UserNotExist;
import apx.school.demo.Repository.UserPostgreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserPostgreRepository userPostgreRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    public AuthDto login(final LoginDto request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails user = userPostgreRepository.findByEmail(request.getUsername())
                .orElseThrow(UserNotExist::new);
        String token = jwtService.getToken(user);
        userService.setEmail(user.getUsername());
        return new AuthDto(token);
    }

    public AuthDto register(final RegisterDto request){
        Optional<UserEntity> entity = this.userPostgreRepository.findByEmail(request.getEmail());
        if (entity.isPresent()){
            throw new UserAlreadyExist();
        }

        UserEntity user = new UserEntity();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMyBooks(new HashMap<>());

        userPostgreRepository.save(user);
        userService.setEmail(user.getEmail());
        return new AuthDto(this.jwtService.getToken(user));
    }
}