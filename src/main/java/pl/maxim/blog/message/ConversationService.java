package pl.maxim.blog.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.maxim.blog.message.dto.ConversationMessageResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ConversationService {

    private final MessageJdbcDao jdbcDao;

    public ConversationService(MessageJdbcDao jdbcDao) {
        this.jdbcDao = jdbcDao;
    }

    @Transactional(readOnly = true)
    public Page<ConversationMessageResponse> conversation(Long meId, Long otherId, Pageable pageable) {
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        long total = jdbcDao.conversationCount(meId, otherId);
        List<ConversationMessageResponse> rows = new ArrayList<>(jdbcDao.conversation(meId, otherId, limit, offset));

        Collections.reverse(rows);

        return new PageImpl<>(rows, pageable, total);
    }
}
