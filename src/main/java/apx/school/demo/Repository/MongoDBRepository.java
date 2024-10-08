package apx.school.demo.Repository;

import apx.school.demo.Entity.BookEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MongoDBRepository extends MongoRepository<BookEntity, String> {

    @Query("{'author': {$regex: ?0, $options: 'i'}}")
    List<BookEntity> findByAuthor(String author);

    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    List<BookEntity> findByTitle(String title);

    @Query("{'availability': {$regex: ?0}}")
    List<BookEntity> findByAvailability(String availability);

    @Query("{'_id': ?0}")
    @Update("{'$set': {'availability': ?1}}")
    void updateAvailability(String id, String availability);
}