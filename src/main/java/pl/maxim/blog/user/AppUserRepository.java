package pl.maxim.blog.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Page<AppUser> findByUsernameContainingIgnoreCase(String q, Pageable pageable);
    Optional<AppUser> findByEmail(String email);
}
