package pl.maxim.blog.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.maxim.blog.user.dto.UserResponse;

@Service
public class UserService {

    private final AppUserRepository userRepository;

    public UserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> search(String q, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCase(q == null ? "" : q, pageable)
                .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole(), u.isEnabled()));
    }

    @Transactional(readOnly = true)
    public UserResponse me(String username) {
        AppUser u = userRepository.findByUsername(username).orElseThrow();
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole(), u.isEnabled());
    }
}
