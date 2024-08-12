package apx.school.demo.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "El usuario no existe")
public class UserNotExist extends RuntimeException {

    public UserNotExist() {
        super("El usuario no existe");
    }
}
