package ru.demoexam.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.demoexam.backend.domain.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByLoginAndPasswordAndActiveTrue(String login, String password);

    List<User> findByRoleInAndActiveTrueOrderByFullNameAsc(List<String> roles);
}
