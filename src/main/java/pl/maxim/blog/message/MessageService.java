package pl.maxim.blog.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.maxim.blog.common.ResourceNotFoundException;
import pl.maxim.blog.message.dto.MessageResponse;
import pl.maxim.blog.message.dto.SendMessageRequest;
import pl.maxim.blog.post.dto.UserSummary;
import pl.maxim.blog.security.CurrentUserService;
import pl.maxim.blog.user.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final MessageJdbcDao messageJdbcDao;
    private final AppUserRepository userRepository;
    private final CurrentUserService currentUserService;

    public MessageService(MessageRepository messageRepository, MessageJdbcDao messageJdbcDao, AppUserRepository userRepository, CurrentUserService currentUserService) {
        this.messageRepository = messageRepository;
        this.messageJdbcDao = messageJdbcDao;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public MessageResponse send(SendMessageRequest req) {
        var sender = currentUserService.requireUser();
        var recipient = userRepository.findById(req.recipientId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.recipientId()));

        Message m = new Message();
        m.setSender(sender);
        m.setRecipient(recipient);
        m.setContent(req.content());

        Message saved = messageRepository.save(m);
        log.info("Message sent from {} to {}", sender.getId(), recipient.getId());

        return new MessageResponse(
                saved.getId(),
                new UserSummary(sender.getId(), sender.getUsername()),
                new UserSummary(recipient.getId(), recipient.getUsername()),
                saved.getContent(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> inbox(Pageable pageable) {
        var user = currentUserService.requireUser();
        return messageRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(m -> new MessageResponse(
                        m.getId(),
                        new UserSummary(m.getSender().getId(), m.getSender().getUsername()),
                        new UserSummary(m.getRecipient().getId(), m.getRecipient().getUsername()),
                        m.getContent(),
                        m.getCreatedAt()
                ));
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> sent(Pageable pageable) {
        var user = currentUserService.requireUser();
        return messageRepository.findBySenderIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(m -> new MessageResponse(
                        m.getId(),
                        new UserSummary(m.getSender().getId(), m.getSender().getUsername()),
                        new UserSummary(m.getRecipient().getId(), m.getRecipient().getUsername()),
                        m.getContent(),
                        m.getCreatedAt()
                ));
    }

    @Transactional
    public void delete(Long id) {
        Message m = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + id));

        var user = currentUserService.requireUser();
        boolean participant = m.getSender().getId().equals(user.getId()) || m.getRecipient().getId().equals(user.getId());

        if (!participant && !currentUserService.isAdmin()) {
            throw new AccessDeniedException("Forbidden");
        }

        int rows = messageJdbcDao.deleteById(id);
        if (rows == 0) {
            throw new ResourceNotFoundException("Message not found: " + id);
        }
        log.info("Message {} deleted by user {}", id, user.getId());
    }
}
