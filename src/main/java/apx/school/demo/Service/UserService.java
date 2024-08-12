package apx.school.demo.Service;

import apx.school.demo.Exception.BookNotExist;
import apx.school.demo.Exception.BookNotAvailability;
import apx.school.demo.Exception.BookNotInProperty;
import apx.school.demo.Exception.UserNotExist;
import apx.school.demo.Dto.UserDto;
import apx.school.demo.Entity.BookEntity;
import apx.school.demo.Entity.UserEntity;
import apx.school.demo.Repository.MongoDBRepository;
import apx.school.demo.Repository.PostgreDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;


@Service
public class UserService {

    @Autowired
    private PostgreDBRepository postgreDBRepository;

    @Autowired
    private MongoDBRepository bookMongoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String email;

    public void setEmail(String email){ this.email = email; }

    public List<UserDto> getAll(){
        return this.postgreDBRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public UserDto getMyData(){
        return this.postgreDBRepository.findByEmail(this.email)
                .map(this::toDto)
                .orElseThrow(UserNotExist::new);
    }

    public UserDto getById(Long id){
        UserEntity entity = this.postgreDBRepository.findById(id)
                .orElseThrow(UserNotExist::new);
        return toDto(entity);
    }

    public UserDto update(UserDto request){
        UserEntity OldData = this.postgreDBRepository.findByEmail(this.email)
                .orElseThrow(UserNotExist::new);

        UserEntity NewData = new UserEntity();
        NewData.setId(OldData.getId());
        NewData.setName(request.getName());
        NewData.setEmail(this.email);
        NewData.setPassword(passwordEncoder.encode((request.getPassword())));
        NewData.setMyBooks(OldData.getMyBooks());
        UserEntity entity = this.postgreDBRepository.save(NewData);
        return toDto(entity);
    }

    public String setNewBook(String book_id){
        BookEntity book = this.bookMongoRepository.findById(book_id)
                .orElseThrow(BookNotExist::new);
        if (book.getAvailability().equals("No disponible")){
           throw new BookNotAvailability();
        }
        UserEntity user = this.postgreDBRepository.findByEmail(this.email)
                .orElseThrow(UserNotExist::new);

        HashMap<String, String> books = user.getMyBooks();
        books.put(book.getId(), book.getTitle());
        this.postgreDBRepository.updateMyBooks(this.email, books);
        this.bookMongoRepository.updateAvailability(book_id, "No disponible");
        return "El libro '"+book.getTitle()+"' se ha agregado a su colección";
    }

    public String delete(Long id){
        this.postgreDBRepository.findById(id)
                .orElseThrow(UserNotExist::new);

        this.postgreDBRepository.deleteById(id);
        return "El usuario se elimino correctamente";
    }

    public String restoreBook(String book_id){
        UserEntity user = this.postgreDBRepository.findByEmail(this.email)
                .orElseThrow(UserNotExist::new);

        HashMap<String, String> books = user.getMyBooks();
        String deletedBook = books.get(book_id);
        if (deletedBook == null){
            throw new BookNotInProperty();
        }
        books.remove(book_id);
        this.postgreDBRepository.updateMyBooks(this.email, books);
        this.bookMongoRepository.updateAvailability(book_id, "Disponible");
        return "Has devuelto el libro '"+deletedBook+"'";
    }


    private UserDto toDto(UserEntity entity){
        return new UserDto(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getMyBooks()
        );
    }
}
