package pl.maxim.blog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import pl.maxim.blog.message.Message;
import pl.maxim.blog.message.MessageJdbcDao;
import pl.maxim.blog.message.MessageRepository;
import pl.maxim.blog.message.MessageService;
import pl.maxim.blog.message.dto.SendMessageRequest;
import pl.maxim.blog.security.CurrentUserService;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.AppUserRepository;
import pl.maxim.blog.user.Role;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceUnitTest {

    @Mock
    MessageRepository messageRepository;

    @Mock
    MessageJdbcDao messageJdbcDao;

    @Mock
    AppUserRepository userRepository;

    @Mock
    CurrentUserService currentUserService;

    @InjectMocks
    MessageService service;

    @Test
    void send_requires_recipient_exists() {
        AppUser sender = new AppUser();
        ReflectionTestUtils.setField(sender, "id", 1L);
        sender.setUsername("s");
        sender.setEmail("s@ex.com");
        sender.setPassword("p");
        sender.setRole(Role.USER);

        when(currentUserService.requireUser()).thenReturn(sender);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.send(new SendMessageRequest(2L, "hi")))
                .isInstanceOf(pl.maxim.blog.common.ResourceNotFoundException.class);
    }

    @Test
    void delete_forbidden_for_non_participant_and_not_admin() {
        AppUser sender = new AppUser();
        ReflectionTestUtils.setField(sender, "id", 1L);
        sender.setUsername("s");
        sender.setEmail("s@ex.com");
        sender.setPassword("p");
        sender.setRole(Role.USER);

        AppUser recipient = new AppUser();
        ReflectionTestUtils.setField(recipient, "id", 2L);
        recipient.setUsername("r");
        recipient.setEmail("r@ex.com");
        recipient.setPassword("p");
        recipient.setRole(Role.USER);

        Message m = new Message();
        ReflectionTestUtils.setField(m, "id", 100L);
        m.setSender(sender);
        m.setRecipient(recipient);
        m.setContent("x");

        AppUser other = new AppUser();
        ReflectionTestUtils.setField(other, "id", 3L);
        other.setUsername("o");
        other.setEmail("o@ex.com");
        other.setPassword("p");
        other.setRole(Role.USER);

        when(messageRepository.findById(100L)).thenReturn(Optional.of(m));
        when(currentUserService.requireUser()).thenReturn(other);
        when(currentUserService.isAdmin()).thenReturn(false);

        assertThatThrownBy(() -> service.delete(100L)).isInstanceOf(AccessDeniedException.class);
        verify(messageJdbcDao, never()).deleteById(anyLong());
    }
}
