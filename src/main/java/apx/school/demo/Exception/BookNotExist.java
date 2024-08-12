package apx.school.demo.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "El libro no existe")
public class BookNotExist extends RuntimeException{

    public BookNotExist(){
        super("El libro no existe");
    }
}
