package itk.student.task.manager.repository;

import itk.student.task.manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    Page<User> findByDeletedAtIsNullAndEmailContainingIgnoreCase(String search, Pageable pageable);

    Page<User> findByDeletedAtIsNull(Pageable pageable);
}
