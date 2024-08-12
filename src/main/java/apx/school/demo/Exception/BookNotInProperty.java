package apx.school.demo.Exception;

public class BookNotInProperty extends RuntimeException{

    public BookNotInProperty(){
        super("El libro que intento devolver no se encuentra en su propiedad");
    }
}
